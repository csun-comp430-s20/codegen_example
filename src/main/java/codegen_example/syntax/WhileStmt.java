package codegen_example.syntax;

import java.util.List;

public class WhileStmt implements Stmt {
    public final Exp guard;
    public final List<Stmt> body;

    public WhileStmt(final Exp guard,
                     final List<Stmt> body) {
        this.guard = guard;
        this.body = body;
    }

    @Override
    public String toString() {
        return ("while (" +
                guard.toString() +
                ") {\n" +
                StmtHelpers.stmtsToStringAutoIndent(body) +
                "\n}");
    }
} // WhileStmt
