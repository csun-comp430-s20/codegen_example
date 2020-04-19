package codegen_example.syntax;

public class Variable {
    public final String name;
    
    public Variable(final String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof Variable &&
                ((Variable)other).name.equals(name));
    }

    @Override
    public String toString() {
        return name;
    }
} // Variable
