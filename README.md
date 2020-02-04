# jAADD 
This repository contains the jAADD library. 
It implements *Affine Arithmetic Decision Diagrams* (AADD) in Java. 
AADD permit the symbolic execution of Java or Kotlin programs, 
in particularof signal processing software.
Other languages for the JVM platform might work as well. 

AADD are a combination of reduced, ordered BDD that model discrete
conditions and Affine Forms that model computations on reals. 
Both interact via predicates.

The environment for development is: 
- Java 11 or newer
- Gradle as build tool.  
- Junit 5 for unit testing.
- Apache common math library for solving LP problems.

For the very urgent: 
```
cd jAADD
./gradlew build 
./gradlew fatJar
```
jAADD.jar and jAADD-all.jar will be in build/libs. 

### BDD and its use

To create leaves of a BDD, one has to use the method `BDDLeaf(boolean)`, 
or by directly using the leaves `BDD.ONE` or `BDD.ZERO`.
Internal nodes refer to a condition that is described by an Affine Form. 
Each level of a BDD refers to a level that can be accessed by an index.
Different BDD with same index share the conditions. 
The last condition has the index maxindex.
Using it in a constructor allows to share conditions among two BDD. 
The following example creates a BDD with 3 leaves and one internal node: 
```
AffineForm cond = new AffineForm(1.0, 2.0, 1); // [1,2] >= 0 using symbol 1.
BDD a = new BDD(cond, BDD.ONE, BDD.ZERO);
BDD b = new BDD(lastindex, BDD.ZERO, BDD.ONE);
```
To compute with BDD, one can use the boolean operations as defined 
in the class definition. 
```
BDD r = a.and(b);
```

### AADD and its use

AADD are used Like BDD.
However, they support arithmetic operations such as add, sub, mult, etc. 
The ITE function permits interaction between AADD (Arithmetics) and BDD (Logic). 

Check AADDTest.java to see how to use arithmetic operations, compare two AADD and
how to compute bounds of generated AADD.
 

### Installation of dependencies for developers

There are the following dependencies: 
* jUnit Jupiter; best way is to let Gradle or IntelliJ install it. 
This happens automatically in Gradle and in IntelliJ if you click in the source code and try to add a new test.
Then, IntelliJ will offer you to install it, if not yet installed. 


### Installation of dependencies for projects
The following dependencies need to be considered: 
* Apache math lib. 
You will have to add both dependencies in your project. 
Also put the jAADD.jar file in your classpath.
In gradle, copy a jAADD.jar to a folder libs and include a dependency do it: 

```
dependencies {
    compile files('libs/jAADD.jar')
}
```
In case of problems or other feedback contact:

Carna Zivkovic  
Chair of Design of Cyber-Physical Systems  
TU Kaiserslautern  
Postfach 3049   
67653 Kaiserslautern  
zivkovic@cs.uni-kl.de  
https://cps.cs.uni-kl.de/mitarbeiter/carna-radojicic-msc/  

Christoph Grimm  
Chair of Design of Cyber-Physical Systems  
TU Kaiserslautern  
Postfach 3049  
67653 Kaiserslautern  
grimm@cs.uni-kl.de  
