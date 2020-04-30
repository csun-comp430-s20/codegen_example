package codegen_example.syntax;

import java.util.List;

public class MethodDefinition extends Callable {
    public final MethodName name;
    public final Exp returned;

    public MethodDefinition(final Type returnType,
                            final MethodName name,
                            final List<FormalParam> formalParams,
                            final List<Stmt> body,
                            final Exp returned) {
        super(name, formalParams, body, returnType);
        this.name = name;
        this.returned = returned;
    }

    public static String formalParamsToString(final List<FormalParam> formalParams) {
        final StringBuffer result = new StringBuffer();
        for (final FormalParam param : formalParams) {
            result.append(param.toString());
        }
        return result.toString();
    } // formalParamsToString
} // MethodDefinition
