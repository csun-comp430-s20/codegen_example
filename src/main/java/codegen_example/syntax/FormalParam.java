package codegen_example.syntax;

public class FormalParam {
    public final Type type;
    public final Variable variable;

    public FormalParam(final Type type,
                       final Variable variable) {
        this.type = type;
        this.variable = variable;
    }

    @Override
    public String toString() {
        return type.toString() + " " + variable.toString();
    }
} // FormalParam
