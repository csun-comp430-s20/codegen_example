package codegen_example.codegen;

import java.util.List;
import java.util.ArrayList;

import codegen_example.syntax.Type;
import codegen_example.syntax.Function;
import codegen_example.syntax.FormalParam;

public class DescriptorString {
    public final String descriptorString;

    private DescriptorString(final String descriptorString) {
        this.descriptorString = descriptorString;
    }

    public static DescriptorString makeDescriptorString(final List<DescriptorParam> params,
                                                        final DescriptorParam returnValue)
        throws CodeGeneratorException {
        final StringBuffer result = new StringBuffer();
        result.append("(");
        for (DescriptorParam param : params) {
            result.append(param.toDescriptorStringComponent());
        }
        result.append(")");
        result.append(returnValue.toDescriptorStringComponent());
        return new DescriptorString(result.toString());
    } // makeDescriptorString

    public static List<DescriptorParam> paramsToDescriptors(final List<FormalParam> formalParams) {
        final List<DescriptorParam> result = new ArrayList<DescriptorParam>();
        for (final FormalParam formalParam : formalParams) {
            result.add(new DescriptorParamType(formalParam.type));
        }
        return result;
    } // paramsToDescriptors
    
    public static DescriptorString makeDescriptorString(final Function function)
        throws CodeGeneratorException {
        return makeDescriptorString(paramsToDescriptors(function.formalParams),
                                    new DescriptorParamType(function.returnType));
    } // makeDescriptorString
} // DescriptorString
