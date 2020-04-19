package codegen_example.syntax;

public class PrintStmt implements Stmt {
    public final Variable variable;

    public PrintStmt(final Variable variable) {
        this.variable = variable;
    }

    @Override
    public String toString() {
        return "print(" + variable.toString() + ");";
    }
} // PrintStmt
