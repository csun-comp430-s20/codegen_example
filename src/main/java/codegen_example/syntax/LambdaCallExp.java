package codegen_example.syntax;

public class LambdaCallExp implements Exp {
    public final Exp lambda;
    public final ReferenceType returnType;
    public final Exp param;

    public LambdaCallExp(final Exp lambda,
                         final ReferenceType returnType,
                         final Exp param) {
        this.lambda = lambda;
        this.returnType = returnType;
        this.param = param;
    }
} // LambdaCallExp
