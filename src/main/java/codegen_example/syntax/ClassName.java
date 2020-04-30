package codegen_example.syntax;

public class ClassName {
    public final String name;

    public ClassName(final String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof ClassName &&
                name.equals(((ClassName)other).name));
    }
} // ClassName
