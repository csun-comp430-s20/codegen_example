package codegen_example.syntax;

import java.util.List;

public class MethodCallExp implements Exp {
    public final Exp callOn;
    public final ClassName callOnName;
    public final MethodName name;
    public final List<Exp> actualParams;

    public MethodCallExp(final Exp callOn,
                         final ClassName callOnName,
                         final MethodName name,
                         final List<Exp> actualParams) {
        this.callOn = callOn;
        this.callOnName = callOnName;
        this.name = name;
        this.actualParams = actualParams;
    }

    public static String expressionsToString(final List<Exp> expressions) {
        final int len = expressions.size();
        int curPos = 0;
        final StringBuffer result = new StringBuffer();

        for (final Exp exp : expressions) {
            result.append(exp.toString());
            if (curPos < len - 1) {
                result.append(", ");
            }
            curPos++;
        }
        return result.toString();
    } // expressionsToString
} // MethodCallExp
