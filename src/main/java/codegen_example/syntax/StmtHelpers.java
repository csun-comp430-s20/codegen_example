package codegen_example.syntax;

import java.util.List;

public class StmtHelpers {
    public static final int NUM_SPACES_PER_INDENT = 4;

    // TERRIBLE HACK so toString can always be called correctly on
    // a statement.
    private static int indentLevel = 0;

    public static String stmtsToStringAutoIndent(final List<Stmt> stmts) {
        final String result = stmtsToString(++indentLevel, stmts);
        --indentLevel;
        return result;
    }
    
    public static String indentToSpaces(final int indentAmount) {
        final StringBuffer result = new StringBuffer();
        final int numSpaces = indentAmount * NUM_SPACES_PER_INDENT;
        for (int numSpace = 0; numSpace < numSpaces; numSpace++) {
            result.append(' ');
        }
        return result.toString();
    } // indentToSpaces
    
    public static String stmtsToString(final int indentAmount,
                                       final List<Stmt> stmts) {
        final StringBuffer result = new StringBuffer();
        final String indentString = indentToSpaces(indentAmount);
        for (final Stmt statement : stmts) {
            result.append(indentString);
            result.append(statement.toString());
        }
        return result.toString();
    } // stmtsToString
} // StmtHelpers
