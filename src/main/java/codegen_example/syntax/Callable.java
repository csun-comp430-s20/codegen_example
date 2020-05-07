package codegen_example.syntax;

import java.util.List;

public class Callable {
    public final MethodName name;
    public final List<FormalParam> formalParams;
    public final List<Stmt> body;
    public final Type returnType;

    public Callable(final MethodName name,
                    final List<FormalParam> formalParams,
                    final List<Stmt> body,
                    final Type returnType) {
        this.name = name;
        this.formalParams = formalParams;
        this.body = body;
        this.returnType = returnType;
    }

    public static String toDescriptorString(final List<FormalParam> formalParams,
                                            final Type returnType) {
        final StringBuffer result = new StringBuffer();
        result.append("(");
        for (final FormalParam param : formalParams) {
            result.append(param.type.toDescriptorString());
        }
        result.append(")");
        result.append(returnType.toDescriptorString());
        return result.toString();
    } // toDescriptorString
    
    public String toDescriptorString() {
        return toDescriptorString(formalParams, returnType);
    } // toDescriptorString
} // Callable
