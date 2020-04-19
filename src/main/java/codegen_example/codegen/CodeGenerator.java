package codegen_example.codegen;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

import codegen_example.syntax.*;

// public class outputClassName {
//   public outputClassName() { super(); }
//   public static void outputMethodName() { PROGRAM }
// }

public class CodeGenerator {
    // ---BEGIN INSTANCE VARIABLES---
    public final String outputClassName;
    public final String outputMethodName;

    private final ClassWriter writer;
    private final Map<Variable, VariableEntry> variables;
    private int nextIndex;
    private final MethodVisitor methodVisitor;
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

        methodVisitor = writer.visitMethod(ACC_PUBLIC | ACC_STATIC,
                                           outputMethodName,
                                           "()V",
                                           null,
                                           null);
        methodVisitor.visitCode();
    } // CodeGenerator

    private VariableEntry getEntryFor(final Variable variable) throws CodeGeneratorException {
        final VariableEntry entry = variables.get(variable);
        if (entry != null) {
            return entry;
        } else {
            throw new CodeGeneratorException("no such variable declared: " + variable);
        }
    } // getEntryFor

    private VariableEntry addEntry(final Variable variable, final Type type) throws CodeGeneratorException {
        if (variables.containsKey(variable)) {
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

    private void writeOp(final BOP bop) throws CodeGeneratorException {
        if (bop instanceof PlusBOP) {
            methodVisitor.visitInsn(IADD);
        } else if (bop instanceof MinusBOP) {
            methodVisitor.visitInsn(ISUB);
        } else if (bop instanceof DivBOP) {
            methodVisitor.visitInsn(IDIV);
        } else if (bop instanceof MultBOP) {
            methodVisitor.visitInsn(IMUL);
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
        } else {
            assert(false);
            throw new CodeGeneratorException("Unrecognized statement: " + stmt);
        }
    } // writeStatement
    
    public void writeProgram(final Program program) throws CodeGeneratorException, IOException {
        for (final Stmt statement : program.statements) {
            writeStatement(statement);
        }
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        writer.visitEnd();
        
        final BufferedOutputStream output =
            new BufferedOutputStream(new FileOutputStream(new File(outputClassName + ".class")));
        output.write(writer.toByteArray());
        output.close();
    } // writeProgram
} // CodeGenerator
