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
f is a function
fn is a function name
fp is a formal parameter
p is a program

t ::= int | bool
e ::= x | i | b | e1 op e2 | fn(e*)
op ::= + | - | / | * | < | ==
s ::= t x = e; | x = e; | print(x); |
      if (e) { s* } else { s* } |
      while (e) { s* }
fp ::= t x
f ::= t fn(fp*) { s* return e; }
p ::= f* s*
```
