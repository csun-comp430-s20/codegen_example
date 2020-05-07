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
    // ---BEGIN CONSTANTS---
    public static final String WORK_DIRECTORY = "test_workspace";
    // ---END CONSTANTS---
    
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
    public TestResult runTest(final Program program)
        throws CodeGeneratorException, IOException {
        assert(!program.classDefs.isEmpty());
        
        final ClassGenerator generator = new ClassGenerator(program);
        generator.writeClasses(WORK_DIRECTORY);
        
        final ProcessBuilder builder =
            new ProcessBuilder("java", program.classDefs.get(0).name.name)
            .directory(new File(WORK_DIRECTORY));
        builder.redirectErrorStream(true);
        final Process process = builder.start();
        try {
            return new TestResult(readUntilClose(process.getInputStream()),
                                  generator);
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
        final TestResult testResult = runTest(program);
        assertArrayEquals(expectedOutput, testResult.output);
        testResult.generator.deleteClasses(WORK_DIRECTORY);
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
    } // testPrintMinusOne

    @Test
    public void testPrintZero() throws CodeGeneratorException, IOException {
        testPrintNum(0);
    } // testPrintZero

    @Test
    public void testPrintOne() throws CodeGeneratorException, IOException {
        testPrintNum(1);
    } // testPrintOne

    @Test
    public void testPrintTwo() throws CodeGeneratorException, IOException {
        testPrintNum(2);
    } // testPrintTwo

    @Test
    public void testPrintThree() throws CodeGeneratorException, IOException {
        testPrintNum(3);
    } // testPrintThree

    @Test
    public void testPrintFour() throws CodeGeneratorException, IOException {
        testPrintNum(4);
    } // testPrintFour

    @Test
    public void testPrintFive() throws CodeGeneratorException, IOException {
        testPrintNum(5);
    } // testPrintFive

    @Test
    public void testPrintSix() throws CodeGeneratorException, IOException {
        testPrintNum(6);
    } // testPrintSix

    @Test
    public void testPrintMaxInt() throws CodeGeneratorException, IOException {
        testPrintNum(Integer.MAX_VALUE);
    } // testPrintMaxInt

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
    } // testPrintTrue

    @Test
    public void testPrintFalse() throws CodeGeneratorException, IOException {
        testPrintBool(false);
    } // testPrintFalse

    @Test
    public void testIntAssignment() throws CodeGeneratorException, IOException {
        // int x = 0;
        // x = 1;
        // print(x);
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              new IntegerLiteralExp(0)),
                  new AssignStmt(new Variable("x"),
                                 new IntegerLiteralExp(1)),
                  new PrintStmt(new Variable("x")));
        assertOutputInMain("TestIntAssignment",
                           body,
                           "1");
    } // testIntAssignment

    @Test
    public void testBoolAssignment() throws CodeGeneratorException, IOException {
        // boolean x = true;
        // x = false;
        // print(x);
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new BoolType(),
                                              new Variable("x"),
                                              new BooleanLiteralExp(true)),
                  new AssignStmt(new Variable("x"),
                                 new BooleanLiteralExp(false)),
                  new PrintStmt(new Variable("x")));
        assertOutputInMain("TestBoolAssignment",
                           body,
                           "false");
    } // testBoolAssignment

    @Test
    public void testAdd() throws CodeGeneratorException, IOException {
        // int x = 1 + 2;
        // print(x);
        final Exp add = new BinopExp(new IntegerLiteralExp(1),
                                     new PlusBOP(),
                                     new IntegerLiteralExp(2));
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              add),
                  new PrintStmt(new Variable("x")));
        assertOutputInMain("TestAdd",
                           body,
                           "3");
    } // testAdd

    @Test
    public void testSubtract() throws CodeGeneratorException, IOException {
        // int x = 1 - 2;
        // print(x);
        final Exp sub = new BinopExp(new IntegerLiteralExp(1),
                                     new MinusBOP(),
                                     new IntegerLiteralExp(2));
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              sub),
                  new PrintStmt(new Variable("x")));
        assertOutputInMain("TestSubtract",
                           body,
                           "-1");
    } // testSubtract

    @Test
    public void testDivision() throws CodeGeneratorException, IOException {
        // int x = 10 / 5;
        // print(x);
        final Exp div = new BinopExp(new IntegerLiteralExp(10),
                                     new DivBOP(),
                                     new IntegerLiteralExp(5));
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              div),
                  new PrintStmt(new Variable("x")));
        assertOutputInMain("TestDivision",
                           body,
                           "2");
    } // testDivision

    @Test
    public void testMult() throws CodeGeneratorException, IOException {
        // int x = 10 * 5;
        // print(x);
        final Exp mult = new BinopExp(new IntegerLiteralExp(10),
                                      new MultBOP(),
                                      new IntegerLiteralExp(5));
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              mult),
                  new PrintStmt(new Variable("x")));
        assertOutputInMain("TestMult",
                           body,
                           "50");
    } // testMult

    @Test
    public void testLessThanFalse() throws CodeGeneratorException, IOException {
        // bool b = 10 < 5;
        // print(b);
        final Exp lt = new BinopExp(new IntegerLiteralExp(10),
                                    new LessThanBOP(),
                                    new IntegerLiteralExp(5));
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new BoolType(),
                                              new Variable("b"),
                                              lt),
                  new PrintStmt(new Variable("b")));
        assertOutputInMain("TestLessThanFalse",
                           body,
                           "false");
    } // testLessThanFalse

    // key point: this test does NOT improve coverage, but it did catch a bug -
    // I was using the wrong bytecode instruction, but the issue was only apparent
    @Test
    public void testLessThanTrue() throws CodeGeneratorException, IOException {
        // bool b = 5 < 10;
        // print(b);
        final Exp lt = new BinopExp(new IntegerLiteralExp(5),
                                    new LessThanBOP(),
                                    new IntegerLiteralExp(10));
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new BoolType(),
                                              new Variable("b"),
                                              lt),
                  new PrintStmt(new Variable("b")));
        assertOutputInMain("TestLessThanTrue",
                           body,
                           "true");
    } // testLessThanTrue

    @Test
    public void testEqualsFalse() throws CodeGeneratorException, IOException {
        // bool b = 10 == 5;
        // print(b);
        final Exp exp = new BinopExp(new IntegerLiteralExp(10),
                                     new EqualsBOP(),
                                     new IntegerLiteralExp(5));
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new BoolType(),
                                              new Variable("b"),
                                              exp),
                  new PrintStmt(new Variable("b")));
        assertOutputInMain("TestEqualsFalse",
                           body,
                           "false");
    } // testEqualsFalse

    @Test
    public void testEqualsTrue() throws CodeGeneratorException, IOException {
        // bool b = 10 == 10;
        // print(b);
        final Exp exp = new BinopExp(new IntegerLiteralExp(10),
                                     new EqualsBOP(),
                                     new IntegerLiteralExp(10));
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new BoolType(),
                                              new Variable("b"),
                                              exp),
                  new PrintStmt(new Variable("b")));
        assertOutputInMain("TestEqualsTrue",
                           body,
                           "true");
    } // testEqualsTrue

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
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              firstAdd),
                  new VariableDeclarationStmt(new IntType(),
                                              new Variable("y"),
                                              secondAdd),
                  new PrintStmt(new Variable("x")),
                  new PrintStmt(new Variable("y")));
        assertOutputInMain("TestMultipleVars",
                           body,
                           "5",
                           "10");
    } // testMultipleVars

    @Test
    public void testIfTrueTrivialCondition() throws CodeGeneratorException, IOException {
        // int x = 0;
        // int y = 1;
        // if (true) {
        //   print(x);
        // } else {
        //   print(y);
        // }

        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              new IntegerLiteralExp(0)),
                  new VariableDeclarationStmt(new IntType(),
                                              new Variable("y"),
                                              new IntegerLiteralExp(1)),
                  new IfStmt(new BooleanLiteralExp(true),
                             stmts(new PrintStmt(new Variable("x"))),
                             stmts(new PrintStmt(new Variable("y")))));
        assertOutputInMain("TestIfTrueTrivialCondition",
                           body,
                           "0");
    } // testIfTrueTrivialCondition

    @Test
    public void testIfFalseTrivialCondition() throws CodeGeneratorException, IOException {
        // int x = 0;
        // int y = 1;
        // if (false) {
        //   print(x);
        // } else {
        //   print(y);
        // }

        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              new IntegerLiteralExp(0)),
                  new VariableDeclarationStmt(new IntType(),
                                              new Variable("y"),
                                              new IntegerLiteralExp(1)),
                  new IfStmt(new BooleanLiteralExp(false),
                             stmts(new PrintStmt(new Variable("x"))),
                             stmts(new PrintStmt(new Variable("y")))));
        assertOutputInMain("TestIfFalseTrivialCondition",
                           body,
                           "1");
    } // testIfFalseTrivialCondition

    @Test
    public void testEmptyTrueBranch() throws CodeGeneratorException, IOException {
        // int x = 0;
        // if (true) {
        // } else {
        //   print(x);
        // }

        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              new IntegerLiteralExp(0)),
                  new IfStmt(new BooleanLiteralExp(true),
                             stmts(),
                             stmts(new PrintStmt(new Variable("x")))));
        assertOutputInMain("TestEmptyTrueBranch",
                           body);
    } // testEmptyTrueBranch

    @Test
    public void testEmptyFalseBranch() throws CodeGeneratorException, IOException {
        // int x = 0;
        // if (false) {
        //   print(x);
        // } else {
        // }

        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              new IntegerLiteralExp(0)),
                  new IfStmt(new BooleanLiteralExp(false),
                             stmts(new PrintStmt(new Variable("x"))),
                             stmts()));
        assertOutputInMain("TestEmptyFalseBranch",
                           body);
    } // testEmptyFalseBranch

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
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              new IntegerLiteralExp(0)),
                  new VariableDeclarationStmt(new IntType(),
                                              new Variable("y"),
                                              new IntegerLiteralExp(1)),
                  new IfStmt(guard,
                             stmts(new PrintStmt(new Variable("x"))),
                             stmts(new PrintStmt(new Variable("y")))));
        assertOutputInMain("TestIfComplexConditionTrue",
                           body,
                           "0");
    } // testIfComplexConditionTrue
    
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
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              new Variable("x"),
                                              new IntegerLiteralExp(0)),
                  new VariableDeclarationStmt(new IntType(),
                                              new Variable("y"),
                                              new IntegerLiteralExp(1)),
                  new IfStmt(guard,
                             stmts(new PrintStmt(new Variable("x"))),
                             stmts(new PrintStmt(new Variable("y")))));
        assertOutputInMain("TestIfComplexConditionFalse",
                           body,
                           "1");
    } // testIfComplexConditionFalse

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
        final List<Stmt> body =
            stmts(new IfStmt(new BooleanLiteralExp(true),
                             trueBranch,
                             falseBranch));
        assertOutputInMain("TestNestedVariableDeclaration",
                           body,
                           "0");
    } // testNestedVariableDeclaration

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
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
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
        assertOutputInMain("TestWhileLoopInitiallyFalse",
                           body,
                           "-1");
    } // testWhileLoopInitiallyFalse

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
        final List<Stmt> body =
            stmts(new VariableDeclarationStmt(new IntType(),
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
        assertOutputInMain("TestWhileLoopInitiallyTrue",
                           body,
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
    } // testWhileLoopInitiallyTrue

    @Test
    public void testMethodReturningIntNoParams() throws CodeGeneratorException, IOException {
        // class Foo extends Object {
        //   init() { super(); }
        //   int foo() { return 1; }
        //   main {
        //     Foo f = new Foo();
        //     int x = f[Foo].foo();
        //     print(x);
        //   }
        // }

        final ClassName cname = new ClassName("Foo");
        final MethodName mname = new MethodName("foo");
        final Variable f = new Variable("f");
        final Variable x = new Variable("x");
        final List<Stmt> mainBody =
            stmts(new VariableDeclarationStmt(new ReferenceType(cname),
                                              f,
                                              new NewExp(cname, actualParams())),
                  new VariableDeclarationStmt(new IntType(),
                                              x,
                                              new MethodCallExp(new VariableExp(f),
                                                                cname,
                                                                mname,
                                                                actualParams())),
                  new PrintStmt(x));
        final ClassDefinition classDef =
            new ClassDefinition(cname,
                                new ClassName(ClassGenerator.objectName),
                                new ArrayList<FormalParam>(),
                                new Constructor(new ArrayList<FormalParam>(),
                                                new ArrayList<Exp>(),
                                                stmts()),
                                new MainDefinition(mainBody),
                                methods(new MethodDefinition(new IntType(),
                                                             mname,
                                                             new ArrayList<FormalParam>(),
                                                             stmts(),
                                                             new IntegerLiteralExp(1))));
        assertOutput(makeProgram(classDef),
                     "1");
    } // testMethodReturningIntNoParams

    @Test
    public void testMethodReturningBoolNoParams() throws CodeGeneratorException, IOException {
        // class Foo extends Object {
        //   init() { super(); }
        //   bool foo() { return true; }
        //   main {
        //     Foo f = new Foo();
        //     bool x = f[Foo].foo();
        //     print(x);
        //   }
        // }

        final ClassName cname = new ClassName("Foo");
        final MethodName mname = new MethodName("foo");
        final Variable f = new Variable("f");
        final Variable x = new Variable("x");
        final List<Stmt> mainBody =
            stmts(new VariableDeclarationStmt(new ReferenceType(cname),
                                              f,
                                              new NewExp(cname, actualParams())),
                  new VariableDeclarationStmt(new BoolType(),
                                              x,
                                              new MethodCallExp(new VariableExp(f),
                                                                cname,
                                                                mname,
                                                                actualParams())),
                  new PrintStmt(x));
        final ClassDefinition classDef =
            new ClassDefinition(cname,
                                new ClassName(ClassGenerator.objectName),
                                new ArrayList<FormalParam>(),
                                new Constructor(new ArrayList<FormalParam>(),
                                                new ArrayList<Exp>(),
                                                stmts()),
                                new MainDefinition(mainBody),
                                methods(new MethodDefinition(new BoolType(),
                                                             mname,
                                                             new ArrayList<FormalParam>(),
                                                             stmts(),
                                                             new BooleanLiteralExp(true))));
        assertOutput(makeProgram(classDef),
                     "true");
    } // testMethodReturningBoolNoParams

    @Test
    public void testIntFunctionTakingParams() throws CodeGeneratorException, IOException {
        // class Foo extends Object {
        //   init() { super(); }
        //   int add(int x, int y) {
        //     print(x);
        //     print(y);
        //     return x + y;
        //   }
        //   main {
        //     Foo f = new Foo();
        //     int x = f[Foo].add(1, 2);
        //     print(x);
        //   }
        // }

        final ClassName cname = new ClassName("Foo");
        final Variable f = new Variable("f");
        final Variable x = new Variable("x");
        final Variable y = new Variable("y");
        final MethodName mname = new MethodName("add");
        final List<FormalParam> formalParams = new ArrayList<FormalParam>();
        formalParams.add(new FormalParam(new IntType(), new Variable("x")));
        formalParams.add(new FormalParam(new IntType(), new Variable("y")));
        
        final MethodDefinition add =
            new MethodDefinition(new IntType(),
                                 mname,
                                 formalParams,
                                 stmts(new PrintStmt(x),
                                       new PrintStmt(y)),
                                 new BinopExp(new VariableExp(x),
                                              new PlusBOP(),
                                              new VariableExp(y)));
        final List<Exp> params = actualParams(new IntegerLiteralExp(1),
                                              new IntegerLiteralExp(2));
        final List<Stmt> entryPoint =
            stmts(new VariableDeclarationStmt(new ReferenceType(cname),
                                              f,
                                              new NewExp(cname, actualParams())),
                  new VariableDeclarationStmt(new IntType(),
                                              x,
                                              new MethodCallExp(new VariableExp(f),
                                                                cname,
                                                                mname,
                                                                params)),
                  new PrintStmt(x));
        final ClassDefinition classDef =
            new ClassDefinition(cname,
                                new ClassName(ClassGenerator.objectName),
                                new ArrayList<FormalParam>(),
                                new Constructor(new ArrayList<FormalParam>(),
                                                new ArrayList<Exp>(),
                                                stmts()),
                                new MainDefinition(entryPoint),
                                methods(add));
        assertOutput(makeProgram(classDef),
                     "1",
                     "2",
                     "3");
    } // testIntFunctionTakingParams
    
    @Test
    public void testMutualRecursion() throws CodeGeneratorException, IOException {
        // class Foo extends Object {
        //   init() { super(); }
        //   bool isEven(int x) {
        //     bool result = false;
        //     if (x == 0) {
        //       result = true;
        //     } else {
        //       result = this[Foo].isOdd(x - 1);
        //     }
        //     return result;
        //   }
        //
        //   bool isOdd(int x) {
        //     bool result = false;
        //     if (x == 0) {
        //       result = false;
        //     } else {
        //       result = this[Foo].isEven(x - 1);
        //     }
        //     return result;
        //   }
        //
        //   main {
        //     Foo f = new Foo();
        //     bool evenYes = f[Foo].isEven(6);
        //     bool evenNo = f[Foo].isEven(7);
        //     bool oddYes = f[Foo].isOdd(9);
        //     bool oddNo = f[Foo].isOdd(10);
        //     print(evenYes);
        //     print(evenNo);
        //     print(oddYes);
        //     print(oddNo);
        //   }
        // }

        final ClassName cname = new ClassName("Foo");
        final Variable f = new Variable("f");
        final List<FormalParam> formalParams = new ArrayList<FormalParam>();
        formalParams.add(new FormalParam(new IntType(), new Variable("x")));
        
        final Variable result = new Variable("result");
        final MethodDefinition isEven =
            new MethodDefinition(new BoolType(),
                                 new MethodName("isEven"),
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
                                                                       new MethodCallExp(new VariableExp(new Variable("this")),
                                                                                         cname,
                                                                                         new MethodName("isOdd"),
                                                                                         actualParams(new BinopExp(new VariableExp(new Variable("x")),
                                                                                                                   new MinusBOP(),
                                                                                                                   new IntegerLiteralExp(1)))))))),
                                 new VariableExp(result));
        final MethodDefinition isOdd =
            new MethodDefinition(new BoolType(),
                                 new MethodName("isOdd"),
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
                                                                       new MethodCallExp(new VariableExp(new Variable("this")),
                                                                                         cname,
                                                                                         new MethodName("isEven"),
                                                                                         actualParams(new BinopExp(new VariableExp(new Variable("x")),
                                                                                                                   new MinusBOP(),
                                                                                                                   new IntegerLiteralExp(1)))))))),
                                 new VariableExp(result));

        final Variable evenYes = new Variable("evenYes");
        final Variable evenNo = new Variable("evenNo");
        final Variable oddYes = new Variable("oddYes");
        final Variable oddNo = new Variable("oddNo");

        final List<Stmt> mainBody =
            stmts(new VariableDeclarationStmt(new ReferenceType(cname),
                                              f,
                                              new NewExp(cname, actualParams())),
                  new VariableDeclarationStmt(new BoolType(),
                                              evenYes,
                                              new MethodCallExp(new VariableExp(f),
                                                                cname,
                                                                new MethodName("isEven"),
                                                                actualParams(new IntegerLiteralExp(6)))),
                  new VariableDeclarationStmt(new BoolType(),
                                              evenNo,
                                              new MethodCallExp(new VariableExp(f),
                                                                cname,
                                                                new MethodName("isEven"),
                                                                actualParams(new IntegerLiteralExp(7)))),
                  new VariableDeclarationStmt(new BoolType(),
                                              oddYes,
                                              new MethodCallExp(new VariableExp(f),
                                                                cname,
                                                                new MethodName("isOdd"),
                                                                actualParams(new IntegerLiteralExp(9)))),
                  new VariableDeclarationStmt(new BoolType(),
                                              oddNo,
                                              new MethodCallExp(new VariableExp(f),
                                                                cname,
                                                                new MethodName("isOdd"),
                                                                actualParams(new IntegerLiteralExp(10)))),
                  new PrintStmt(evenYes),
                  new PrintStmt(evenNo),
                  new PrintStmt(oddYes),
                  new PrintStmt(oddNo));
        final ClassDefinition classDef =
            new ClassDefinition(cname,
                                new ClassName(ClassGenerator.objectName),
                                new ArrayList<FormalParam>(),
                                new Constructor(new ArrayList<FormalParam>(),
                                                new ArrayList<Exp>(),
                                                stmts()),
                                new MainDefinition(mainBody),
                                methods(isEven, isOdd));
        assertOutput(makeProgram(classDef),
                     "true",
                     "false",
                     "true",
                     "false");
    } // testMutualRecursion

    @Test
    public void testGettersSetters() throws CodeGeneratorException, IOException {
        // class TestGettersSetters extends Object {
        //   int theInt;
        //   bool theBool;
        //   init(int theInt, int theBool) {
        //     super();
        //     print(theInt);
        //     print(theBool);
        //     this[TestGettersSetters].theInt = theInt;
        //     this[TestGettersSetters].theBool = theBool;
        //   }
        //   int getInt() {
        //     return this[TestGettersSetters].theInt;
        //   }
        //   bool getBool() {
        //     return this[TestGettersSetters].theBool;
        //   }
        //   int setInt(int theInt) {
        //     this[TestGettersSetters].theInt = theInt;
        //     return 0;
        //   }
        //   int setBool(bool theBool) {
        //     this[TestGettersSetters].theBool = theBool;
        //     return 0;
        //   }
        //   int printContents() {
        //     int tempInt = this[TestGettersSetters].getInt();
        //     bool tempBool = this[TestGettersSetters].getBool();
        //     print(tempInt);
        //     print(tempBool);
        //     return 0;
        //   }
        //   main {
        //     TestGettersSetters f = new TestGettersSetters(1, true);
        //     int junk = f[TestGettersSetters].printContents();
        //     junk = f[TestGettersSetters].setInt(5);
        //     junk = f[TestGettersSetters].setBool(false);
        //     junk = f[TestGettersSetters].printContents();
        //   }
        // }

        final ClassName cname = new ClassName("TestGettersSetters");
        final Variable f = new Variable("f");
        final Variable theInt = new Variable("theInt");
        final Variable theBool = new Variable("theBool");
        final Variable thisVar = new Variable("this");
        final Variable junk = new Variable("junk");
        final Variable tempInt = new Variable("tempInt");
        final Variable tempBool = new Variable("tempBool");
        final MethodName setInt = new MethodName("setInt");
        final MethodName getInt = new MethodName("getInt");
        final MethodName setBool = new MethodName("setBool");
        final MethodName getBool = new MethodName("getBool");
        final MethodName printContents = new MethodName("printContents");

        final List<FormalParam> bothFormalParams = new ArrayList<FormalParam>();
        bothFormalParams.add(new FormalParam(new IntType(), theInt));
        bothFormalParams.add(new FormalParam(new BoolType(), theBool));

        final List<FormalParam> setIntFormalParams = new ArrayList<FormalParam>();
        setIntFormalParams.add(new FormalParam(new IntType(), theInt));
        final List<FormalParam> setBoolFormalParams = new ArrayList<FormalParam>();
        setBoolFormalParams.add(new FormalParam(new BoolType(), theBool));

        final List<Stmt> constructorBody =
            stmts(new PrintStmt(theInt),
                  new PrintStmt(theBool),
                  new PutStmt(new VariableExp(thisVar),
                              cname,
                              theInt,
                              new VariableExp(theInt)),
                  new PutStmt(new VariableExp(thisVar),
                              cname,
                              theBool,
                              new VariableExp(theBool)));

        final List<Stmt> mainBody =
            stmts(new VariableDeclarationStmt(new ReferenceType(cname),
                                              f,
                                              new NewExp(cname,
                                                         actualParams(new IntegerLiteralExp(1),
                                                                      new BooleanLiteralExp(true)))),
                  new VariableDeclarationStmt(new IntType(),
                                              junk,
                                              new MethodCallExp(new VariableExp(f),
                                                                cname,
                                                                printContents,
                                                                actualParams())),
                  new AssignStmt(junk,
                                 new MethodCallExp(new VariableExp(f),
                                                   cname,
                                                   setInt,
                                                   actualParams(new IntegerLiteralExp(5)))),
                  new AssignStmt(junk,
                                 new MethodCallExp(new VariableExp(f),
                                                   cname,
                                                   setBool,
                                                   actualParams(new BooleanLiteralExp(false)))),
                  new AssignStmt(junk,
                                 new MethodCallExp(new VariableExp(f),
                                                   cname,
                                                   printContents,
                                                   actualParams())));

        final List<Stmt> printContentsBody =
            stmts(new VariableDeclarationStmt(new IntType(),
                                              tempInt,
                                              new MethodCallExp(new VariableExp(thisVar),
                                                                cname,
                                                                getInt,
                                                                actualParams())),
                  new VariableDeclarationStmt(new BoolType(),
                                              tempBool,
                                              new MethodCallExp(new VariableExp(thisVar),
                                                                cname,
                                                                getBool,
                                                                actualParams())),
                  new PrintStmt(tempInt),
                  new PrintStmt(tempBool));

        final ClassDefinition classDef =
            new ClassDefinition(cname,
                                new ClassName(ClassGenerator.objectName),
                                bothFormalParams,
                                new Constructor(bothFormalParams,
                                                actualParams(),
                                                constructorBody),
                                new MainDefinition(mainBody),
                                methods(new MethodDefinition(new IntType(),
                                                             getInt,
                                                             new ArrayList<FormalParam>(),
                                                             stmts(),
                                                             new GetExp(new VariableExp(thisVar),
                                                                        cname,
                                                                        theInt)),
                                        new MethodDefinition(new BoolType(),
                                                             getBool,
                                                             new ArrayList<FormalParam>(),
                                                             stmts(),
                                                             new GetExp(new VariableExp(thisVar),
                                                                        cname,
                                                                        theBool)),
                                        new MethodDefinition(new IntType(),
                                                             setInt,
                                                             setIntFormalParams,
                                                             stmts(new PutStmt(new VariableExp(thisVar),
                                                                               cname,
                                                                               theInt,
                                                                               new VariableExp(theInt))),
                                                             new IntegerLiteralExp(0)),
                                        new MethodDefinition(new IntType(),
                                                             setBool,
                                                             setBoolFormalParams,
                                                             stmts(new PutStmt(new VariableExp(thisVar),
                                                                               cname,
                                                                               theBool,
                                                                               new VariableExp(theBool))),
                                                             new IntegerLiteralExp(0)),
                                        new MethodDefinition(new IntType(),
                                                             printContents,
                                                             new ArrayList<FormalParam>(),
                                                             printContentsBody,
                                                             new IntegerLiteralExp(0))));
        assertOutput(makeProgram(classDef),
                     "1",
                     "true",
                     "1",
                     "true",
                     "5",
                     "false");
    } // testGettersSetters

    @Test
    public void testMultipleClasses() throws CodeGeneratorException, IOException {
        // class List extends Object {
        //   init() { super(); }
        //   main {}
        //   int length() { return -1; }
        // }
        // class Nil extends List {
        //   init() { super(); }
        //   main {}
        //   int length() { return 0; }
        // }
        // class Cons extends List {
        //   int head;
        //   List tail;
        //   init(int head, List tail) {
        //     super();
        //     this[Cons].head = head;
        //     this[Cons].tail = tail;
        //   }
        //   int length() { return 1 + this[Cons].tail[List].length(); }
        //   main {
        //     List list = new Cons(-1, new Cons(-2, new Cons(-3, new Nil())));
        //     int len = list[List].length();
        //     print(len);
        //   }
        // }

        final Variable thisVar = new Variable("this");

        final ClassDefinition listDef =
            new ClassDefinition(new ClassName("List"),
                                new ClassName(ClassGenerator.objectName),
                                new ArrayList<FormalParam>(),
                                new Constructor(new ArrayList<FormalParam>(),
                                                actualParams(),
                                                stmts()),
                                new MainDefinition(stmts()),
                                methods(new MethodDefinition(new IntType(),
                                                             new MethodName("length"),
                                                             new ArrayList<FormalParam>(),
                                                             stmts(),
                                                             new IntegerLiteralExp(-1))));
        final ClassDefinition nilDef =
            new ClassDefinition(new ClassName("Nil"),
                                new ClassName("List"),
                                new ArrayList<FormalParam>(),
                                new Constructor(new ArrayList<FormalParam>(),
                                                actualParams(),
                                                stmts()),
                                new MainDefinition(stmts()),
                                methods(new MethodDefinition(new IntType(),
                                                             new MethodName("length"),
                                                             new ArrayList<FormalParam>(),
                                                             stmts(),
                                                             new IntegerLiteralExp(0))));

        final List<FormalParam> consConstructorFormalParams = new ArrayList<FormalParam>();
        consConstructorFormalParams.add(new FormalParam(new IntType(), new Variable("head")));
        consConstructorFormalParams.add(new FormalParam(new ReferenceType(new ClassName("List")),
                                                        new Variable("tail")));

        final List<Stmt> mainBody =
            stmts(new VariableDeclarationStmt(new ReferenceType(new ClassName("List")),
                                              new Variable("list"),
                                              new NewExp(new ClassName("Cons"),
                                                         actualParams(new IntegerLiteralExp(-1),
                                                                      new NewExp(new ClassName("Cons"),
                                                                                 actualParams(new IntegerLiteralExp(-2),
                                                                                              new NewExp(new ClassName("Cons"),
                                                                                                         actualParams(new IntegerLiteralExp(-3),
                                                                                                                      new NewExp(new ClassName("Nil"),
                                                                                                                                 actualParams())))))))),
                  new VariableDeclarationStmt(new IntType(),
                                              new Variable("len"),
                                              new MethodCallExp(new VariableExp(new Variable("list")),
                                                                new ClassName("List"),
                                                                new MethodName("length"),
                                                                actualParams())),
                  new PrintStmt(new Variable("len")));

        final ClassDefinition consDef =
            new ClassDefinition(new ClassName("Cons"),
                                new ClassName("List"),
                                consConstructorFormalParams,
                                new Constructor(consConstructorFormalParams,
                                                actualParams(),
                                                stmts(new PutStmt(new VariableExp(thisVar),
                                                                  new ClassName("Cons"),
                                                                  new Variable("head"),
                                                                  new VariableExp(new Variable("head"))),
                                                      new PutStmt(new VariableExp(thisVar),
                                                                  new ClassName("Cons"),
                                                                  new Variable("tail"),
                                                                  new VariableExp(new Variable("tail"))))),
                                new MainDefinition(mainBody),
                                methods(new MethodDefinition(new IntType(),
                                                             new MethodName("length"),
                                                             new ArrayList<FormalParam>(),
                                                             stmts(),
                                                             new BinopExp(new IntegerLiteralExp(1),
                                                                          new PlusBOP(),
                                                                          new MethodCallExp(new GetExp(new VariableExp(thisVar),
                                                                                                       new ClassName("Cons"),
                                                                                                       new Variable("tail")),
                                                                                            new ClassName("List"),
                                                                                            new MethodName("length"),
                                                                                            actualParams())))));
        assertOutput(makeProgram(consDef, nilDef, listDef),
                     "3");
    } // testMultipleClasses

    @Test
    public void testLambdaCreationCall() throws CodeGeneratorException, IOException {
        // class Integer extends Object {
        //   int value;
        //   init(int value) {
        //     super();
        //     this[Integer].value = value;
        //   }
        //   main {
        //     Integer i = new Integer(5);
        //     (Integer => Integer) f = (Integer x) => [Integer]x;
        //     int value = f[Integer](i)[Integer].value;
        //     print(value);
        //   }
        // }
        final ClassName integer = new ClassName("Integer");
        final List<FormalParam> instanceVariables = new ArrayList<FormalParam>();
        instanceVariables.add(new FormalParam(new IntType(), new Variable("value")));

        final List<Stmt> main =
            stmts(new VariableDeclarationStmt(new ReferenceType(integer),
                                              new Variable("i"),
                                              new NewExp(integer,
                                                         actualParams(new IntegerLiteralExp(5)))),
                  new VariableDeclarationStmt(new LambdaType(new ReferenceType(integer),
                                                             new ReferenceType(integer)),
                                              new Variable("f"),
                                              new LambdaExp(new ReferenceType(integer),
                                                            new Variable("x"),
                                                            new ReferenceType(integer),
                                                            new VariableExp(new Variable("x")))),
                  new VariableDeclarationStmt(new IntType(),
                                              new Variable("value"),
                                              new GetExp(new LambdaCallExp(new VariableExp(new Variable("f")),
                                                                           new ReferenceType(integer),
                                                                           new VariableExp(new Variable("i"))),
                                                         integer,
                                                         new Variable("value"))),
                  new PrintStmt(new Variable("value")));
        
        final ClassDefinition intDef =
            new ClassDefinition(integer,
                                new ClassName(ClassGenerator.objectName),
                                instanceVariables,
                                new Constructor(instanceVariables,
                                                actualParams(),
                                                stmts(new PutStmt(new VariableExp(new Variable("this")),
                                                                  integer,
                                                                  new Variable("value"),
                                                                  new VariableExp(new Variable("value"))))),
                                new MainDefinition(main),
                                methods());
        assertOutput(makeProgram(intDef),
                     "5");
    }
} // CodeGeneratorTest
