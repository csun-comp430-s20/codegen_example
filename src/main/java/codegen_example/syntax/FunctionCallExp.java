package codegen_example.syntax;

import java.util.List;

public class FunctionCallExp implements Exp {
    public final FunctionName name;
    public final List<Exp> actualParams;

    public FunctionCallExp(final FunctionName name,
                           final List<Exp> actualParams) {
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
    
    @Override
    public String toString() {
        return (name.toString() +
                "(" +
                expressionsToString(actualParams) +
                ")");
    }
} // FunctionCallExp
