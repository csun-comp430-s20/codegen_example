package codegen_example.syntax;

public class IntegerLiteralExp implements Exp {
    public final int value;

    public IntegerLiteralExp(final int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
} // IntegerLiteralExp
