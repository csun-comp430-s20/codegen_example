package codegen_example.syntax;

import java.util.List;

public class NewExp implements Exp {
    public final ClassName name;
    public final List<Exp> actualParams;

    public NewExp(final ClassName name,
                  final List<Exp> actualParams) {
        this.name = name;
        this.actualParams = actualParams;
    }
} // NewExp
