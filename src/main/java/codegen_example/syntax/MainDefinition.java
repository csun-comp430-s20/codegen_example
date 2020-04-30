package codegen_example.syntax;

import java.util.List;
import java.util.ArrayList;

public class MainDefinition extends Callable {
    public MainDefinition(final List<Stmt> body) {
        super(new MethodName("main"),
              new ArrayList<FormalParam>(),
              body,
              new VoidType());
    }

    // TODO: very hacky
    @Override
    public String toDescriptorString() {
        return "([Ljava/lang/String;)V";
    }
} // MainDefinition
