package codegen_example.codegen;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import org.objectweb.asm.Label;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

import codegen_example.syntax.*;

// Assumptions:
// -The input program is well-typed
// -Shadowed variables have been renamed to ensure name-level uniqueness.
//  For example:
//
//  int x = 0;
//  { int x = 1; print(x); }
//  print(x);
//
//  ...becomes:
//
//  int x_1 = 0;
//  { int x_2 = 1; print(x_2); }
//  print(x_2);
//
// Helpful resources:
// -Basics on Java Bytecode:
//     - JVM bytecode in general: https://www.beyondjava.net/java-programmers-guide-java-byte-code
//     - Writing bytecode with ASM: https://www.beyondjava.net/quick-guide-writing-byte-code-asm
// -ASM reference manual: https://asm.ow2.io/asm4-guide.pdf
// -ASM Javadoc: https://javadoc.io/doc/org.ow2.asm/asm/latest/index.html
// -Writing classes from scratch with ASM: https://dzone.com/articles/fully-dynamic-classes-with-asm
public class ClassGenerator {
    // ---BEGIN CONSTANTS---
    public static final Variable thisVariable = new Variable("this");
    public static final String objectName = "java/lang/Object";
    // ---END CONSTANTS---
    
    // ---BEGIN INSTANCE VARIABLES---
    private final Map<ClassName, ClassDefinition> allClasses;
    // ---END INSTANCE VARIABLES---

    public ClassGenerator(final Program program) throws CodeGeneratorException {
        this(program.classDefs);
    } // ClassGenerator
    
    public ClassGenerator(final List<ClassDefinition> allClasses)
        throws CodeGeneratorException {
        this.allClasses = new HashMap<ClassName, ClassDefinition>();
        for (final ClassDefinition classDef : allClasses) {
            if (this.allClasses.containsKey(classDef.name)) {
                throw new CodeGeneratorException("Redefinition of class: " + classDef.name);
            }
            this.allClasses.put(classDef.name, classDef);
        }
    } // ClassGenerator

    public static String printlnDescriptorString(final Type forType) {
        final String inner;
        if (forType instanceof ReferenceType) {
            inner = new ReferenceType(new ClassName(objectName)).toDescriptorString();
        } else {
            inner = forType.toDescriptorString();
        }
        return "(" + inner + ")V";
    } // printlnDescriptorString
    
    public void writeClasses() throws CodeGeneratorException, IOException {
        for (final ClassDefinition classDef : allClasses.values()) {
            final SingleClassGenerator genClass = new SingleClassGenerator(classDef);
            genClass.writeClass();
        }
    } // writeClasses
    
    private ClassDefinition classDefFor(final ClassName name) throws CodeGeneratorException {
        final ClassDefinition classDef = allClasses.get(name);
        if (classDef == null) {
            throw new CodeGeneratorException("No such class: " + name);
        } 
        return classDef;
    } // classDefFor
    
    private String constructorDescriptorFor(final ClassName name) throws CodeGeneratorException {
        if (name.name.equals(objectName)) {
            return "()V";
        } else {
            return classDefFor(name).constructor.toDescriptorString();
        }
    } // constructorDescriptorFor

    private String methodDescriptorFor(final ClassName className,
                                       final MethodName methodName) throws CodeGeneratorException {
        if (className.name.equals(objectName)) {
            throw new CodeGeneratorException("Nonexistant method: " + methodName.name);
        } else {
            final ClassDefinition classDef = classDefFor(className);
            for (final MethodDefinition methodDef : classDef.methods) {
                if (methodDef.name.equals(methodName)) {
                    return methodDef.toDescriptorString();
                }
            }
            return methodDescriptorFor(classDef.extendsName, methodName);
        }
    } // methodDescriptorFor

