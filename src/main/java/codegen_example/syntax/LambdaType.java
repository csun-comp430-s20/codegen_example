package codegen_example.syntax;

public class LambdaType extends ReferenceType {
    public final ReferenceType paramType;
    public final ReferenceType returnType;

    public LambdaType(final ReferenceType paramType,
                      final ReferenceType returnType) {
        super(new ClassName("Function1"));
        this.paramType = paramType;
        this.returnType = returnType;
    }

    @Override
    public int hashCode() {
        return paramType.hashCode() + returnType.hashCode();
    } // hashCode

    @Override
    public boolean equals(final Object other) {
        if (other instanceof LambdaType) {
            final LambdaType otherLambda = (LambdaType)other;
            return (paramType.equals(otherLambda.paramType) &&
                    returnType.equals(otherLambda.returnType));
        } else {
            return false;
        }
    } // equals

    @Override
    public String toString() {
        return "(" + paramType.toString() + " => " + returnType.toString() + ")";
    } // toString

    @Override
    public String toSignatureString() {
        return "LFunction1<" + paramType.toSignatureString() + returnType.toSignatureString() + ">;";
    }
} // LambdaType

