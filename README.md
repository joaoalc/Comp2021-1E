# Compilers Project

## Group 1E

- **Name:** Eduardo Correia | **Number:** up201806433 | **Grade:** 18.0 | **Contribution:** 33.3%
- **Name:** João Cardoso | **Number:** up201806531 | **Grade:** 18.0 | **Contribution:** 33.3%
- **Name:** Guilherme Calassi | **Number:** up201800157 | **Grade:** 0.0 | **Contribution:** 0.0%
- **Name:** Telmo Baptista | **Number:** up201806554 | **Grade:** 18.0 | **Contribution:** 33.3%

**Grade of the project:** 18.0

## Summary

This project consists in a compiler, named *jmm*, that translates *Java--* (a language that consists in a subset of
Java) programs into Java bytecodes.

## Dealing with Syntactic Errors

On the while statement, if any error occurs in the condition, body or structure, it won't stop in the first error and will skip tokens to detect more errors after the first one.

## Semantic Analysis

We utilize 2 post-order visitors, `DeclarationVisitor` and `ExpressionVisitor`, in that order, to execute the semantic analysis.

The first one is used for imports and class and method declarations and the second one for the other types of nodes.
We utilize the MySymbolTable class as the symbol table, Method to store each method and ValueSymbol for variables (includes a boolean to see if it's innitialized or not).

Our semantic analyser, upon finding an error, skips the node it is currently analyzing and continues as if there were no
errors. Additionally, it does not have an error limit. It detects errors of 4 different types, which have messages
similiar to those:

- `TypeMismatch`: An expression has at least one argument of the wrong type. Eg: true && 1;
- `UndeclaredVariable`: A variable is used without being declared. Eg: int b; b = a + 1; //a is not a class or method field
- `UndeclaredMethod`: A method is being called that isn't declared, imported nor from a superclass
- `VariableRedefinition`: A variable is redeclared in the same scope. This can be inside the class or inside of a method; However, if one is in the class declaration and the other one is inside the method declaration, this error won't happen.
  Example: `int a; boolean a;` Inside the same method as the other declaration.

Additionally, we also detect some instances of variables being declared but not initialized as a warning.

## Code Generation

We utilize a custom visitor (`ollir/OllirEmitter`) mostly based on a post-order visitor to visit the nodes.

Inside some functions, we utilize a switch case or if statement to generate the code based on the current node's children/parent nodes.

We use 3 class variables: `localVariableCounter`, labelCounter and identCounter. They count, respectively, the current number of auxiliary variables used (to name the variables), the number of if and while loops used (to name the labes) and the indentation level of the current line of code.

### Problems

Because of the way imports (and superclasses) work in the *Java--* language, we do not know the return type or arguments of those functions.

Under normal circunstances, we determine those attributes of the function by the variables used in them/the operations they have

```
import io;
(...)
1 + io.aFunction(true);
```

We assume a function returns `int` and its argument is a `boolean`, since it's being used in a sum and the argument is a boolean.

However, in the scenario where one of those functions' return value is used as an argument of another one of those functions' arguments, we cannot know that type, since it is unknown on "both sides".

```
import io;
(...)
io.functionA(io.functionB);
```

In this scenario, we assume the type to be `void`.

**Note:** This doesn't apply to functions that are declared in the current file.

To generate the Jasmin code, we iterate over the class' methods and correspondent instructions and for each one of these, we have a function that generates its code.

## Task Distribution

- **Eduardo Correia:** Grammar, Jasmin code generation and tests.
- **João Cardoso:** Grammar, Semantic analysis, OLLIR code generation.
- **Telmo Baptista:** Syntatic analysis, Semantic analysis, OLLIR code generation, Optimizations.

## Pros

- Proper synthatic error detection.
- Proper semantic error detection.
- We detect some instances of uninitialized (but declared) variables and add a warning report.
- We have implemented the `-o` optimizations, such as constant propagation and while loop `goto` usage reduction.
- We also implemented constant folding (e.g.: `a = 1 + 1` turns into `a = 2`) and removed unused local variables.
- Our local variable removal properly accounts for function calls, in which case the functions are kept in order while the variable is removed.
- During OLLIR code generation, in, for example, if statements, when negating the conditions, we do not use unecessary auxiliary variables. Eg: `if(!true)` becomes `if(0.bool &&.bool 0.bool)`
- In OLLIR generation, certain keywords (e.g.: array, field) are renamed, in order to not create an OLLIR/jasmin error when they are used.

## Cons

- The code structure of OLLIR generation and semantic analysis is unorganised and impossible to maintain, and it would need a big refactor.
- We weren't able to implement register allocation (-r).

## Compile

To compile the program, run ``gradle build``. This will compile your classes to ``classes/main/java`` and copy the JAR
file to the root directory. The JAR file will have the same name as the repository folder.

### Run ``.jar``

To run the JAR, do the following command:

```cmd
java -jar <jar_filename> [-o] <filepath>
```

Where ``<jar_filename>`` is the name of the JAR file that has been copied to the root folder, `-o` is the optimization flag as described in the project specification, and ``<filepath>`` is the target JMM file's path to compile.

## Test

To test the program, run ``gradle build``. This will execute the build, and run the JUnit tests in the ``test`` folder.
If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``). You can also see a
test report by opening ``build/reports/tests/test/index.html``.

## Run generated `.class` file

To run the compiled program, do the following command:

```cmd
java -cp "./;test/fixtures/libs/compiled" <class>
```

