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

Describe how the syntactic error recovery of your tool works. Does it exit after the first error?

## Semantic Analysis

Refer the semantic rules implemented by your tool.
Our tool's semantic analysis stage detects errors of 4 different types:
	TypeMismatch: An expression has at least one argument of the wrong type.
		Eg: true && 1;
	UndeclaredVariable: A variable is used without being declared.
		Eg: 
			int b;
			b = a + 1; //a is not a class or method field
	UndeclaredMethod: A method is being called that isn't declared, imported or from a superclass
		Eg: 
			

## Code Generation

Describe how the code generation of your tool works and identify the possible problems your tool has regarding code
generation.

## Task Distribution

- **Eduardo Correia:** Grammar, Jasmin code generation and tests. 
- **João Cardoso:** Grammar, Semantic analysis, OLLIR code generation.
- **Telmo Baptista:** Semantic analysis, OLLIR code generation.

## Pros

Identify the most positive aspects of your tool

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
