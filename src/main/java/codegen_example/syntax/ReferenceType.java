package codegen_example.syntax;

public class ReferenceType implements Type {
    public final ClassName refersTo;

    public ReferenceType(final ClassName refersTo) {
        this.refersTo = refersTo;
    }

    @Override
    public int hashCode() {
        return refersTo.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof ReferenceType &&
                refersTo.equals(((ReferenceType)other).refersTo));
    }

    public String toDescriptorString() {
        return "L" + refersTo.name + ";";
    }
} // ReferenceType
