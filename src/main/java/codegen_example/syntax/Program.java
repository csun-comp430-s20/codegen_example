package codegen_example.syntax;

import java.util.List;

public class Program {
    public final List<Function> functions;
    public final List<Stmt> entryPoint;

    public Program(final List<Function> functions,
                   final List<Stmt> entryPoint) {
        this.functions = functions;
        this.entryPoint = entryPoint;
    }
} // Program
