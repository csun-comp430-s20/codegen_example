package codegen_example.syntax;

public class VariableExp implements Exp {
    public final Variable variable;

    public VariableExp(final Variable variable) {
        this.variable = variable;
    }

    @Override
    public String toString() {
        return variable.toString();
    }
} // VariableExp

