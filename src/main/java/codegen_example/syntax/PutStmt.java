package codegen_example.syntax;

public class PutStmt implements Stmt {
    public final Exp target;
    public final ClassName name;
    public final Variable field;
    public final Exp putHere;

    public PutStmt(final Exp target,
                   final ClassName name,
                   final Variable field,
                   final Exp putHere) {
        this.target = target;
        this.name = name;
        this.field = field;
        this.putHere = putHere;
    }
} // PutStmt

