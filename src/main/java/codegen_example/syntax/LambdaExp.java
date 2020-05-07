package codegen_example.syntax;

public class LambdaExp implements Exp {
    public final ReferenceType paramType;
    public final Variable param;
    public final ReferenceType returnType;
    public final Exp body;

    public LambdaExp(final ReferenceType paramType,
                     final Variable param,
                     final ReferenceType returnType,
                     final Exp body) {
        this.paramType = paramType;
        this.param = param;
        this.returnType = returnType;
        this.body = body;
    }
} // LambdaExp

    
