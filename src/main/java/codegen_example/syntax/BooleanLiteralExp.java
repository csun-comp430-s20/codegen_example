package codegen_example.syntax;

public class BooleanLiteralExp implements Exp {
    public final boolean value;

    public BooleanLiteralExp(final boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
} // BooleanLiteralExp
