# jAADD v3.0


V3 of jAADD will be published soon.
Here.
V3 separates textual expressions from the pure computational library and will offer the following features; 
hence, V3 will be more compact. 

- Re-worked API of base classes to completely get rid of nullable fields. 
    + A node is EITHER a leaf (with no childs) OR an internal with true and false childs. 
    + Code is statically checked by Kotlin using sealed class + case statements.
- Support for reals (AADD), integers (IDD), strings (StrDD), booleans (BDD).
- Smart garbage collection for noise symbols. 
- Merging of AADD/IDD improved. 
- Combined use of interval arithmetics and affine arithmetics.
- Consideration of FP roundoff errors. 
    
- The expression language will be in a separate repository SysMD that adds support for
    + creating feature models and constraint networks in SysMLv2 inspired language, based on subset of its meta-model.
    + bi-directional computations. 
    + support for real-valued quantities with SI and national units.
    + SysMD will as well be published under /tukcps, but in separate repository. 


# jAADD v2.0
This repository contains the jAADD library in version 2.0.
It implements *Affine Arithmetic Decision Diagrams* (AADD) for Java and Kotlin.
AADD are a combination of reduced, ordered BDD that model discrete
conditions and Affine Forms that model computations on reals.
Both interact via conditions resp. the ITE function.


The environment for development is:
- Kotlin v1.3+ with coroutines.
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

To create leaves of a BDD, one has to use the method `BDD.constant(boolean)`,
or by directly using the constants `BDD.False` or `BDD.True`.
Internal nodes refer to a condition that is described by an Affine Form.
Each level of a BDD refers to a level that can be accessed by an index.
Different BDD with same index share the conditions.
The last condition has the index maxindex.
Using it in a constructor allows us to share conditions among two BDD.
The following example shows how to create BDD objects in Java or Kotlin:
```
var f = BDD.False            // leaf with value false
var t = BDD.True             // leaf with value true
var a = BDD.constant(true);  // leaf with Boolean value defined by parameter
var X = BDD.variable("X")    // internal node depending on decision variable "X"
```
To compute with BDD, one can use Boolean operations, i.e. an, or, not, as defined
in the class definition:  
```
var d = f.and(X).or(t);
var e = t.and(BDD.variable("X"));
```
or, in Kotlin:
```
var d = f and X or t;
var e = t and BDD.variable("X");
```
Note that jAADD's BDD implementation is just intended to complement jAADD. 
If you need only BDD, there are much better BDD packages available, 
e.g. JavaBDD (http://javabdd.sourceforge.net). 

### AADD and its use

AADD are used like BDD: instantiate AADD, and apply operations.
However, they support arithmetic operations such as plus, minus, times, etc.
For example, to compute the volume of an ellipsoid = 4/3*pi*a*b*c where a,b,c are
each from independent ranges [1,10], we declare AADD as follows:
```
var a  = AADD.range(1.0, 10.0, 1);   // last parameter: noise symbol index
var b  = AADD.range(1.0, 10.0, 2);
var c  = AADD.range(1.0, 10.0, 3);
var pi = AADD.range(3.14, 3.15, "inaccuracy of pi"); // or commenting string
```
The computation and printing the result is then done e.g. as follows:
```
var vol = pi.times(a.times(b.times(c)));
    vol = vol.times(AADD.scalar(4.0/3.0));
System.out.println("Volume = " + vol);
```
or, shorter in Kotlin with overloaded operators:
```
var vol = scalar(4.0/3.0)*pi*a*b*c
println("Volume =" + vol)
```

The ITE function permits the interaction between AADD (Arithmetics) and BDD (Logic).
Check the AADDTutorial.java to see how to use arithmetic operations, compare two AADD and
how to compute bounds of generated AADD.

### AADD and BDD combined

Imagine the following program:
```
var a = AADD.range(-1.0, 1.0)
if (a > 0.0)
    a=a+10.0
else
    a = a-10.0
println("a = "+a)
```
With jAADD, we can symbolically execute it by using ifS, elseS, endS and assignS:
```
var a = AADD.range(-1.0, 1.0)
ifS (a Ge scalar(0.0))
    a=a.assignS(a+scalar(10.0))
elseS
    a=a.assignS(a-scalar(10.0))
endS    
println("a = "+a)
```

### Installation of dependencies for developers

There are the following dependencies, all already in the gradle configuration,
but maybe good to know:

* jUnit Jupiter; best way is to let Gradle or IntelliJ install it.
* kotlinx.coroutines; it is already in the Gradle dependencies
* Apache math lib; it is already in the Gradle dependencies

This happens automatically in Gradle and in IntelliJ if you click in the source code and try to add a new test.
Then, IntelliJ will offer you to install it, if not yet installed.


### Installation of dependencies for projects that use jAADD
The following dependencies need to be considered and are configured in the Gradle configuration:

* Apache math lib.
You will have to add Apache math to your project.
Also put the jAADD.jar file in your classpath.
In gradle, copy a jAADD.jar to a folder libs and include a dependency do it:

* xgraph library for visualization of graphs.

```
dependencies {
    compile files('libs/jAADD.jar')
}
```

### API Changes for version 2.0 (Kotlin interoperability)
The version 2.0 comes with some modifications in the API
to permit interoperability with Kotlin, or to clean up the API.
The following are the changes:

1. Use of getter/setter methods and adapted names following Java naming conventions for all fields. The following fields are concerned:

    * getMin() and getMax() replace the fields min and max fields of Range and AADD that are private now.
    * getValue() replaces Value() as getter for the field value in BDD and AADD.
    * getResult() replaces in the expression parser the field result that is private now.
    * several methods, e.g. range(...), scalar(...) now start with small letter as usual in Java.

2. Renaming of arithmetic functions to their respective operator names.
For example:
    * x.sum(y) has become x.plus(y)
    * x.mul(y) has become x.times(y)

3. The method names in the factories for BDD and AADD have been renamed.
    * BDD.constant(boolean) replaces BDD.newLeaf(Boolean)
    * BDD.variable("X") replaces BDD.Bool("X").

4. AADDMgr has been split into static fields or methods of the respective classes AADD or BDD.
Only the AADD and BDD streams remain in the class AADDstreams.