    private String fieldDescriptorFor(final ClassName className,
                                     final Variable fieldName) throws CodeGeneratorException {
        if (className.name.equals(objectName)) {
            throw new CodeGeneratorException("Nonexistant field: " + fieldName.name);
        } else {
            final ClassDefinition classDef = classDefFor(className);
            for (final FormalParam field : classDef.instanceVariables) {
                if (field.variable.equals(fieldName)) {
                    return field.type.toDescriptorString();
                }
            }
            return fieldDescriptorFor(classDef.extendsName, fieldName);
        }
    } // fieldDescriptorFor
    
    private class SingleClassGenerator {
        // ---BEGIN INSTANCE VARIABLES---
        private final ClassDefinition forClass;
        private final ReferenceType thisType;
        private final ClassWriter classWriter;
        // ---END INSTANCE VARIABLES---

        public SingleClassGenerator(final ClassDefinition forClass) throws CodeGeneratorException {
            this.forClass = forClass;
            thisType = new ReferenceType(forClass.name);
            classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classWriter.visit(V1_7, // Java 1.7
                              ACC_PUBLIC, // public
                              forClass.name.name, // class name
                              null, // signature (null means not generic)
                              forClass.extendsName.name, // superclass
                              new String[0]); // interfaces (none)
        } // SingleClassGenerator

        public void writeClass() throws CodeGeneratorException, IOException {
            SingleMethodGenerator methodGen = new SingleMethodGenerator(forClass.constructor);
            methodGen.writeMethod();
            methodGen = new SingleMethodGenerator(forClass.main);
            methodGen.writeMethod();
            for (final MethodDefinition method : forClass.methods) {
                methodGen = new SingleMethodGenerator(method);
                methodGen.writeMethod();
            }
            classWriter.visitEnd();

            final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(new File(forClass.name.name + ".class")));
            output.write(classWriter.toByteArray());
            output.close();
        } // writeClass
        
        private class SingleMethodGenerator {
            // ---BEGIN INSTANCE VARIABLES---
            private final Callable method;
            private final VariableTable variables;
            private final MethodVisitor methodVisitor;
            // ---END INSTANCE VARIABLES---

            // TODO: the instanceofs in here are very hacky
            public SingleMethodGenerator(final Callable method) throws CodeGeneratorException {
                this.method = method;
                final int flags = (method instanceof MainDefinition) ? ACC_PUBLIC | ACC_STATIC : ACC_PUBLIC;
                variables = VariableTable.withFormalParamsFrom(thisType, method);
                methodVisitor = classWriter.visitMethod(flags,
                                                        method.name.name,
                                                        method.toDescriptorString(),
                                                        null,
                                                        null);
            } // SingleMethodGenerator

            private void loadVariable(final Variable variable) throws CodeGeneratorException {
                variables.getEntryFor(variable).load(methodVisitor);
            } // loadVariable

            private void storeVariable(final Variable variable) throws CodeGeneratorException {
                variables.getEntryFor(variable).store(methodVisitor);
            } // storeVariable

            private void doReturn(final Type returnType) throws CodeGeneratorException {
                if (returnType instanceof IntType ||
                    returnType instanceof BoolType) {
                    methodVisitor.visitInsn(IRETURN);
                } else if (returnType instanceof VoidType) {
                    methodVisitor.visitInsn(RETURN);
                } else if (returnType instanceof ReferenceType) {
                    methodVisitor.visitInsn(ARETURN);
                } else {
                    assert(false);
                    throw new CodeGeneratorException("Unknown type: " + returnType);
                }
            } // doReturn
            
            private void writeIntLiteral(final int value) {
                switch (value) {
                case -1:
                    methodVisitor.visitInsn(ICONST_M1);
                    break;
                case 0:
                    methodVisitor.visitInsn(ICONST_0);
                    break;
                case 1:
                    methodVisitor.visitInsn(ICONST_1);
                    break;
                case 2:
                    methodVisitor.visitInsn(ICONST_2);
                    break;
                case 3:
                    methodVisitor.visitInsn(ICONST_3);
                    break;
                case 4:
                    methodVisitor.visitInsn(ICONST_4);
                    break;
                case 5:
                    methodVisitor.visitInsn(ICONST_5);
                    break;
                default:
                    methodVisitor.visitLdcInsn(Integer.valueOf(value));
                }
            } // writeIntLiteral
            
