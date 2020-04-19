package codegen_example.syntax;

public class AssignStmt implements Stmt {
    public final Variable variable;
    public final Exp exp;
    
    public AssignStmt(final Variable variable,
                      final Exp exp) {
        this.variable = variable;
        this.exp = exp;
    }
} // AssignStmt
