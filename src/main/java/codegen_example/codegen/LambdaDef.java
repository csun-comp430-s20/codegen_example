package codegen_example.codegen;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

import codegen_example.syntax.*;

public class LambdaDef {
    public final ClassName className;
    public final List<FormalParam> instanceVariables;
    public final Variable param;
    public final ReferenceType paramType;
    public final ReferenceType returnType;
    public final Exp body;
    public final List<FormalParam> formalParams;
    
    public LambdaDef(final ClassName className,
                     final List<FormalParam> instanceVariables,
                     final Variable param,
                     final ReferenceType paramType,
                     final ReferenceType returnType,
                     final Exp body) {
        this.className = className;
        this.instanceVariables = instanceVariables;
        this.param = param;
        this.paramType = paramType;
        this.returnType = returnType;
        this.body = body;
        formalParams = new ArrayList<FormalParam>();
        formalParams.add(new FormalParam(paramType, param));
    } // LambdaDef

    public String toSignatureString() {
        return (new ReferenceType(new ClassName(ClassGenerator.objectName)).toSignatureString() +
                new LambdaType(paramType, returnType).toSignatureString());
    } // toSignatureString

    public String constructorDescriptorString() {
        return Callable.toDescriptorString(instanceVariables,
                                           new VoidType());
    } // constructorDescriptorString

    public String fieldDescriptorString(final Variable fieldName) throws CodeGeneratorException {
        for (final FormalParam instanceVar : instanceVariables) {
            if (instanceVar.variable.equals(fieldName)) {
                return instanceVar.type.toDescriptorString();
            }
        }
        throw new CodeGeneratorException("Unknown field: " + fieldName);
    } // fieldDescriptorString
        
    public String typedApplyDescriptorString() {
        return Callable.toDescriptorString(formalParams, returnType);
    } // typedApplyDescriptorString

    public static String bridgeApplyDescriptorString() {
        final List<FormalParam> objectFormalParams = new ArrayList<FormalParam>();
        final ReferenceType objectType =
            new ReferenceType(new ClassName(ClassGenerator.objectName));
        objectFormalParams.add(new FormalParam(objectType, new Variable("")));
        return Callable.toDescriptorString(objectFormalParams, objectType);
    } // bridgeApplyDescriptorString
    
    public void writeConstructor(final ClassWriter classWriter) {
        final MethodVisitor methodVisitor =
            classWriter.visitMethod(ACC_PUBLIC,
                                    "<init>",
                                    constructorDescriptorString(),
                                    null,
                                    null);
        methodVisitor.visitCode();
        // ---BEGIN CODE FOR SUPER---
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL,
                                      ClassGenerator.objectName,
                                      "<init>",
                                      "()V",
                                      false);
        // ---END CODE FOR SUPER---
        int variableIndex = 1;
        // this[cn].x = x
        for (final FormalParam instanceVariable : instanceVariables) {
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, variableIndex++);
            methodVisitor.visitFieldInsn(PUTFIELD,
                                         className.name,
                                         instanceVariable.variable.name,
                                         instanceVariable.type.toDescriptorString());
        }
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
    } // writeConstructor

    public void writeTypedApply(final ClassWriter classWriter,
                                final Map<ClassName, ClassDefinition> allClasses,
                                final LambdaMaker lambdaMaker)
        throws CodeGeneratorException {
        final MethodVisitor methodVisitor =
            classWriter.visitMethod(ACC_PUBLIC,
                                    LambdaMaker.APPLY_NAME.name,
                                    typedApplyDescriptorString(),
                                    null,
                                    null);
        methodVisitor.visitCode();
        final ExpressionStatementGenerator gen =
            new ExpressionStatementGenerator(allClasses,
                                             lambdaMaker,
                                             VariableTable.withFormalParams(new ReferenceType(className),
                                                                            formalParams),
                                             methodVisitor);
        gen.writeExpression(body);
        gen.doReturn(returnType);
        methodVisitor.visitMaxs(0, 0);
    } // writeTypedApply

    public void writeBridgeApply(final ClassWriter classWriter,
                                 final Map<ClassName, ClassDefinition> allClasses)
        throws CodeGeneratorException {
        final MethodVisitor methodVisitor =
            classWriter.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC | ACC_BRIDGE,
                                    LambdaMaker.APPLY_NAME.name,
                                    bridgeApplyDescriptorString(),
                                    null,
                                    null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitTypeInsn(CHECKCAST,
                                    paramType.refersTo.name);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
                                      className.name,
                                      LambdaMaker.APPLY_NAME.name,
                                      typedApplyDescriptorString(),
                                      false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(0, 0);
    } // writeBridgeApply
} // LambdaDef
