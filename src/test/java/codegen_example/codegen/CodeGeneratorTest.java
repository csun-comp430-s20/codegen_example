package codegen_example.codegen;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

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
    public static final String CLASS_NAME_PREFIX = "Compiled";
    public static final String METHOD_NAME = "compiledProgram";
    // ---END STATICS---

    @Rule
    public TestName testName = new TestName();
    private String currentClassName = null;

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

    public String[] runTest(final Program program)
        throws CodeGeneratorException, IOException {
        currentClassName = CLASS_NAME_PREFIX + testName.getMethodName();
        final CodeGenerator generator = new CodeGenerator(currentClassName, METHOD_NAME);
        generator.writeProgram(program);
        final ProcessBuilder builder = new ProcessBuilder("java", currentClassName);
        builder.redirectErrorStream(true);
        final Process process = builder.start();
        try {
            return readUntilClose(process.getInputStream());
        } finally {
            process.getErrorStream().close();
            process.getOutputStream().close();
        }
    } // runTest

    public static List<Stmt> stmts(final Stmt... statements) {
        final List<Stmt> list = new ArrayList<Stmt>();
        for (final Stmt statement : statements) {
            list.add(statement);
        }
        return list;
    } // stmts
    
    public static Program makeProgram(final Stmt... statements) {
        return new Program(stmts(statements));
    } // makeProgram

    public void assertOutput(final Program program,
                             final String... expectedOutput)
        throws CodeGeneratorException, IOException {
        assertArrayEquals(expectedOutput,
                          runTest(program));
        new File(currentClassName + ".class").delete();
    } // runTest

    public void testPrintNum(final int value) throws CodeGeneratorException, IOException {
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

    @Test
    public void testLessThanFalse() throws CodeGeneratorException, IOException {
        // bool b = 10 < 5;
        // print(b);
        final Exp lt = new BinopExp(new IntegerLiteralExp(10),
                                    new LessThanBOP(),
                                    new IntegerLiteralExp(5));
        assertOutput(makeProgram(new VariableDeclarationStmt(new BoolType(),
                                                             new Variable("b"),
                                                             lt),
                                 new PrintStmt(new Variable("b"))),
                     "false");
    }

    // key point: this test does NOT improve coverage, but it did catch a bug -
    // I was using the wrong bytecode instruction, but the issue was only apparent
    @Test
    public void testLessThanTrue() throws CodeGeneratorException, IOException {
        // bool b = 5 < 10;
        // print(b);
        final Exp lt = new BinopExp(new IntegerLiteralExp(5),
                                    new LessThanBOP(),
                                    new IntegerLiteralExp(10));
        assertOutput(makeProgram(new VariableDeclarationStmt(new BoolType(),
                                                             new Variable("b"),
                                                             lt),
                                 new PrintStmt(new Variable("b"))),
                     "true");
    }

    @Test
    public void testEqualsFalse() throws CodeGeneratorException, IOException {
        // bool b = 10 == 5;
        // print(b);
        final Exp exp = new BinopExp(new IntegerLiteralExp(10),
                                     new EqualsBOP(),
                                     new IntegerLiteralExp(5));
        assertOutput(makeProgram(new VariableDeclarationStmt(new BoolType(),
                                                             new Variable("b"),
                                                             exp),
                                 new PrintStmt(new Variable("b"))),
                     "false");
    }

    @Test
    public void testEqualsTrue() throws CodeGeneratorException, IOException {
        // bool b = 10 == 10;
        // print(b);
        final Exp exp = new BinopExp(new IntegerLiteralExp(10),
                                     new EqualsBOP(),
                                     new IntegerLiteralExp(10));
        assertOutput(makeProgram(new VariableDeclarationStmt(new BoolType(),
                                                             new Variable("b"),
                                                             exp),
                                 new PrintStmt(new Variable("b"))),
                     "true");
    }

    @Test
    public void testMultipleVars() throws CodeGeneratorException, IOException {
        // int x = 2 + 3;
        // int y = x + x;
        // print(y);
        final Exp firstAdd = new BinopExp(new IntegerLiteralExp(2),
                                          new PlusBOP(),
                                          new IntegerLiteralExp(3));
        final Exp secondAdd = new BinopExp(new VariableExp(new Variable("x")),
                                           new PlusBOP(),
                                           new VariableExp(new Variable("x")));
        assertOutput(makeProgram(new VariableDeclarationStmt(new IntType(),
                                                             new Variable("x"),
                                                             firstAdd),
                                 new VariableDeclarationStmt(new IntType(),
                                                             new Variable("y"),
                                                             secondAdd),
                                 new PrintStmt(new Variable("x")),
                                 new PrintStmt(new Variable("y"))),
                     "5",
                     "10");
    }

    @Test
    public void testIfTrueTrivialCondition() throws CodeGeneratorException, IOException {
        // int x = 0;
        // int y = 1;
        // if (true) {
        //   print(x);
        // } else {
        //   print(y);
        // }
        assertOutput(makeProgram(new VariableDeclarationStmt(new IntType(),
                                                             new Variable("x"),
                                                             new IntegerLiteralExp(0)),
                                 new VariableDeclarationStmt(new IntType(),
                                                             new Variable("y"),
                                                             new IntegerLiteralExp(1)),
                                 new IfStmt(new BooleanLiteralExp(true),
                                            stmts(new PrintStmt(new Variable("x"))),
                                            stmts(new PrintStmt(new Variable("y"))))),
                     "0");
    }

    @Test
    public void testIfFalseTrivialCondition() throws CodeGeneratorException, IOException {
        // int x = 0;
        // int y = 1;
        // if (false) {
        //   print(x);
        // } else {
        //   print(y);
        // }
        assertOutput(makeProgram(new VariableDeclarationStmt(new IntType(),
                                                             new Variable("x"),
                                                             new IntegerLiteralExp(0)),
                                 new VariableDeclarationStmt(new IntType(),
                                                             new Variable("y"),
                                                             new IntegerLiteralExp(1)),
                                 new IfStmt(new BooleanLiteralExp(false),
                                            stmts(new PrintStmt(new Variable("x"))),
                                            stmts(new PrintStmt(new Variable("y"))))),
                     "1");
    }

    @Test
    public void testEmptyTrueBranch() throws CodeGeneratorException, IOException {
        // int x = 0;
        // if (true) {
        // } else {
        //   print(x);
        // }
        assertOutput(makeProgram(new VariableDeclarationStmt(new IntType(),
                                                             new Variable("x"),
                                                             new IntegerLiteralExp(0)),
                                 new IfStmt(new BooleanLiteralExp(true),
                                            stmts(),
                                            stmts(new PrintStmt(new Variable("x"))))));
    }

    @Test
    public void testEmptyFalseBranch() throws CodeGeneratorException, IOException {
        // int x = 0;
        // if (false) {
        //   print(x);
        // } else {
        // }
        assertOutput(makeProgram(new VariableDeclarationStmt(new IntType(),
                                                             new Variable("x"),
                                                             new IntegerLiteralExp(0)),
                                 new IfStmt(new BooleanLiteralExp(false),
                                            stmts(new PrintStmt(new Variable("x"))),
                                            stmts())));
    }

    @Test
    public void testIfComplexConditionTrue() throws CodeGeneratorException, IOException {
        // int x = 0;
        // int y = 1;
        // if (x < y) {
        //   print(x);
        // } else {
        //   print(y);
        // }
        final Exp guard = new BinopExp(new VariableExp(new Variable("x")),
                                       new LessThanBOP(),
                                       new VariableExp(new Variable("y")));
        assertOutput(makeProgram(new VariableDeclarationStmt(new IntType(),
                                                             new Variable("x"),
                                                             new IntegerLiteralExp(0)),
                                 new VariableDeclarationStmt(new IntType(),
                                                             new Variable("y"),
                                                             new IntegerLiteralExp(1)),
                                 new IfStmt(guard,
                                            stmts(new PrintStmt(new Variable("x"))),
                                            stmts(new PrintStmt(new Variable("y"))))),
                     "0");
    }
    
    @Test
    public void testIfComplexConditionFalse() throws CodeGeneratorException, IOException {
        // int x = 0;
        // int y = 1;
        // if (y < x) {
        //   print(x);
        // } else {
        //   print(y);
        // }
        final Exp guard = new BinopExp(new VariableExp(new Variable("y")),
                                       new LessThanBOP(),
                                       new VariableExp(new Variable("x")));
        assertOutput(makeProgram(new VariableDeclarationStmt(new IntType(),
                                                             new Variable("x"),
                                                             new IntegerLiteralExp(0)),
                                 new VariableDeclarationStmt(new IntType(),
                                                             new Variable("y"),
                                                             new IntegerLiteralExp(1)),
                                 new IfStmt(guard,
                                            stmts(new PrintStmt(new Variable("x"))),
                                            stmts(new PrintStmt(new Variable("y"))))),
                     "1");
    }

    @Test
    public void testNestedVariableDeclaration() throws CodeGeneratorException, IOException {
        // if (true) {
        //   int x = 0;
        //   print(x);
        // } else {
        //   int y = 1;
        //   print(y);
        // }
        final List<Stmt> trueBranch =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              new IntegerLiteralExp(0)),
                  new PrintStmt(new Variable("x")));
        final List<Stmt> falseBranch =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("y"),
                                              new IntegerLiteralExp(1)),
                  new PrintStmt(new Variable("y")));
        assertOutput(makeProgram(new IfStmt(new BooleanLiteralExp(true),
                                            trueBranch,
                                            falseBranch)),
                     "0");
    }
} // CodeGeneratorTest
