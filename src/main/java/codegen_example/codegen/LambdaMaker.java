package codegen_example.codegen;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.IOException;

import org.objectweb.asm.ClassWriter;
import static org.objectweb.asm.Opcodes.*;

import codegen_example.syntax.*;

public class LambdaMaker {
    // ---BEGIN CONSTANTS---
    public static final String LAMBDA_PREFIX = "Lambda";
    public static final ClassName EXTENDS_NAME = new ClassName("Function1");
    public static final MethodName APPLY_NAME = new MethodName("apply");
    // ---END CONSTANTS---

    // ---BEGIN INSTANCE VARIABLES---
    private final Map<ClassName, ClassDefinition> allClasses;
    private final List<LambdaDef> additionalClasses;
    private int curLambda = 0;
    // ---END INSTANCE VARIABLES---

    public LambdaMaker(final Map<ClassName, ClassDefinition> allClasses) {
        this.allClasses = allClasses;
        additionalClasses = new ArrayList<LambdaDef>();
    } // LambdaMaker
    
    public static <A> Set<A> setUnion(final Set<A> first, final Set<A> second) {
        final HashSet<A> result = new HashSet<A>(first);
        result.addAll(second);
        return result;
    } // setUnion
    
    public static <A> Set<A> addSet(final Set<A> set, final A element) {
        if (set.contains(element)) {
            return set;
        } else {
            final HashSet<A> result = new HashSet<A>(set);
            result.add(element);
            return result;
        }
    } // addSet

    public static Set<Variable> freeVariables(final Set<Variable> params, final List<Exp> exps)
        throws CodeGeneratorException {
        final Set<Variable> result = new HashSet<Variable>();
        for (final Exp exp : exps) {
            result.addAll(freeVariables(params, exp));
        }
        return result;
    } // freeVariables
            
    public static Set<Variable> freeVariables(final Set<Variable> params, final Exp exp)
        throws CodeGeneratorException {
        if (exp instanceof VariableExp) {
            final Variable variable = ((VariableExp)exp).variable;
            if (!params.contains(variable)) {
                return addSet(new HashSet<Variable>(), variable);
            } else {
                return new HashSet<Variable>();
            }
        } else if (exp instanceof IntegerLiteralExp ||
                   exp instanceof BooleanLiteralExp) {
            return new HashSet<Variable>();
        } else if (exp instanceof BinopExp) {
            final BinopExp asBinop = (BinopExp)exp;
            return setUnion(freeVariables(params, asBinop.left),
                            freeVariables(params, asBinop.right));
        } else if (exp instanceof MethodCallExp) {
            final MethodCallExp asMethodCall = (MethodCallExp)exp;
            return setUnion(freeVariables(params, asMethodCall.callOn),
                            freeVariables(params, asMethodCall.actualParams));
        } else if (exp instanceof NewExp) {
            return freeVariables(params, ((NewExp)exp).actualParams);
        } else if (exp instanceof GetExp) {
            return freeVariables(params, ((GetExp)exp).target);
        } else if (exp instanceof LambdaExp) {
            final LambdaExp asLambda  = (LambdaExp)exp;
            return freeVariables(addSet(params, asLambda.param),
                                 asLambda.body);
        } else if (exp instanceof LambdaCallExp) {
            final LambdaCallExp asLambda = (LambdaCallExp)exp;
            return setUnion(freeVariables(params, asLambda.lambda),
                            freeVariables(params, asLambda.param));
        } else {
            assert(false);
            throw new CodeGeneratorException("Unknown expression: " + exp);
        }
    } // freeVariables

    public static Set<Variable> freeVariables(final LambdaExp lambdaExp)
        throws CodeGeneratorException {
        return freeVariables(addSet(new HashSet<Variable>(),
                                    lambdaExp.param),
                             lambdaExp.body);
    } // freeVariables

    public LambdaDef classDefinitionFor(final ClassName name) throws CodeGeneratorException {
        for (final LambdaDef lambdaDef : additionalClasses) {
            if (lambdaDef.className.equals(name)) {
                return lambdaDef;
            }
        }
        throw new CodeGeneratorException("No such class: " + name);
    } // classDefinitionFor
    
    public String constructorDescriptorFor(final ClassName name) throws CodeGeneratorException {
        return classDefinitionFor(name).constructorDescriptorString();
    } // constructorDescriptorFor

