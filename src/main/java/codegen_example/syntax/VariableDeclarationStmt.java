package codegen_example.syntax;

public class VariableDeclarationStmt implements Stmt {
    public final Type type;
    public final Variable variable;
    public final Exp exp;

    public VariableDeclarationStmt(final Type type,
                                   final Variable variable,
                                   final Exp exp) {
        this.type = type;
        this.variable = variable;
        this.exp = exp;
    }

    @Override
    public String toString() {
        return (type.toString() +
                " " +
                variable.toString() +
                " = " +
                exp.toString() +
                ";");
    }
} // VariableDeclarationStmt
