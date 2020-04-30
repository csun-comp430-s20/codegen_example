# Compilation Example #

Compiles to JVM bytecode, using the [ASM](https://asm.ow2.io/) bytecode manipulation library.

If you're interested in seeing a bigger language which compiles to MIPS assembly, see [this example (and its branches)](https://github.com/csun-comp430-s19/codegen-expressions-example).

## Grammar ##

```
x is a variable
i is an integer
b is a boolean
t is a type
e is an expression
op is a binary operator
s is a statement
m is a method
mn is a method name
fp is a formal parameter
cn is a class name
c is a class
p is a program

t ::= int | bool
e ::= x | i | b | e1 op e2 | e[cn].mn(e*) | new cn(e*) |
      e[cn].x
op ::= + | - | / | * | < | ==
s ::= t x = e; | x = e; | print(x); |
      if (e) { s* } else { s* } |
      while (e) { s* } |
      e1[cn].x = e2
fp ::= t x
m ::= t mn(fp*) { s* return x; }
c ::= class cn extends cn {
        fp*
        init(fp*) {
          super(e*);
          s*
        }
        main {
          s*
        }
        m*
     }
p ::= c*
```

For all uses of `e[cn]`, the `cn` is only needed because we don't have a typechecker to tell us what the type of `e` is.
With a proper typechecker, we'd have that information available.

`this` is just a variable that happens to be in scope in constructors and methods.
