package codegen_example.syntax;

public class MethodName {
    public final String name;

    public MethodName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof MethodName &&
                name.equals(((MethodName)other).name));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
} // MethodName
