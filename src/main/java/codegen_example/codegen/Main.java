package codegen_example.codegen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

// Helpful resources:
// -ASM reference manual: https://asm.ow2.io/asm4-guide.pdf
// -ASM Javadoc: https://asm.ow2.io/javadoc/org/objectweb/asm/package-summary.html
// -Basics on Java Bytecode:
//     - Writing bytecode with ASM: https://www.beyondjava.net/quick-guide-writing-byte-code-asm
// -Writing classes from scratch with ASM: https://dzone.com/articles/fully-dynamic-classes-with-asm

public class Main {
    public static final String className = "Test";
    public static final String methodName = "test";
    
    // public class Test {
    //   public static int test() { return 1; }
    // }
    
    public static void main(final String[] args) throws IOException {
        final String className = "Test";
        final ClassWriter writer =
            new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        writer.visit(V1_7,
                     ACC_PUBLIC,
                     className,
                     null,
                     "java/lang/Object",
                     new String[0]);
        // constructor
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
        
        final MethodVisitor testMethod =
            writer.visitMethod(ACC_PUBLIC,
                               methodName,
                               "()I",
                               null,
                               null);
        testMethod.visitCode();
        testMethod.visitInsn(ICONST_1);
        testMethod.visitInsn(IRETURN);

        writer.visitEnd();
                                                            
        final BufferedOutputStream output =
            new BufferedOutputStream(new FileOutputStream(new File("Test.class")));
        output.write(writer.toByteArray());
        output.close();
    } // main
}