            private void writeArithmeticComparisonOp(final BOP bop) throws CodeGeneratorException {
                // there is no direct instruction for these, but instead a branching
                // version IF.  Basic idea (with <):
                //   push left (assumed written already)
                //   push right (assumed written  already)
                //   iflt is_less_than
                //   push 0
                //   goto after_less_than
                // is_less_than:
                //   push 1
                // after_less_than:
                final Label conditionTrue = new Label();
                final Label afterCondition = new Label();
                if (bop instanceof LessThanBOP) {
                    methodVisitor.visitJumpInsn(IF_ICMPLT, conditionTrue);
                } else if (bop instanceof EqualsBOP) {
                    methodVisitor.visitJumpInsn(IF_ICMPEQ, conditionTrue);
                } else {
                    assert(false);
                    throw new CodeGeneratorException("Unrecognized operation: " + bop);
                }
                writeIntLiteral(0);
                methodVisitor.visitJumpInsn(GOTO, afterCondition);
                methodVisitor.visitLabel(conditionTrue);
                writeIntLiteral(1);
                methodVisitor.visitLabel(afterCondition);
            } // writeArithmeticComparisonOp
            
            private void writeOp(final BOP bop) throws CodeGeneratorException {
                if (bop instanceof PlusBOP) {
                    methodVisitor.visitInsn(IADD);
                } else if (bop instanceof MinusBOP) {
                    methodVisitor.visitInsn(ISUB);
                } else if (bop instanceof DivBOP) {
                    methodVisitor.visitInsn(IDIV);
                } else if (bop instanceof MultBOP) {
                    methodVisitor.visitInsn(IMUL);
                } else if (bop instanceof LessThanBOP ||
                           bop instanceof EqualsBOP) {
                    writeArithmeticComparisonOp(bop);
                } else {
                    assert(false);
                    throw new CodeGeneratorException("unknown binary operator: " + bop);
                }
            } // writeOp
            
            private void writeMethodCall(final MethodCallExp call) throws CodeGeneratorException {
                writeExpression(call.callOn);
                writeExpressions(call.actualParams);
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
                                              call.callOnName.name,
                                              call.name.name,
                                              methodDescriptorFor(call.callOnName,
                                                                  call.name),
                                              false);
            } // writeMethodCall

            private void writeNew(final NewExp newExp) throws CodeGeneratorException {
                methodVisitor.visitTypeInsn(NEW, newExp.name.name);
                
                // calling the constructor will pop this off
                methodVisitor.visitInsn(DUP);

                writeExpressions(newExp.actualParams);
                methodVisitor.visitMethodInsn(INVOKESPECIAL,
                                              newExp.name.name,
                                              "<init>",
                                              constructorDescriptorFor(newExp.name),
                                              false);
            } // writeNew

            private void writeGet(final GetExp getExp) throws CodeGeneratorException {
                writeExpression(getExp.target);
                methodVisitor.visitFieldInsn(GETFIELD,
                                             getExp.name.name,
                                             getExp.field.name,
                                             fieldDescriptorFor(getExp.name, getExp.field));
            } // writeGet
            
            private void writeExpressions(final List<Exp> exps) throws CodeGeneratorException {
                for (final Exp exp : exps) {
                    writeExpression(exp);
                }
            } // writeExpressions

