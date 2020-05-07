package codegen_example.codegen;

import java.util.Map;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

import codegen_example.syntax.*;

public class ExpressionStatementGenerator {
    // ---BEGIN INSTANCE VARIABLES---
    private final Map<ClassName, ClassDefinition> allClasses;
    private final LambdaMaker lambdaMaker;
    private final VariableTable variables;
    private final MethodVisitor methodVisitor;
    // ---END INSTANCE VARIABLES---

    public ExpressionStatementGenerator(final Map<ClassName, ClassDefinition> allClasses,
                                        final LambdaMaker lambdaMaker,
                                        final VariableTable variables,
                                        final MethodVisitor methodVisitor) {
        this.allClasses = allClasses;
        this.lambdaMaker = lambdaMaker;
        this.variables = variables;
        this.methodVisitor = methodVisitor;
    }

    public static String printlnDescriptorString(final Type forType) {
        final String inner;
        if (forType instanceof ReferenceType) {
            inner = new ReferenceType(new ClassName(ClassGenerator.objectName)).toDescriptorString();
        } else {
            inner = forType.toDescriptorString();
        }
        return "(" + inner + ")V";
    } // printlnDescriptorString

    public ClassDefinition classDefFor(final ClassName name) throws CodeGeneratorException {
        final ClassDefinition classDef = allClasses.get(name);
        if (classDef == null) {
            throw new CodeGeneratorException("No such class: " + name);
        } 
        return classDef;
    } // classDefFor

    public String constructorDescriptorFor(final ClassName name) throws CodeGeneratorException {
        if (name.name.equals(ClassGenerator.objectName)) {
            return "()V";
        } else if (name.name.startsWith(LambdaMaker.LAMBDA_PREFIX)) {
            return lambdaMaker.constructorDescriptorFor(name);
        } else {
            return classDefFor(name).constructor.toDescriptorString();
        }
    } // constructorDescriptorFor

    public String methodDescriptorFor(final ClassName className,
                                      final MethodName methodName) throws CodeGeneratorException {
        if (className.name.equals(ClassGenerator.objectName)) {
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

    public String fieldDescriptorFor(final ClassName className,
                                     final Variable fieldName) throws CodeGeneratorException {
        if (className.name.equals(ClassGenerator.objectName)) {
            throw new CodeGeneratorException("Nonexistant field: " + fieldName.name);
        } else if (className.name.startsWith(LambdaMaker.LAMBDA_PREFIX)) {
            return lambdaMaker.fieldDescriptorFor(className, fieldName);
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

    public void loadVariable(final Variable variable) throws CodeGeneratorException {
        variables.getEntryFor(variable).load(methodVisitor);
    } // loadVariable

    public void storeVariable(final Variable variable) throws CodeGeneratorException {
        variables.getEntryFor(variable).store(methodVisitor);
    } // storeVariable

    public void doReturn(final Type returnType) throws CodeGeneratorException {
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

    public void writeIntLiteral(final int value) {
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

    public void writeArithmeticComparisonOp(final BOP bop) throws CodeGeneratorException {
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

    public void writeOp(final BOP bop) throws CodeGeneratorException {
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

    public void writeMethodCall(final MethodCallExp call) throws CodeGeneratorException {
        writeExpression(call.callOn);
        writeExpressions(call.actualParams);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
                                      call.callOnName.name,
                                      call.name.name,
                                      methodDescriptorFor(call.callOnName,
                                                          call.name),
                                      false);
    } // writeMethodCall

    public void writeNew(final NewExp newExp) throws CodeGeneratorException {
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

    public void writeGet(final GetExp getExp) throws CodeGeneratorException {
        writeExpression(getExp.target);
        methodVisitor.visitFieldInsn(GETFIELD,
                                     getExp.name.name,
                                     getExp.field.name,
                                     fieldDescriptorFor(getExp.name, getExp.field));
    } // writeGet

    public void writeLambdaExp(final LambdaExp lambdaExp) throws CodeGeneratorException {
        writeExpression(lambdaMaker.translateLambda(lambdaExp, variables));
    } // writeLambdaExp

    public void writeLambdaCallExp(final LambdaCallExp lambdaCallExp) throws CodeGeneratorException {
        writeExpression(lambdaCallExp.lambda);
        writeExpression(lambdaCallExp.param);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE,
                                      LambdaMaker.EXTENDS_NAME.name,
                                      LambdaMaker.APPLY_NAME.name,
                                      LambdaDef.bridgeApplyDescriptorString(),
                                      true);
        methodVisitor.visitTypeInsn(CHECKCAST,
                                    lambdaCallExp.returnType.refersTo.name);
    } // writeLambdaCallExp
    
    public void writeExpressions(final List<Exp> exps) throws CodeGeneratorException {
        for (final Exp exp : exps) {
            writeExpression(exp);
        }
    } // writeExpressions

    public void writeExpression(final Exp exp) throws CodeGeneratorException {
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
        } else if (exp instanceof LambdaExp) {
            writeLambdaExp((LambdaExp)exp);
        } else if (exp instanceof LambdaCallExp) {
            writeLambdaCallExp((LambdaCallExp)exp);
        } else {
            assert(false);
            throw new CodeGeneratorException("Unrecognized expression: " + exp);
        }
    } // writeExpression

    public void writePrint(final Variable variable) throws CodeGeneratorException {
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

    public void writePutStatement(final PutStmt put) throws CodeGeneratorException {
        writeExpression(put.target);
        writeExpression(put.putHere);
        methodVisitor.visitFieldInsn(PUTFIELD,
                                     put.name.name,
                                     put.field.name,
                                     fieldDescriptorFor(put.name, put.field));
    } // writePutStatement

    public void writeStatements(final List<Stmt> stmts) throws CodeGeneratorException {
        for (final Stmt statement : stmts) {
            writeStatement(statement);
        }
    } // writeStatements

    public void writeStatement(final Stmt stmt) throws CodeGeneratorException {
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
} // ExpressionStatementGenerator
