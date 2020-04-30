package codegen_example.syntax;

import java.util.List;

public class ClassDefinition {
    public final ClassName name;
    public final ClassName extendsName;
    public final List<FormalParam> instanceVariables;
    public final Constructor constructor;
    public final MainDefinition main;
    public final List<MethodDefinition> methods;

    public ClassDefinition(final ClassName name,
                           final ClassName extendsName,
                           final List<FormalParam> instanceVariables,
                           final Constructor constructor,
                           final MainDefinition main,
                           final List<MethodDefinition> methods) {
        this.name = name;
        this.extendsName = extendsName;
        this.instanceVariables = instanceVariables;
        this.constructor = constructor;
        this.main = main;
        this.methods = methods;
    }
} // ClassDefinition
    
