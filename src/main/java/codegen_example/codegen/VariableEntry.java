package codegen_example.codegen;

import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;

import codegen_example.syntax.Type;
import codegen_example.syntax.BoolType;
import codegen_example.syntax.IntType;
import codegen_example.syntax.ReferenceType;
import codegen_example.syntax.Variable;

public class VariableEntry {
    public final Variable variable;
    public final Type type;
    public final int index;

    public VariableEntry(final Variable variable,
                         final Type type,
                         final int index) {
        assert(index >= 0);
        this.variable = variable;
        this.type = type;
        this.index = index;
    }

    public void load(final MethodVisitor visitor) throws CodeGeneratorException {
        // both are treated as integers at the bytecode level
        if (type instanceof IntType ||
            type instanceof BoolType) {
            visitor.visitVarInsn(ILOAD, index);
        } else if (type instanceof ReferenceType) {
            visitor.visitVarInsn(ALOAD, index);
        } else {
            throw new CodeGeneratorException("Unknown load type: " + type);
        }
    } // load

    public void store(final MethodVisitor visitor) throws CodeGeneratorException {
        // both are treated as integers at the bytecode level
        if (type instanceof IntType ||
            type instanceof BoolType) {
            visitor.visitVarInsn(ISTORE, index);
        } else if (type instanceof ReferenceType) {
            visitor.visitVarInsn(ASTORE, index);
        } else {
            throw new CodeGeneratorException("Unknown store type: " + type);
        }
    } // store
} // VariableEntry
