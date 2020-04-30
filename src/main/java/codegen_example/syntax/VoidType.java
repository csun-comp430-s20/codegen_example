package codegen_example.syntax;

public class VoidType implements Type {
    @Override
    public int hashCode() {
        return 2;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof VoidType;
    }

    @Override
    public String toString() {
        return "void";
    }

    public String toDescriptorString() {
        return "V";
    }
} // VoidType