    public String fieldDescriptorFor(final ClassName className,
                                     final Variable fieldName) throws CodeGeneratorException {
        return classDefinitionFor(className).fieldDescriptorString(fieldName);
    } // fieldDescriptorFor

    private List<Exp> translateLambdaBodies(final List<Exp> bodies,
                                            final Variable lambdaParam,
                                            final ReferenceType lambdaParamType,
                                            final ClassName lambdaClass)
        throws CodeGeneratorException {
        final List<Exp> result = new ArrayList<Exp>();
        for (final Exp exp : bodies) {
            result.add(translateLambdaBody(exp,
                                           lambdaParam,
                                           lambdaParamType,
                                           lambdaClass));
        }
        return result;
    } // translateLambdaBodies
    
    // anything that's not the lambda param must be captured on the lambda,
    // meaning we can do this[lambdaClass].x
    private Exp translateLambdaBody(final Exp body,
                                    final Variable lambdaParam,
                                    final ReferenceType lambdaParamType,
                                    final ClassName lambdaClass)
        throws CodeGeneratorException {
        if (body instanceof VariableExp) {
            final Variable theVar = ((VariableExp)body).variable;
            if (theVar.equals(lambdaParam)) {
                return body;
            } else {
                // captured in the lambda class
                return new GetExp(new VariableExp(ClassGenerator.thisVariable),
                                  lambdaClass,
                                  theVar);
            }
        } else if (body instanceof IntegerLiteralExp ||
                   body instanceof BooleanLiteralExp) {
            return body;
        } else if (body instanceof BinopExp) {
            final BinopExp asBinop = (BinopExp)body;
            return new BinopExp(translateLambdaBody(asBinop.left,
                                                    lambdaParam,
                                                    lambdaParamType,
                                                    lambdaClass),
                                asBinop.bop,
                                translateLambdaBody(asBinop.right,
                                                    lambdaParam,
                                                    lambdaParamType,
                                                    lambdaClass));
        } else if (body instanceof MethodCallExp) {
            final MethodCallExp asMethodCall = (MethodCallExp)body;
            return new MethodCallExp(translateLambdaBody(asMethodCall.callOn,
                                                         lambdaParam,
                                                         lambdaParamType,
                                                         lambdaClass),
                                     asMethodCall.callOnName,
                                     asMethodCall.name,
                                     translateLambdaBodies(asMethodCall.actualParams,
                                                           lambdaParam,
                                                           lambdaParamType,
                                                           lambdaClass));
        } else if (body instanceof NewExp) {
            final NewExp asNew = (NewExp)body;
            return new NewExp(asNew.name,
                              translateLambdaBodies(asNew.actualParams,
                                                    lambdaParam,
                                                    lambdaParamType,
                                                    lambdaClass));
        } else if (body instanceof GetExp) {
            final GetExp asGet = (GetExp)body;
            return new GetExp(translateLambdaBody(asGet.target,
                                                  lambdaParam,
                                                  lambdaParamType,
                                                  lambdaClass),
                              asGet.name,
                              asGet.field);
        } else if (body instanceof LambdaExp) {
            // TODO: this needs a table of variables in scope.
            // I *think* this should just be `this`, but that means that
            // we need to properly handle naming `this`
            return translateLambda((LambdaExp)body,
                                   VariableTable.withFormalParam(new ReferenceType(lambdaClass),
                                                                 lambdaParamType,
                                                                 lambdaParam));
        } else if (body instanceof LambdaCallExp) {
            final LambdaCallExp asCall = (LambdaCallExp)body;
            return new LambdaCallExp(translateLambdaBody(asCall.lambda,
                                                         lambdaParam,
                                                         lambdaParamType,
                                                         lambdaClass),
                                     asCall.returnType,
                                     translateLambdaBody(asCall.param,
                                                         lambdaParam,
                                                         lambdaParamType,
                                                         lambdaClass));
        } else {
            assert(false);
            throw new CodeGeneratorException("Unrecognized expression: " + body);
        }
    } // translateLambdaBody

