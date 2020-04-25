package codegen_example.syntax;

public class FunctionName {
    public final String name;

    public FunctionName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof FunctionName &&
                name.equals(((FunctionName)other).name));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
} // FunctionName
