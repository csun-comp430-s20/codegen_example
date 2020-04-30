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

    // Runs main in the first provided class
    public String[] runTest(final Program program)
        throws CodeGeneratorException, IOException {
        assert(!program.classDefs.isEmpty());
        
        final ClassGenerator generator = new ClassGenerator(program);
        generator.writeClasses();
        
        final ProcessBuilder builder =
            new ProcessBuilder("java", program.classDefs.get(0).name.name);
        builder.redirectErrorStream(true);
        final Process process = builder.start();
        try {
            return readUntilClose(process.getInputStream());
        } finally {
            process.getErrorStream().close();
            process.getOutputStream().close();
        }
    } // runTest

    public static List<Exp> actualParams(final Exp... params) {
        final List<Exp> list = new ArrayList<Exp>();
        for (final Exp param : params) {
            list.add(param);
        }
        return list;
    } // actualParams
    
    public static List<MethodDefinition> methods(final MethodDefinition... theMethods) {
        final List<MethodDefinition> list = new ArrayList<MethodDefinition>();
        for (final MethodDefinition method : theMethods) {
            list.add(method);
        }
        return list;
    } // methods
    
    public static List<Stmt> stmts(final Stmt... statements) {
        final List<Stmt> list = new ArrayList<Stmt>();
        for (final Stmt statement : statements) {
            list.add(statement);
        }
        return list;
    } // stmts

    public static Program makeProgram(final ClassDefinition... classDefs) {
        final List<ClassDefinition> list = new ArrayList<ClassDefinition>();
        for (final ClassDefinition classDef : classDefs) {
            list.add(classDef);
        }
        return new Program(list);
    } // makeProgram
    
    public void assertOutput(final Program program,
                             final String... expectedOutput)
        throws CodeGeneratorException, IOException {
        assertArrayEquals(expectedOutput,
                          runTest(program));
        for (final ClassDefinition classDef : program.classDefs) {
            new File(classDef.name.name + ".class").delete();
        }
    } // assertOutput

    public void assertOutputInMain(final String className,
                                   final List<Stmt> body,
                                   final String... expectedOutput)
        throws CodeGeneratorException, IOException {
        final Program program =
            makeProgram(new ClassDefinition(new ClassName(className),
                                            new ClassName(ClassGenerator.objectName),
                                            new ArrayList<FormalParam>(),
                                            new Constructor(new ArrayList<FormalParam>(),
                                                            actualParams(),
                                                            stmts()),
                                            new MainDefinition(body),
                                            methods()));
        assertOutput(program, expectedOutput);
    } // assertOutputInMain
    
    public void testPrintNum(final int value) throws CodeGeneratorException, IOException {
        // class TestPrintNumvalue extends java/lang/Object {
        //   init() { super(); }
        //   main {
        //     int x = value;
        //     print(x);
        //   }
        // }
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              new IntegerLiteralExp(value)),
                  new PrintStmt(new Variable("x")));

        assertOutputInMain("TestPrintNum" + value,
                           body,
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

    public void testPrintBool(final boolean value) throws CodeGeneratorException, IOException {
        // class TestPrintBoolvalue extends java/lang/Object {
        //   init() { super(); }
        //   main {
        //     boolean x = value;
        //     print(x);
        //   }
        // }
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new BoolType(),
                                              new Variable("x"),
                                              new BooleanLiteralExp(value)),
                  new PrintStmt(new Variable("x")));
        assertOutputInMain("TestPrintBool" + value,
                           body,
                           Boolean.toString(value));
    } // testPrintBool

    @Test
    public void testPrintTrue() throws CodeGeneratorException, IOException {
        testPrintBool(true);
    }

    @Test
    public void testPrintFalse() throws CodeGeneratorException, IOException {
        testPrintBool(false);
    }

    /*
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

    @Test
    public void testWhileLoopInitiallyFalse() throws CodeGeneratorException, IOException {
        // int x = 10;
        // int y = -1;
        // while (x < 10) {
        //   print(x);
        //   x = x + 1;
        // }
        // print(y);

        final Variable x = new Variable("x");
        final Variable y = new Variable("y");
        final Program program =
            makeProgram(new VariableDeclarationStmt(new IntType(),
                                                    x,
                                                    new IntegerLiteralExp(10)),
                        new VariableDeclarationStmt(new IntType(),
                                                    y,
                                                    new IntegerLiteralExp(-1)),
                        new WhileStmt(new BinopExp(new VariableExp(x),
                                                   new LessThanBOP(),
                                                   new IntegerLiteralExp(10)),
                                      stmts(new PrintStmt(x),
                                            new AssignStmt(x,
                                                           new BinopExp(new VariableExp(x),
                                                                        new PlusBOP(),
                                                                        new IntegerLiteralExp(1))))),
                        new PrintStmt(y));
        assertOutput(program,
                     "-1");
    }

    @Test
    public void testWhileLoopInitiallyTrue() throws CodeGeneratorException, IOException {
        // int x = 0;
        // int y = -1;
        // while (x < 10) {
        //   print(x);
        //   x = x + 1;
        // }
        // print(y);

        final Variable x = new Variable("x");
        final Variable y = new Variable("y");
        final Program program =
            makeProgram(new VariableDeclarationStmt(new IntType(),
                                                    x,
                                                    new IntegerLiteralExp(0)),
                        new VariableDeclarationStmt(new IntType(),
                                                    y,
                                                    new IntegerLiteralExp(-1)),
                        new WhileStmt(new BinopExp(new VariableExp(x),
                                                   new LessThanBOP(),
                                                   new IntegerLiteralExp(10)),
                                      stmts(new PrintStmt(x),
                                            new AssignStmt(x,
                                                           new BinopExp(new VariableExp(x),
                                                                        new PlusBOP(),
                                                                        new IntegerLiteralExp(1))))),
                        new PrintStmt(y));
        assertOutput(program,
                     "0",
                     "1",
                     "2",
                     "3",
                     "4",
                     "5",
                     "6",
                     "7",
                     "8",
                     "9",
                     "-1");
    }

    @Test
    public void testFunctionReturningIntNoParams() throws CodeGeneratorException, IOException {
        // int foo() { return 1; }
        // int x = foo();
        // print(x);

        final FunctionName fname = new FunctionName("foo");
        final Function foo =
            new Function(new IntType(),
                         fname,
                         new ArrayList<FormalParam>(),
                         stmts(),
                         new IntegerLiteralExp(1));
        final Variable x = new Variable("x");
        final Program program =
            makeProgramWithFunctions(functions(foo),
                                     new VariableDeclarationStmt(new IntType(),
                                                                 x,
                                                                 new FunctionCallExp(fname,
                                                                                     new ArrayList<Exp>())),
                                     new PrintStmt(x));
        assertOutput(program, "1");
    }

    @Test
    public void testFunctionReturningBoolNoParams() throws CodeGeneratorException, IOException {
        // bool foo() { return true; }
        // bool x = foo();
        // print(x);

        final FunctionName fname = new FunctionName("foo");
        final Function foo =
            new Function(new BoolType(),
                         fname,
                         new ArrayList<FormalParam>(),
                         stmts(),
                         new BooleanLiteralExp(true));
        final Variable x = new Variable("x");
        final Program program =
            makeProgramWithFunctions(functions(foo),
                                     new VariableDeclarationStmt(new BoolType(),
                                                                 x,
                                                                 new FunctionCallExp(fname,
                                                                                     new ArrayList<Exp>())),
                                     new PrintStmt(x));
        assertOutput(program, "true");
    }

    @Test
    public void testIntFunctionTakingParams() throws CodeGeneratorException, IOException {
        // int add(int x, int y) {
        //   print(x);
        //   print(y);
        //   return x + y;
        // }
        // int x = add(1, 2);
        // print(x);

        final Variable x = new Variable("x");
        final Variable y = new Variable("y");
        final FunctionName fname = new FunctionName("add");
        final List<FormalParam> formalParams = new ArrayList<FormalParam>();
        formalParams.add(new FormalParam(new IntType(), new Variable("x")));
        formalParams.add(new FormalParam(new IntType(), new Variable("y")));
        
        final Function add =
            new Function(new IntType(),
                         fname,
                         formalParams,
                         stmts(new PrintStmt(x),
                               new PrintStmt(y)),
                         new BinopExp(new VariableExp(x),
                                      new PlusBOP(),
                                      new VariableExp(y)));
        final List<Exp> params = actualParams(new IntegerLiteralExp(1),
                                              new IntegerLiteralExp(2));
        
        final List<Stmt> entryPoint =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              x,
                                              new FunctionCallExp(fname, params)),
                  new PrintStmt(x));
        final Program program = new Program(functions(add), entryPoint);

        assertOutput(program,
                     "1",
                     "2",
                     "3");
    }

    @Test
    public void testMutualRecursion() throws CodeGeneratorException, IOException {
        // bool isEven(int x) {
        //   bool result = false;
        //   if (x == 0) {
        //     result = true;
        //   } else {
        //     result = isOdd(x - 1);
        //   }
        //   return result;
        // }
        //
        // bool isOdd(int x) {
        //   bool result = false;
        //   if (x == 0) {
        //     result = false;
        //   } else {
        //     result = isEven(x - 1);
        //   }
        //   return result;
        // }
        //
        // bool evenYes = isEven(6);
        // bool evenNo = isEven(7);
        // bool oddYes = isOdd(9);
        // bool oddNo = isOdd(10);
        // print(evenYes);
        // print(evenNo);
        // print(oddYes);
        // print(oddNo);
        
        final List<FormalParam> formalParams = new ArrayList<FormalParam>();
        formalParams.add(new FormalParam(new IntType(), new Variable("x")));
        
        final Variable result = new Variable("result");
        final Function isEven =
            new Function(new BoolType(),
                         new FunctionName("isEven"),
                         formalParams,
                         stmts(new VariableDeclarationStmt(new BoolType(),
                                                           result,
                                                           new BooleanLiteralExp(false)),
                               new IfStmt(new BinopExp(new VariableExp(new Variable("x")),
                                                       new EqualsBOP(),
                                                       new IntegerLiteralExp(0)),
                                          stmts(new AssignStmt(result,
                                                               new BooleanLiteralExp(true))),
                                          stmts(new AssignStmt(result,
                                                               new FunctionCallExp(new FunctionName("isOdd"),
                                                                                   actualParams(new BinopExp(new VariableExp(new Variable("x")),
                                                                                                             new MinusBOP(),
                                                                                                             new IntegerLiteralExp(1)))))))),
                         new VariableExp(result));
        final Function isOdd =
            new Function(new BoolType(),
                         new FunctionName("isOdd"),
                         formalParams,
                         stmts(new VariableDeclarationStmt(new BoolType(),
                                                           result,
                                                           new BooleanLiteralExp(false)),
                               new IfStmt(new BinopExp(new VariableExp(new Variable("x")),
                                                       new EqualsBOP(),
                                                       new IntegerLiteralExp(0)),
                                          stmts(new AssignStmt(result,
                                                               new BooleanLiteralExp(false))),
                                          stmts(new AssignStmt(result,
                                                               new FunctionCallExp(new FunctionName("isEven"),
                                                                                   actualParams(new BinopExp(new VariableExp(new Variable("x")),
                                                                                                             new MinusBOP(),
                                                                                                             new IntegerLiteralExp(1)))))))),
                         new VariableExp(result));

        final Variable evenYes = new Variable("evenYes");
        final Variable evenNo = new Variable("evenNo");
        final Variable oddYes = new Variable("oddYes");
        final Variable oddNo = new Variable("oddNo");
        
        final Program program =
            makeProgramWithFunctions(functions(isEven, isOdd),
                                     new VariableDeclarationStmt(new BoolType(),
                                                                 evenYes,
                                                                 new FunctionCallExp(new FunctionName("isEven"),
                                                                                     actualParams(new IntegerLiteralExp(6)))),
                                     new VariableDeclarationStmt(new BoolType(),
                                                                 evenNo,
                                                                 new FunctionCallExp(new FunctionName("isEven"),
                                                                                     actualParams(new IntegerLiteralExp(7)))),
                                     new VariableDeclarationStmt(new BoolType(),
                                                                 oddYes,
                                                                 new FunctionCallExp(new FunctionName("isOdd"),
                                                                                     actualParams(new IntegerLiteralExp(9)))),
                                     new VariableDeclarationStmt(new BoolType(),
                                                                 oddNo,
                                                                 new FunctionCallExp(new FunctionName("isOdd"),
                                                                                     actualParams(new IntegerLiteralExp(10)))),
                                     new PrintStmt(evenYes),
                                     new PrintStmt(evenNo),
                                     new PrintStmt(oddYes),
                                     new PrintStmt(oddNo));
        assertOutput(program,
                     "true",
                     "false",
                     "true",
                     "false");
    }
    */
} // CodeGeneratorTest
