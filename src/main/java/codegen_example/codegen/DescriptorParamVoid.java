package codegen_example.codegen;

public class DescriptorParamVoid implements DescriptorParam {
    public DescriptorParamVoid() {}

    public String toDescriptorStringComponent() throws CodeGeneratorException {
        return "V";
    } // toDescriptorStringComponent
} // DescriptorParamVoid
