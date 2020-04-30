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

    public String toDescriptorString() {
        final StringBuffer result = new StringBuffer();
        result.append("(");
        for (final FormalParam param : formalParams) {
            result.append(param.type.toDescriptorString());
        }
        result.append(")");
        result.append(returnType.toDescriptorString());
        return result.toString();
    } // toDescriptorString
} // Callable