            private void writeExpression(final Exp exp) throws CodeGeneratorException {
                if (exp instanceof VariableExp) {
                    loadVariable(((VariableExp)exp).variable);
                } else if (exp instanceof IntegerLiteralExp) {
                    writeIntLiteral(((IntegerLiteralExp)exp).value);
                } else if (exp instanceof BooleanLiteralExp) {
                    final boolean value = ((BooleanLiteralExp)exp).value;
                    writeIntLiteral((value) ? 1 : 0);
                } else if (exp instanceof BinopExp) {
                    final BinopExp asBinop = (BinopExp)exp;
                    writeExpression(asBinop.left);
                    writeExpression(asBinop.right);
                    writeOp(asBinop.bop);
                } else if (exp instanceof MethodCallExp) {
                    writeMethodCall((MethodCallExp)exp);
                } else if (exp instanceof NewExp) {
                    writeNew((NewExp)exp);
                } else if (exp instanceof GetExp) {
                    writeGet((GetExp)exp);
                } else {
                    assert(false);
                    throw new CodeGeneratorException("Unrecognized expression: " + exp);
                }
            } // writeExpression

            private void writePrint(final Variable variable) throws CodeGeneratorException {
                final VariableEntry entry = variables.getEntryFor(variable);
                final String descriptor = printlnDescriptorString(entry.type);

                methodVisitor.visitFieldInsn(GETSTATIC,
                                             "java/lang/System",
                                             "out",
                                             new ReferenceType(new ClassName("java/io/PrintStream")).toDescriptorString());
                loadVariable(variable);
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
                                              "java/io/PrintStream",
                                              "println",
                                              descriptor,
                                              false);
            } // writePrint
            
            private void writeIfStatement(final IfStmt ifStmt) throws CodeGeneratorException {
                // if false, jump to the else branch.  If true, fall through to true branch.
                // true branch needs to jump after the false.  Looks like this:
                //
                //   condition_expression
                //   if !condition, jump to false
                //   true stuff
                //   goto after_false
                // false:
                //   false stuff
                // after_false:
                
                // condition is a boolean, which is represented with an integer which is either
                // 0 or 1.  IFEQ jumps if the value on top of the operand stack is 0, so this naturally
                // ends up giving us the if !condition (as odd as it looks)
                final Label falseLabel = new Label();
                final Label afterFalseLabel = new Label();
                writeExpression(ifStmt.guard);
                methodVisitor.visitJumpInsn(IFEQ, falseLabel);
                writeStatements(ifStmt.trueBranch);
                methodVisitor.visitJumpInsn(GOTO, afterFalseLabel);
                methodVisitor.visitLabel(falseLabel);
                writeStatements(ifStmt.falseBranch);
                methodVisitor.visitLabel(afterFalseLabel);
            } // writeIfStatement
            
            private void writeWhileStatement(final WhileStmt whileStmt) throws CodeGeneratorException {
                // head:
                //   condition_expression
                //   if !condition, jump to after_while
                //   body
                //   goto head
                // after_while

                final Label head = new Label();
                final Label afterWhile = new Label();
                methodVisitor.visitLabel(head);
                writeExpression(whileStmt.guard);
                methodVisitor.visitJumpInsn(IFEQ, afterWhile);
                writeStatements(whileStmt.body);
                methodVisitor.visitJumpInsn(GOTO, head);
                methodVisitor.visitLabel(afterWhile);
            } // whileWhileStatement

            private void writePutStatement(final PutStmt put) throws CodeGeneratorException {
                writeExpression(put.target);
                writeExpression(put.putHere);
                methodVisitor.visitFieldInsn(PUTFIELD,
                                             put.name.name,
                                             put.field.name,
                                             fieldDescriptorFor(put.name, put.field));
            } // writePutStatement
            
            private void writeStatements(final List<Stmt> stmts) throws CodeGeneratorException {
                for (final Stmt statement : stmts) {
                    writeStatement(statement);
                }
            } // writeStatements
            
            private void writeStatement(final Stmt stmt) throws CodeGeneratorException {
                if (stmt instanceof VariableDeclarationStmt) {
                    final VariableDeclarationStmt asDec = (VariableDeclarationStmt)stmt;
                    variables.addEntry(asDec.variable, asDec.type);
                    writeExpression(asDec.exp);
                    storeVariable(asDec.variable);
                } else if (stmt instanceof AssignStmt) {
                    final AssignStmt asAssign = (AssignStmt)stmt;
                    writeExpression(asAssign.exp);
                    storeVariable(asAssign.variable);
                } else if (stmt instanceof PrintStmt) {
                    writePrint(((PrintStmt)stmt).variable);
                } else if (stmt instanceof IfStmt) {
                    writeIfStatement((IfStmt)stmt);
                } else if (stmt instanceof WhileStmt) {
                    writeWhileStatement((WhileStmt)stmt);
                } else if (stmt instanceof PutStmt) {
                    writePutStatement((PutStmt)stmt);
                } else {
                    assert(false);
                    throw new CodeGeneratorException("Unrecognized statement: " + stmt);
                }
            } // writeStatement
            
            public void writeMethod() throws CodeGeneratorException {
                methodVisitor.visitCode();
                if (method instanceof Constructor) {
                    // ---BEGIN CODE FOR SUPER---
                    loadVariable(thisVariable); // load "this"
                    writeExpressions(((Constructor)method).superParams);
                    methodVisitor.visitMethodInsn(INVOKESPECIAL,
                                                  forClass.extendsName.name,
                                                  "<init>",
                                                  constructorDescriptorFor(forClass.extendsName),
                                                  false);
                    // ---END CODE FOR SUPER---
                }
                writeStatements(method.body);
                if (method instanceof MethodDefinition) {
                    loadVariable(((MethodDefinition)method).returned);
                }
                doReturn(method.returnType);
                methodVisitor.visitMaxs(0, 0);
            } // writeMethod
        } // SingleMethodGenerator
    } // SingleClassGenerator
} // ClassGenerator

