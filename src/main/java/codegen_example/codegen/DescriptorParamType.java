package codegen_example.codegen;

import codegen_example.syntax.Type;
import codegen_example.syntax.IntType;
import codegen_example.syntax.BoolType;

public class DescriptorParamType implements DescriptorParam {
    public final Type type;

    public DescriptorParamType(final Type type) {
        this.type = type;
    }

    public String toDescriptorStringComponent() throws CodeGeneratorException {
        if (type instanceof IntType) {
            return "I";
        } else if (type instanceof BoolType) {
            return "Z";
        } else {
            assert(false);
            throw new CodeGeneratorException("Unrecognized type: " + type.toString());
        }
    } // toDescriptorStringComponent
} // DescriptorParamType
