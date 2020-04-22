package codegen_example.syntax;

import java.util.List;

public class IfStmt implements Stmt {
    public final Exp guard;
    public final List<Stmt> trueBranch;
    public final List<Stmt> falseBranch;

    public IfStmt(final Exp guard,
                  final List<Stmt> trueBranch,
                  final List<Stmt> falseBranch) {
        this.guard = guard;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    @Override
    public String toString() {
        return ("if (" +
                guard.toString() +
                ") {\n" +
                StmtHelpers.stmtsToStringAutoIndent(trueBranch) +
                "\n} else {\n" +
                StmtHelpers.stmtsToStringAutoIndent(falseBranch) +
                "\n}");
    }
} // IfStmt
