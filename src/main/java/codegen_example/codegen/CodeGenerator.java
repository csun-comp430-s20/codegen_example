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
public class CodeGenerator {
    // ---BEGIN INSTANCE VARIABLES---
    public final String outputClassName;
    public final String outputMethodName;

    private final ClassWriter writer;
    private final Map<Variable, VariableEntry> variables;
    private int nextIndex;
    private MethodVisitor methodVisitor;
    // ---END INSTANCE VARIABLES
    
    public CodeGenerator(final String outputClassName,
                         final String outputMethodName) {
        this.outputClassName = outputClassName;
        this.outputMethodName = outputMethodName;
        writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        variables = new HashMap<Variable, VariableEntry>();
        nextIndex = 0;
        
        writer.visit(V1_7, // Java 1.7
                     ACC_PUBLIC, // public
                     outputClassName, // class name
                     null, // signature (null means not generic)
                     "java/lang/Object", // superclass
                     new String[0]); // interfaces (none)

        // ---BEGIN CONSTRUCTOR DEFINITION---
        final MethodVisitor constructor =
            writer.visitMethod(ACC_PUBLIC, // access modifier
                               "<init>", // method name (constructor)
                               "()V", // descriptor (no params, returns void)
                               null, // signature (null means not generic)
                               null); // exceptions
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0); // load "this"
        constructor.visitMethodInsn(INVOKESPECIAL,
                                    "java/lang/Object",
                                    "<init>",
                                    "()V",
                                    false); // super()
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(0, 0);
        // ---END CONSTRUCTOR DEFINITION---

        // ---BEGIN MAIN DEFINITION---
        final MethodVisitor main =
            writer.visitMethod(ACC_PUBLIC | ACC_STATIC,
                               "main",
                               "([Ljava/lang/String;)V",
                               null,
                               null);
        main.visitCode();
        main.visitMethodInsn(INVOKESTATIC,
                             outputClassName,
                             outputMethodName,
                             "()V",
                             false);
        main.visitInsn(RETURN);
        main.visitMaxs(0, 0);
        // ---END MAIN DEFINITION---

        methodVisitor = null;
    } // CodeGenerator

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
            // writer.newConst(Integer.valueOf(value))
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

    private void writeEntryPoint(final List<Stmt> entryPoint) throws CodeGeneratorException, IOException {
        assert(methodVisitor == null);
        methodVisitor = writer.visitMethod(ACC_PUBLIC | ACC_STATIC,
                                           outputMethodName,
                                           "()V",
                                           null,
                                           null);
        methodVisitor.visitCode();
        writeStatements(entryPoint);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        writer.visitEnd();
        methodVisitor = null;
    } // writeEntryPoint
    
    public void writeProgram(final Program program) throws CodeGeneratorException, IOException {
        writeEntryPoint(program.entryPoint);
        
        final BufferedOutputStream output =
            new BufferedOutputStream(new FileOutputStream(new File(outputClassName + ".class")));
        output.write(writer.toByteArray());
        output.close();
    } // writeProgram
} // CodeGenerator
