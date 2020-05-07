package codegen_example.codegen;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
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
    private final LambdaMaker lambdaMaker;
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
        lambdaMaker = new LambdaMaker(this.allClasses);
    } // ClassGenerator

    public static void writeClass(final ClassWriter classWriter,
                                  final File destination) throws IOException {
        final BufferedOutputStream output =
            new BufferedOutputStream(new FileOutputStream(destination));
        output.write(classWriter.toByteArray());
        output.close();
    } // writeClass
    
    public static void writeInstanceVariables(final ClassWriter classWriter,
                                              final List<FormalParam> instanceVariables) {
        for (final FormalParam field : instanceVariables) {
            classWriter.visitField(ACC_PUBLIC,
                                   field.variable.name,
                                   field.type.toDescriptorString(),
                                   null,
                                   null).visitEnd();
        }
    } // writeInstanceVariables
    
    public void writeClasses(final String toDirectory) throws CodeGeneratorException, IOException {
        for (final ClassDefinition classDef : allClasses.values()) {
            final SingleClassGenerator genClass = new SingleClassGenerator(classDef);
            genClass.writeClass(toDirectory);
        }
        lambdaMaker.writeLambdas(toDirectory);
    } // writeClasses

    // intended for testing.  Deletes all created classes in the given directory
    public void deleteClasses(final String inDirectory) {
        for (final ClassDefinition classDef : allClasses.values()) {
            new File(inDirectory, classDef.name.name + ".class").delete();
        }
        lambdaMaker.deleteClasses(inDirectory);
    } // deleteClasses
    
    private class SingleClassGenerator {
        // ---BEGIN INSTANCE VARIABLES---
        private final ClassDefinition forClass;
        private final ReferenceType thisType;
        private final ClassWriter classWriter;
        // ---END INSTANCE VARIABLES---

        public SingleClassGenerator(final ClassDefinition forClass)
            throws CodeGeneratorException {
            this.forClass = forClass;
            thisType = new ReferenceType(forClass.name);
            classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classWriter.visit(V1_7, // Java 1.7
                              ACC_PUBLIC, // public
                              forClass.name.name, // class name
                              null, // signature (null means not generic)
                              forClass.extendsName.name, // superclass
                              new String[0]); // interfaces
            writeInstanceVariables(classWriter, forClass.instanceVariables);
        } // SingleClassGenerator

        public void writeClass(final String toDirectory) throws CodeGeneratorException, IOException {
            SingleMethodGenerator methodGen = new SingleMethodGenerator(forClass.constructor);
            methodGen.writeMethod();
            methodGen = new SingleMethodGenerator(forClass.main);
            methodGen.writeMethod();
            for (final MethodDefinition method : forClass.methods) {
                methodGen = new SingleMethodGenerator(method);
                methodGen.writeMethod();
            }
            classWriter.visitEnd();
            ClassGenerator.writeClass(classWriter,
                                      new File(toDirectory, forClass.name.name + ".class"));
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
            
            public void writeMethod() throws CodeGeneratorException {
                methodVisitor.visitCode();
                final ExpressionStatementGenerator gen =
                    new ExpressionStatementGenerator(allClasses,
                                                     lambdaMaker,
                                                     variables,
                                                     methodVisitor);
                if (method instanceof Constructor) {
                    // ---BEGIN CODE FOR SUPER---
                    gen.loadVariable(thisVariable); // load "this"
                    gen.writeExpressions(((Constructor)method).superParams);
                    methodVisitor.visitMethodInsn(INVOKESPECIAL,
                                                  forClass.extendsName.name,
                                                  "<init>",
                                                  gen.constructorDescriptorFor(forClass.extendsName),
                                                  false);
                    // ---END CODE FOR SUPER---
                }
                gen.writeStatements(method.body);
                if (method instanceof MethodDefinition) {
                    gen.writeExpression(((MethodDefinition)method).returned);
                }
                gen.doReturn(method.returnType);
                methodVisitor.visitMaxs(0, 0);
            } // writeMethod
        } // SingleMethodGenerator
    } // SingleClassGenerator
} // ClassGenerator
