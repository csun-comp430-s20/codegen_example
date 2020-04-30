package codegen_example.syntax;

import java.util.List;

public class Constructor extends Callable {
    public final List<Exp> superParams;

    public Constructor(final List<FormalParam> formalParams,
                       final List<Exp> superParams,
                       final List<Stmt> body) {
        super(new MethodName("<init>"),
              formalParams,
              body,
              new VoidType());
        this.superParams = superParams;
    } // Constructor
} // Constructor
