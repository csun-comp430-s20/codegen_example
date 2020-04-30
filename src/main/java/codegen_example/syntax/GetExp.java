package codegen_example.syntax;

public class GetExp implements Exp {
    public final Exp target;
    public final ClassName name;
    public final Variable field;

    public GetExp(final Exp target,
                  final ClassName name,
                  final Variable field) {
        this.target = target;
        this.name = name;
        this.field = field;
    }
} // GetExp
