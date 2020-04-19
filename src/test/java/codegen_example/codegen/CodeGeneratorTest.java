package codegen_example.codegen;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.List;
import java.util.ArrayList;

import codegen_example.syntax.*;

public class CodeGeneratorTest {
    // ---BEGIN STATICS---
    public static final String CLASS_NAME = "Compiled";
    public static final String METHOD_NAME = "compiledProgram";
    // ---END STATICS---

    // each element of the array is a separate line
    public static String[] readUntilClose(final InputStream stream) throws IOException {
        return readUntilClose(new BufferedReader(new InputStreamReader(stream)));
    } // readUntilClose
    
    public static String[] readUntilClose(final BufferedReader reader) throws IOException {
        final List<String> buffer = new ArrayList<String>();
        
        try {
            String currentLine = "";
            while ((currentLine = reader.readLine()) != null) {
                buffer.add(currentLine);
            }
            return buffer.toArray(new String[buffer.size()]);
        } finally {
            reader.close();
        }
    } // readUntilClose

    public static String[] runTest(final Program program)
        throws CodeGeneratorException, IOException {
        final CodeGenerator generator = new CodeGenerator(CLASS_NAME, METHOD_NAME);
        generator.writeProgram(program);
        final ProcessBuilder builder = new ProcessBuilder("java", CLASS_NAME);
        builder.redirectErrorStream(true);
        final Process process = builder.start();
        try {
            return readUntilClose(process.getInputStream());
        } finally {
            process.getErrorStream().close();
            process.getOutputStream().close();
            new File(CLASS_NAME + ".class").delete();
        }
    } // runTest

    public static Program makeProgram(final Stmt... statements) {
        final List<Stmt> list = new ArrayList<Stmt>();
        for (final Stmt statement : statements) {
            list.add(statement);
        }
        return new Program(list);
    } // makeProgram

    public static void assertOutput(final Program program,
                                    final String... expectedOutput)
        throws CodeGeneratorException, IOException {
        assertArrayEquals(expectedOutput,
                          runTest(program));
    } // runTest

    public static void testPrintNum(final int value)
        throws CodeGeneratorException, IOException {
        // int x = value;
        // print(x);
        assertOutput(makeProgram(new VariableDeclarationStmt(new IntType(),
                                                             new Variable("x"),
                                                             new IntegerLiteralExp(value)),
                                 new PrintStmt(new Variable("x"))),
                     Integer.toString(value));
    } // testPrintNum
        
    @Test
    public void testPrintMinusOne() throws CodeGeneratorException, IOException {
        testPrintNum(-1);
    }

    @Test
    public void testPrintZero() throws CodeGeneratorException, IOException {
        testPrintNum(0);
    }

    @Test
    public void testPrintOne() throws CodeGeneratorException, IOException {
        testPrintNum(1);
    }

    @Test
    public void testPrintTwo() throws CodeGeneratorException, IOException {
        testPrintNum(2);
    }

    @Test
    public void testPrintThree() throws CodeGeneratorException, IOException {
        testPrintNum(3);
    }

    @Test
    public void testPrintFour() throws CodeGeneratorException, IOException {
        testPrintNum(4);
    }

    @Test
    public void testPrintFive() throws CodeGeneratorException, IOException {
        testPrintNum(5);
    }

    @Test
    public void testPrintSix() throws CodeGeneratorException, IOException {
        testPrintNum(6);
    }

    @Test
    public void testPrintMaxInt() throws CodeGeneratorException, IOException {
        testPrintNum(Integer.MAX_VALUE);
    }

    @Test
    public void testPrintTrue() throws CodeGeneratorException, IOException {
        // boolean x = true;
        // print(x);
        assertOutput(makeProgram(new VariableDeclarationStmt(new BoolType(),
                                                             new Variable("x"),
                                                             new BooleanLiteralExp(true)),
                                 new PrintStmt(new Variable("x"))),
                     "true");
    }

    @Test
    public void testPrintFalse() throws CodeGeneratorException, IOException {
        // boolean x = false;
        // print(x);
        assertOutput(makeProgram(new VariableDeclarationStmt(new BoolType(),
                                                             new Variable("x"),
                                                             new BooleanLiteralExp(false)),
                                 new PrintStmt(new Variable("x"))),
                     "false");
    }

    @Test
    public void testIntAssignment() throws CodeGeneratorException, IOException {
        // int x = 0;
        // x = 1;
        // print(x);
        assertOutput(makeProgram(new VariableDeclarationStmt(new IntType(),
                                                             new Variable("x"),
                                                             new IntegerLiteralExp(0)),
                                 new AssignStmt(new Variable("x"),
                                                new IntegerLiteralExp(1)),
                                 new PrintStmt(new Variable("x"))),
                     "1");
    }

    @Test
    public void testBoolAssignment() throws CodeGeneratorException, IOException {
        // boolean x = true;
        // x = false;
        // print(x);
        assertOutput(makeProgram(new VariableDeclarationStmt(new BoolType(),
                                                             new Variable("x"),
                                                             new BooleanLiteralExp(true)),
                                 new AssignStmt(new Variable("x"),
                                                new BooleanLiteralExp(false)),
                                 new PrintStmt(new Variable("x"))),
                     "false");
    }

    @Test
    public void testAdd() throws CodeGeneratorException, IOException {
        // int x = 1 + 2;
        // print(x);
        final Exp add = new BinopExp(new IntegerLiteralExp(1),
                                     new PlusBOP(),
                                     new IntegerLiteralExp(2));
        assertOutput(makeProgram(new VariableDeclarationStmt(new IntType(),
                                                             new Variable("x"),
                                                             add),
                                 new PrintStmt(new Variable("x"))),
                     "3");
    }

    @Test
    public void testSubtract() throws CodeGeneratorException, IOException {
        // int x = 1 - 2;
        // print(x);
        final Exp sub = new BinopExp(new IntegerLiteralExp(1),
                                     new MinusBOP(),
                                     new IntegerLiteralExp(2));
        assertOutput(makeProgram(new VariableDeclarationStmt(new IntType(),
                                                             new Variable("x"),
                                                             sub),
                                 new PrintStmt(new Variable("x"))),
                     "-1");
    }

    @Test
    public void testDivision() throws CodeGeneratorException, IOException {
        // int x = 10 / 5;
        // print(x);
        final Exp div = new BinopExp(new IntegerLiteralExp(10),
                                     new DivBOP(),
                                     new IntegerLiteralExp(5));
        assertOutput(makeProgram(new VariableDeclarationStmt(new IntType(),
                                                             new Variable("x"),
                                                             div),
                                 new PrintStmt(new Variable("x"))),
                     "2");
    }

    @Test
    public void testMult() throws CodeGeneratorException, IOException {
        // int x = 10 * 5;
        // print(x);
        final Exp mult = new BinopExp(new IntegerLiteralExp(10),
                                      new MultBOP(),
                                      new IntegerLiteralExp(5));
        assertOutput(makeProgram(new VariableDeclarationStmt(new IntType(),
                                                             new Variable("x"),
                                                             mult),
                                 new PrintStmt(new Variable("x"))),
                     "50");
    }
} // CodeGeneratorTest
