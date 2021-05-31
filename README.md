# Compilers Project

## Group 1E

- **Name:** Eduardo Correia | **Number:** up201806433 | **Grade:** | **Contribution:** 33.3%
- **Name:** João Cardoso | **Number:** up201806531 | **Grade:** | **Contribution:** 33.3%
- **Name:** Guilherme Calassi | **Number:** up201800157 | **Grade:** | **Contribution:** 0.0%
- **Name:** Telmo Baptista | **Number:** up201806554 | **Grade:** | **Contribution:** 33.3%

**Grade of the project:** 17.5

## Summary

This project consists in a compiler, named *jmm*, that translates *Java--* (a language that consists in a subset of
Java) programs into Java bytecodes.

## Dealing with Syntactic Errors

Describe how the syntactic error recovery of your tool works. Does it exit after the first error?

## Semantic Analysis
Our semantic analyser, upon finding an error, skips the node it is currently analyzing and continues as if there were no errors.
Additionally, it does not have an error limit.
It detects errors of 4 different types, which have messages similiar to those :
	TypeMismatch: An expression has at least one argument of the wrong type.
		Eg: true && 1;
	UndeclaredVariable: A variable is used without being declared.
		Eg: 
			int b;
			b = a + 1; //a is not a class or method field
	UndeclaredMethod: A method is being called that isn't declared, imported or from a superclass

	VariableRedefinition: A variable is redeclared in the same scope. This can be inside the class or inside of a method; However, if one is inside
		Eg:
			int a;
			boolean a; //Inside the same method as the other declaration.
Additionally, we also detect some instances of variables being declared but not initialized as a warning.
## Code Generation

Describe how the code generation of your tool works and identify the possible problems your tool has regarding code
generation.)

## Task Distribution

Identify the set of tasks done by each member of the project. You can divide this by checkpoint it if helps)

## Pros

Our tool allows for function overloading.
We have implemented the -o optimizations (constant propagation and while loop goto usage reduction).
We also implemented constant folding (eg: a = 1+1; turns into a = 2;) and removed unused local variables).
Proper synthatic error detection.
Proper semantic error detection.


## Cons

Identify the most negative aspects of your tool.

## Compile

To compile the program, run ``gradle build``. This will compile your classes to ``classes/main/java`` and copy the JAR
file to the root directory. The JAR file will have the same name as the repository folder.

### Run

To run you have two options: Run the ``.class`` files or run the JAR.

### Run ``.class``

To run the ``.class`` files, do the following:

```cmd
java -cp "./build/classes/java/main/" <class_name> <arguments>
```

Where ``<class_name>`` is the name of the class you want to run and ``<arguments>`` are the arguments to be passed
to ``main()``.

### Run ``.jar``

To run the JAR, do the following command:

```cmd
java -jar <jar filename> <arguments>
```

Where ``<jar filename>`` is the name of the JAR file that has been copied to the root folder, and ``<arguments>`` are
the arguments to be passed to ``main()``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder.
If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``). You can also see a
test report by opening ``build/reports/tests/test/index.html``.
