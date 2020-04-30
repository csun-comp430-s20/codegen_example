package codegen_example.codegen;

import codegen_example.syntax.Variable;
import codegen_example.syntax.FormalParam;
import codegen_example.syntax.Callable;
import codegen_example.syntax.Type;
import codegen_example.syntax.ReferenceType;

import java.util.Map;
import java.util.HashMap;

public class VariableTable {
    private final Map<Variable, VariableEntry> variables;
    private int nextIndex;
    
    public VariableTable() {
        variables = new HashMap<Variable, VariableEntry>();
        nextIndex = 0;
    }

    public VariableEntry addEntry(final FormalParam formalParam)
        throws CodeGeneratorException {
        return addEntry(formalParam.variable, formalParam.type);
    } // addEntry
    
    public VariableEntry addEntry(final Variable variable,
                                  final Type type) throws CodeGeneratorException {
        if (variables.containsKey(variable)) {
            // should be caught by typechecker
            throw new CodeGeneratorException("Variable already in scope: " + variable);
        } else {
            final VariableEntry entry = new VariableEntry(variable, type, nextIndex++);
            variables.put(variable, entry);
            return entry;
        }
    } // addEntry

    public VariableEntry getEntryFor(final Variable variable) throws CodeGeneratorException {
        final VariableEntry entry = variables.get(variable);
        if (entry != null) {
            return entry;
        } else {
            // should be caught by typechecker
            throw new CodeGeneratorException("no such variable declared: " + variable);
        }
    } // getEntryFor

    public static VariableTable withFormalParamsFrom(final ReferenceType thisType,
                                                     final Callable callable)
        throws CodeGeneratorException {
        final VariableTable table = new VariableTable();
        table.addEntry(new Variable("this"), thisType);
        for (final FormalParam formalParam : callable.formalParams) {
            table.addEntry(formalParam);
        }
        return table;
    } // withFormalParamsFrom
} // VariableTable

            
