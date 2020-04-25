package codegen_example.syntax;

import java.util.List;

public class Function {
    public final Type returnType;
    public final FunctionName name;
    public final List<FormalParam> formalParams;
    public final List<Stmt> body;
    public final Exp returned;

    public Function(final Type returnType,
                    final FunctionName name,
                    final List<FormalParam> formalParams,
                    final List<Stmt> body,
                    final Exp returned) {
        this.returnType = returnType;
        this.name = name;
        this.formalParams = formalParams;
        this.body = body;
        this.returned = returned;
    }

    public static String formalParamsToString(final List<FormalParam> formalParams) {
        final StringBuffer result = new StringBuffer();
        for (final FormalParam param : formalParams) {
            result.append(param.toString());
        }
        return result.toString();
    } // formalParamsToString
    
    @Override
    public String toString() {
        return (returnType.toString() +
                " " +
                name.toString() +
                "(" +
                formalParamsToString(formalParams) +
                ") { " +
                StmtHelpers.stmtsToStringAutoIndent(body) +
                " return " +
                returned.toString() +
                ";\n}");
    }
} // Function
