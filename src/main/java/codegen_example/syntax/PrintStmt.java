package codegen_example.syntax;

public class PrintStmt implements Stmt {
    public final Exp exp;

    public PrintStmt(final Exp exp) {
        this.exp = exp;
    }
} // PrintStmt
