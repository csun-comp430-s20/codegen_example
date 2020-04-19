# Compilation Example #

Compiles to JVM bytecode, using the [ASM](https://asm.ow2.io/) bytecode manipulation library.

## Grammar ##

```
x is a variable
i is an integer
b is a boolean
t is a type
e is an expression
op is a binary operator
s is a statement
p is a program
t ::= int | bool
e ::= x | i | b | e1 op e2
op ::= + | - | / | *
s ::= t x = e; | x = e; | print(x);
p ::= s*
```