    private void writeLambda(final LambdaDef lambdaDef, final String toDirectory)
        throws CodeGeneratorException, IOException {
        final ClassWriter classWriter =
            new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classWriter.visit(V1_7,
                          ACC_PUBLIC,
                          lambdaDef.className.name,
                          lambdaDef.toSignatureString(),
                          ClassGenerator.objectName,
                          new String[]{ EXTENDS_NAME.name });
        ClassGenerator.writeInstanceVariables(classWriter,
                                              lambdaDef.instanceVariables);
        lambdaDef.writeConstructor(classWriter);
        lambdaDef.writeTypedApply(classWriter, allClasses, this);
        lambdaDef.writeBridgeApply(classWriter, allClasses);
        classWriter.visitEnd();

        ClassGenerator.writeClass(classWriter,
                                  new File(toDirectory,
                                           lambdaDef.className.name + ".class"));
    } // writeLambda
    
    // (int x) => (int y) => x + y
    // 
    // public class Lambda1 implements Function1<int, Function1<int, int>> {
    //   public Lambda1() { super(); }
    //   public Function1<int, int> apply(int x) {
    //     return new Function1<int, int>(this[Lambda1].x);
    //   }
    // }
    // public class Lambda2 implements Function1<int, int> {
    //   int x;
    //   public Lambda2(int x) {
    //     super();
    //     this.x = x;
    //   }
    //   public int apply(int y) {
    //     return this[Lambda2].x + y;
    //   }
    // }
    //
    // new Function1()
    //
    // int x = 0; // in class Foo
    // (int y) => x + y
    //
    // public class Lambda1 implements Function1<int, int> {
    //   int x;
    //   public Lambda1(int x) {
    //     this.x = x;
    //   }
    //   public int apply(int y) {
    //     return this[Lambda1].x + y;
    //   }
    // }
    //
    // [in class with instance variable x]
    // (int y) => (int z) => this[MyClass].x + y + z
    //
    // public class Lambda1 implements Function1<int, Function1<int, int>> {
    //   MyClass ~this;
    //   public Lambda1(MyClass ~this) {
    //     this.~this = ~this;
    //   }
    //   public Function1<int, int> apply(int y) {
    //     return new Lambda2(~this, y);
    //   }
    // }
    // public class Lambda2 implements Function1<int, int> {
    //   MyClass ~this;
    //   int y;
    //   public Lambda2(MyClass ~this, int y) {
    //     this.~this = this;
    //     this.y = y;
    //   }
    //  public int apply(int z) {
    //    return this.~this.x + this.y + z;
    //  }
    // }
    
    // create a new class that extends Function1.  It has instance variables
    // for each captured variable.  The apply method uses the parameters provided
    // along with anything captured.
    //
    public NewExp translateLambda(final LambdaExp lambdaExp,
                                  final VariableTable table)
        throws CodeGeneratorException {
        final ClassName outputClassName = new ClassName(LAMBDA_PREFIX + (curLambda++));
        final Set<Variable> needToCapture = LambdaMaker.freeVariables(lambdaExp);
        final List<FormalParam> instanceVariables = new ArrayList<FormalParam>();

        for (final Variable x : needToCapture) {
            final Type varType = table.getEntryFor(x).type;
            instanceVariables.add(new FormalParam(varType, x));
        }

        final LambdaDef lambdaDef =
            new LambdaDef(outputClassName,
                          instanceVariables,
                          lambdaExp.param,
                          lambdaExp.paramType,
                          lambdaExp.returnType,
                          translateLambdaBody(lambdaExp.body,
                                              lambdaExp.param,
                                              lambdaExp.paramType,
                                              outputClassName));
        additionalClasses.add(lambdaDef);

        final List<Exp> newParams = new ArrayList<Exp>();
        for (final FormalParam instanceVariable : instanceVariables) {
            newParams.add(new VariableExp(instanceVariable.variable));
        }
        
        // TODO: if `this` is captured, this won't work right.  We need to rename `this`.
        return new NewExp(outputClassName, newParams);
    } // translateLambda

    public void writeLambdas(final String toDirectory) throws CodeGeneratorException, IOException {
        for (final LambdaDef lambdaDef : additionalClasses) {
            writeLambda(lambdaDef, toDirectory);
        }
    } // writeLambdas

    // intended for testing.  Deletes all created classes in the given directory
    public void deleteClasses(final String inDirectory) {
        for (final LambdaDef lambdaDef : additionalClasses) {
            new File(inDirectory, lambdaDef.className.name + ".class").delete();
        }
    } // deleteClasses
} // LambdaMaker
