package codegen_example.codegen;

import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;

import codegen_example.syntax.Type;
import codegen_example.syntax.BoolType;
import codegen_example.syntax.IntType;
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

    public void load(final MethodVisitor visitor) {
        // both are treated as integers at the bytecode level
        assert(type instanceof IntType ||
               type instanceof BoolType);
        visitor.visitVarInsn(ILOAD, index);
    } // load

    public void store(final MethodVisitor visitor) {
        // both are treated as integers at the bytecode level
        assert(type instanceof IntType ||
               type instanceof BoolType);
        visitor.visitVarInsn(ISTORE, index);
    }
} // VariableEntry