/*
        
    public void writeClass(final ClassDefinition classDef) throws CodeGeneratorException, IOException {
        final ClassWriter classWriter =
            new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classWriter.visit(V1_7, // Java 1.7
                          ACC_PUBLIC, // public
                          classDef.name.name, // class name
                          null, // signature (null means not generic)
                          classDef.extendsName.name, // superclass
                          new String[0]); // interfaces (none)
        

} // ClassGenerator

public class ClassGenerator {
    // ---BEGIN INSTANCE VARIABLES---
    public final DescriptorString emptyVoid;
    public final String outputClassName;
    public final FunctionName outputMethodName;

    private final Map<FunctionName, Function> functionTable;
    private final ClassWriter classWriter;
    private Map<Variable, VariableEntry> variables;
    private int nextIndex;
    private MethodVisitor methodVisitor;
    // ---END INSTANCE VARIABLES
    
    public ClassGenerator(final String outputClassName,
                          final FunctionName outputMethodName) throws CodeGeneratorException {
        emptyVoid = DescriptorString.makeDescriptorString(new ArrayList<DescriptorParam>(),
                                                          new DescriptorParamVoid());
        this.outputClassName = outputClassName;
        this.outputMethodName = outputMethodName;
        classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        variables = null;
        nextIndex = 0;

        functionTable = new HashMap<FunctionName, Function>();
        classWriter.visit(V1_7, // Java 1.7
                          ACC_PUBLIC, // public
                          outputClassName, // class name
                          null, // signature (null means not generic)
                          "java/lang/Object", // superclass
                          new String[0]); // interfaces (none)

        // ---BEGIN CONSTRUCTOR DEFINITION---
        final MethodVisitor constructor =
            classWriter.visitMethod(ACC_PUBLIC, // access modifier
                                    "<init>", // method name (constructor)
                                    emptyVoid.descriptorString, // descriptor
                                    null, // signature (null means not generic)
                                    null); // exceptions
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0); // load "this"
        constructor.visitMethodInsn(INVOKESPECIAL,
                                    "java/lang/Object",
                                    "<init>",
                                    emptyVoid.descriptorString,
                                    false); // super()
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(0, 0);
        // ---END CONSTRUCTOR DEFINITION---

        // ---BEGIN MAIN DEFINITION---
        final MethodVisitor main =
            classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC,
                                    "main",
                                    "([Ljava/lang/String;)V",
                                    null,
                                    null);
        main.visitCode();
        main.visitMethodInsn(INVOKESTATIC,
                             outputClassName,
                             outputMethodName.name,
                             emptyVoid.descriptorString,
                             false);
        main.visitInsn(RETURN);
        main.visitMaxs(0, 0);
        // ---END MAIN DEFINITION---

        methodVisitor = null;
    } // CodeGenerator

    private void functionStart(final Function function) throws CodeGeneratorException {
        functionStart(function,
                      DescriptorString.makeDescriptorString(function));
    } // functionStart

    private void functionStart(final Function function,
                               final DescriptorString descriptor)
        throws CodeGeneratorException {
        assert(variables == null);
        assert(nextIndex == 0);
        assert(methodVisitor == null);

        variables = new HashMap<Variable, VariableEntry>();
        for (final FormalParam formalParam : function.formalParams) {
            addEntry(formalParam.variable, formalParam.type);
        }

        methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC,
                                                function.name.name,
                                                descriptor.descriptorString,
                                                null,
                                                null);
        methodVisitor.visitCode();
    } // functionStart
    
    private void functionEnd() {
        assert(variables != null);
        assert(methodVisitor != null);

        methodVisitor.visitMaxs(0, 0);
        nextIndex = 0;
        variables = null;
        methodVisitor = null;
    } // functionEnd
    
    private VariableEntry getEntryFor(final Variable variable) throws CodeGeneratorException {
        final VariableEntry entry = variables.get(variable);
        if (entry != null) {
            return entry;
        } else {
            // should be caught by typechecker
            throw new CodeGeneratorException("no such variable declared: " + variable);
        }
    } // getEntryFor

    private VariableEntry addEntry(final Variable variable, final Type type) throws CodeGeneratorException {
        if (variables.containsKey(variable)) {
            // should be caught by typechecker
            throw new CodeGeneratorException("Variable already in scope: " + variable);
        } else {
            final VariableEntry entry = new VariableEntry(variable, type, nextIndex++);
            variables.put(variable, entry);
            return entry;
        }
    } // addEntry

    private void writeIntLiteral(final int value) {
        switch (value) {
        case -1:
            methodVisitor.visitInsn(ICONST_M1);
            break;
        case 0:
            methodVisitor.visitInsn(ICONST_0);
            break;
        case 1:
            methodVisitor.visitInsn(ICONST_1);
            break;
        case 2:
            methodVisitor.visitInsn(ICONST_2);
            break;
        case 3:
            methodVisitor.visitInsn(ICONST_3);
            break;
        case 4:
            methodVisitor.visitInsn(ICONST_4);
            break;
        case 5:
            methodVisitor.visitInsn(ICONST_5);
            break;
        default:
            methodVisitor.visitLdcInsn(Integer.valueOf(value));
        }
    } // writeIntLiteral

    private void writeArithmeticComparisonOp(final BOP bop) throws CodeGeneratorException {
        // there is no direct instruction for these, but instead a branching
        // version IF.  Basic idea (with <):
        //   push left (assumed written already)
        //   push right (assumed written  already)
        //   iflt is_less_than
        //   push 0
        //   goto after_less_than
        // is_less_than:
        //   push 1
        // after_less_than:
        final Label conditionTrue = new Label();
        final Label afterCondition = new Label();
        if (bop instanceof LessThanBOP) {
            methodVisitor.visitJumpInsn(IF_ICMPLT, conditionTrue);
        } else if (bop instanceof EqualsBOP) {
            methodVisitor.visitJumpInsn(IF_ICMPEQ, conditionTrue);
        } else {
            assert(false);
            throw new CodeGeneratorException("Unrecognized operation: " + bop);
        }
        writeIntLiteral(0);
        methodVisitor.visitJumpInsn(GOTO, afterCondition);
        methodVisitor.visitLabel(conditionTrue);
        writeIntLiteral(1);
        methodVisitor.visitLabel(afterCondition);
    } // writeArithmeticComparisonOp
            
    private void writeOp(final BOP bop) throws CodeGeneratorException {
        if (bop instanceof PlusBOP) {
            methodVisitor.visitInsn(IADD);
        } else if (bop instanceof MinusBOP) {
            methodVisitor.visitInsn(ISUB);
        } else if (bop instanceof DivBOP) {
            methodVisitor.visitInsn(IDIV);
        } else if (bop instanceof MultBOP) {
            methodVisitor.visitInsn(IMUL);
        } else if (bop instanceof LessThanBOP ||
                   bop instanceof EqualsBOP) {
            writeArithmeticComparisonOp(bop);
        } else {
            assert(false);
            throw new CodeGeneratorException("unknown binary operator: " + bop);
        }
    } // writeOp

    private void writeFunctionCall(final FunctionCallExp call) throws CodeGeneratorException {
        final Function function = functionTable.get(call.name);
        if (function == null) {
            throw new CodeGeneratorException("Call to nonexistent function: " + function.name);
        }

        final DescriptorString descriptor = DescriptorString.makeDescriptorString(function);

        for (final Exp param : call.actualParams) {
            writeExpression(param);
        }
        methodVisitor.visitMethodInsn(INVOKESTATIC,
                                      outputClassName,
                                      call.name.name,
                                      descriptor.descriptorString,
                                      false);
    } // writeFunctionCall
                                      
    private void writeExpression(final Exp exp) throws CodeGeneratorException {
        if (exp instanceof VariableExp) {
            getEntryFor(((VariableExp)exp).variable).load(methodVisitor);
        } else if (exp instanceof IntegerLiteralExp) {
            writeIntLiteral(((IntegerLiteralExp)exp).value);
        } else if (exp instanceof BooleanLiteralExp) {
            final boolean value = ((BooleanLiteralExp)exp).value;
            writeIntLiteral((value) ? 1 : 0);
        } else if (exp instanceof BinopExp) {
            final BinopExp asBinop = (BinopExp)exp;
            writeExpression(asBinop.left);
            writeExpression(asBinop.right);
            writeOp(asBinop.bop);
        } else if (exp instanceof FunctionCallExp) {
            writeFunctionCall((FunctionCallExp)exp);
        } else {
            assert(false);
            throw new CodeGeneratorException("Unrecognized expression: " + exp);
        }
    } // writeExpression

    public void writePrint(final Variable variable) throws CodeGeneratorException {
        final VariableEntry entry = getEntryFor(variable);
        final String descriptor;
        if (entry.type instanceof IntType) {
            descriptor = "(I)V";
        } else if (entry.type instanceof BoolType) {
            descriptor = "(Z)V";
        } else {
            assert(false);
            throw new CodeGeneratorException("Unrecognized type; " + entry.type);
        }

        methodVisitor.visitFieldInsn(GETSTATIC,
                                     "java/lang/System",
                                     "out",
                                     "Ljava/io/PrintStream;");
        entry.load(methodVisitor);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
                                      "java/io/PrintStream",
                                      "println",
                                      descriptor,
                                      false);
    } // writePrint

    public void writeIfStatement(final IfStmt ifStmt) throws CodeGeneratorException {
        // if false, jump to the else branch.  If true, fall through to true branch.
        // true branch needs to jump after the false.  Looks like this:
        //
        //   condition_expression
        //   if !condition, jump to false
        //   true stuff
        //   goto after_false
        // false:
        //   false stuff
        // after_false:

        // condition is a boolean, which is represented with an integer which is either
        // 0 or 1.  IFEQ jumps if the value on top of the operand stack is 0, so this naturally
        // ends up giving us the if !condition (as odd as it looks)
        final Label falseLabel = new Label();
        final Label afterFalseLabel = new Label();
        writeExpression(ifStmt.guard);
        methodVisitor.visitJumpInsn(IFEQ, falseLabel);
        writeStatements(ifStmt.trueBranch);
        methodVisitor.visitJumpInsn(GOTO, afterFalseLabel);
        methodVisitor.visitLabel(falseLabel);
        writeStatements(ifStmt.falseBranch);
        methodVisitor.visitLabel(afterFalseLabel);
    } // writeIfStatement

    public void writeWhileStatement(final WhileStmt whileStmt) throws CodeGeneratorException {
        // head:
        //   condition_expression
        //   if !condition, jump to after_while
        //   body
        //   goto head
        // after_while

        final Label head = new Label();
        final Label afterWhile = new Label();
        methodVisitor.visitLabel(head);
        writeExpression(whileStmt.guard);
        methodVisitor.visitJumpInsn(IFEQ, afterWhile);
        writeStatements(whileStmt.body);
        methodVisitor.visitJumpInsn(GOTO, head);
        methodVisitor.visitLabel(afterWhile);
    } // whileWhileStatement
    
    public void writeStatements(final List<Stmt> stmts) throws CodeGeneratorException {
        for (final Stmt statement : stmts) {
            writeStatement(statement);
        }
    } // writeStatements
    
    public void writeStatement(final Stmt stmt) throws CodeGeneratorException {
        if (stmt instanceof VariableDeclarationStmt) {
            final VariableDeclarationStmt asDec = (VariableDeclarationStmt)stmt;
            final VariableEntry entry = addEntry(asDec.variable, asDec.type);
            writeExpression(asDec.exp);
            entry.store(methodVisitor);
        } else if (stmt instanceof AssignStmt) {
            final AssignStmt asAssign = (AssignStmt)stmt;
            final VariableEntry entry = getEntryFor(asAssign.variable);
            writeExpression(asAssign.exp);
            entry.store(methodVisitor);
        } else if (stmt instanceof PrintStmt) {
            writePrint(((PrintStmt)stmt).variable);
        } else if (stmt instanceof IfStmt) {
            writeIfStatement((IfStmt)stmt);
        } else if (stmt instanceof WhileStmt) {
            writeWhileStatement((WhileStmt)stmt);
        } else {
            assert(false);
            throw new CodeGeneratorException("Unrecognized statement: " + stmt);
        }
    } // writeStatement

    private void writeReturnFor(final Type type) {
        assert(type instanceof IntType ||
               type instanceof BoolType);
        methodVisitor.visitInsn(IRETURN);
    } // writeReturnFor
    
    private void writeFunction(final Function function) throws CodeGeneratorException {
        functionStart(function);
        writeStatements(function.body);
        writeExpression(function.returned);
        writeReturnFor(function.returnType);
        functionEnd();
    } // writeFunction
    
    private void writeEntryPoint(final List<Stmt> entryPoint) throws CodeGeneratorException {
        functionStart(new Function(null,
                                   outputMethodName,
                                   new ArrayList<FormalParam>(),
                                   null,
                                   null),
                      emptyVoid);
        writeStatements(entryPoint);
        methodVisitor.visitInsn(RETURN);
        functionEnd();
    } // writeEntryPoint
    
    private void loadFunctionTable(final List<Function> functions) throws CodeGeneratorException {
        for (final Function function : functions) {
            if (functionTable.containsKey(function.name)) {
                throw new CodeGeneratorException("Duplicate function name: " + function.name);
            }
            functionTable.put(function.name, function);
        }
    } // loadFunctionTable
    
    public void writeProgram(final Program program) throws CodeGeneratorException, IOException {
        loadFunctionTable(program.functions);
        for (final Function function : program.functions) {
            writeFunction(function);
        }
        writeEntryPoint(program.entryPoint);
        classWriter.visitEnd();
        
        final BufferedOutputStream output =
            new BufferedOutputStream(new FileOutputStream(new File(outputClassName + ".class")));
        output.write(classWriter.toByteArray());
        output.close();
    } // writeProgram
} // CodeGenerator
*/