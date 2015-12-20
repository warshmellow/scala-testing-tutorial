# Intro to Scala Testing and TDD, Part 1 (ScalaTest)
- Covering ScalaTest and IntelliJ 
- Later Parts will cover:
	* Mocking with EasyMock
	* Property Based testing with ScalaCheck
	* Unit Testing Spark and SparkStreaming Apps

## ScalaTest
ScalaTest is a Unit and Integration test framework for Scala,
with behavior similar to Ruby RSpec.

We'll follow the [ScalaTest User Guide](http://www.scalatest.org/user_guide) loosely.

## ScalaTest and IntelliJ
### Setup
##### Start a new project in IntelliJ with maven quickstart template
##### Add the following code to the pom.xml file:
	<dependency>
		<groupId>org.scalatest</groupId>
		<artifactId>scalatest_2.10</artifactId>
		<version>2.2.4</version>
		<scope>test</scope>
	</dependency>

##### Add the folder "src/test/scala/[name e.g. same as only directory in src/main/java]". Right click "src/test/scala" and Mark Directory As -> Test Sources Root

##### Add the folder "src/main/scala/[name]". Right click "src/main/scala" and Mark Directory As -> Sources Root
---
### Basic Testing Exercise: Building a Mutable Stack Class
We're going to use Test Driven Development with ScalaTest to build a mutable stack class called MyStack. It has the usual interface:

	class MyStack:
		def MyStack()
		def push(item)
		def pop()
		def isEmpty()
		def size()

#### Create the file test file "src/test/scala/[name]/MyStackSpec.scala". 
For the rest of this exercise, [name] will assumed to be "dei" (Change yours accordingly). You may need to let IntelliJ add a Scala SDK to your project. Choose Scala 2.10.x. Put in the following code:

``` scala
package dei // package [name]

import org.scalatest._

class MyStackSpec extends FlatSpec {

  behavior of "A MyStack"

}
```
This file contains tests for the class MyStack. It starts with some package and import boilerplate. Then you define a class MyStackSpec that extends FlatSpec. ScalaTest considers your tests to be whatever is in a "spec". Formally, we define a class MyStackSpec that extends \*Spec. The choice of FlatSpec over other \*Spec governs the syntax used to write tests. The syntax we'll cover below assumes FlatSpec, but using other \*Specs will have similar (but not the same syntax).

We specify the thing whose behavior we want to test (here, MyStack) using the syntax:
	
	behavior of "..."
	
Now, this is enough code to specify a spec; it just contains no tests. We can tell IntelliJ to run this MyStackSpec class and we should see the following output:
	
	Testing started at ...
	Empty test suite.

	Process finished ...
	
Vacuously, your MyStack class passed all of its tests.

#### Add an actual requirement: using "it should... in" and "assert"

Now let's put an actual requirement in our spec. 
We can get a few ways to do that through "assertions". (Cf. [Using assertions](http://www.scalatest.org/user_guide/using_assertions)).
Let's say we want to instantiate a MyStack and then it should be empty.

We'll use the construct `it should "[behavior]" in { [test] }`.

``` scala
it should "be empty when instantiated" in {
  val emptyStack = new MyStack[Int]
  assertResult(true) { emptyStack.isEmpty }
}
```
Here, `it` refers to whatever follows `behavior of`. The `should` indicates we are writing a test. The behavior in quotes will be whatever is shown when we run the test suite. `in` is followed by a block that contains the actual code of the test. Usually there is an `assert` in this code block, with the test passing if `assert` passes.
The `assertResult(expected) { }` asserts that the value of the code block is what is `expected`. We may also use `assert(condition)` which takes any condition. Here, we may also write:

``` scala
it should "be empty when instantiated" in {
  val emptyStack = new MyStack[Int]
  assert(emptyStack.isEmpty == true)
}
```
Run this test in IntelliJ by running MyStackSpec. It will complain about not finding MyStack.

#### Create a file src/main/scala/[name]/MyStack.scala and pass the test
Put in the following:

``` scala
package dei // package [name]

class MyStack[T] {

  def isEmpty() = 0 == 0

}
```
Run MyStackSpec again and you'll see all the tests pass. You should see the sentence "A MyStack should be empty when instantiated".

#### Allow for pending tests and ingored tests
Suppose we want the requirement of last-in-first-out behavior, but we're not sure how to test it yet. We could have a pending test: a test that's in the spec but will not run. We use `it should [behavior] in (pending)`.

``` scala
...
class MyStackSpec extends FlatSpec {

  behavior of "A MyStack"

  it should "be empty when instantiated" in {
    ...
  }
  
  it should "pop values in last-in-first-out order" in (pending)
}
```
##### Exercise: Replace `(pending)` with a test `{ [test] }` for LIFO order and modify MyStack.scala to pass the test (you need to define a constructor, pop, push).

If you already have a test and want to ignore it, you can replace `it` with `ignore`, and the test won't run.


#### Allow for tests with Exceptions

In order to assert that an exception is thrown when something happens, we use the `intercept[Exception] { [test] }` construct. Say we want an empty stack to throw a `NoSuchElementException` when we pop it. We could write:

``` scala
it should "throw NoSuchElementException if empty and is popped" in {
  val emptyStack = new MyStack[Int]
  intercept[NoSuchElementException] {
    emptyStack.pop()
  }
}
```

##### Exercise: Pass this test

#### Syntatic Sugar with Matchers

ScalaTest let's you replace `assert` with a wide variety of different constructs called Matchers. See here for a [long list](http://www.scalatest.org/user_guide/using_matchers) of them. For just one example, in the emptiness requirement, let's replace the `assertResult` with the matcher `shouldBe empty`.

First you need to let your MyStackSpec mixin the trait `Matchers`.

``` scala
class MyStackSpec extends FlatSpec with Matchers {
...
```

Then you can change the code as follows:

``` scala
...
  it should "be empty when instantiated" in {
    val emptyStack = new MyStack[Int]
    emptyStack shouldBe empty
  }
...
```

#### Given [things] When [condition] Then [result] pattern

The pattern of "Given [something], When [something meets some condition], Then [some result]" is very common in tests. We can for example write the NoSuchElementException behavior as "Given an empty stack, when trying to pop, then it should throw a NoSuchElementException". We can bake this "comment" into our tests.

First mixin `GivenWhenThen` into our MyStackSpec:

``` scala
...
class MyStackSpec extends FlatSpec with Matchers with GivenWhenThen {
...
```

Then change the code as below:

``` scala
it should "throw NoSuchElementException if an empty stack is popped" in {
    Given("an empty stack")
    val emptyStack = new MyStack[Int]
    
    When("trying to pop")
    Then("should throw NoSuchElementException")
    intercept[NoSuchElementException] {
      emptyStack.pop()
    }
  }
```
The `Given`, `When`, `Then` don't change the test, but they do change the output from the test. Namely, their arguments will be put together into the output from your test.

##### Exercise: Change your "pop values in last-in-first-out order" test to follow this pattern.

#### Fixtures: Running code before and after each and all tests

ScalaTest has a very granular and complex system to specify what gets run before and after tests. These mechanisms are called Fixtures (see [here](http://www.scalatest.org/user_guide/sharing_fixtures)). We'll just cover the case where each test has same before and after behavior, and where there's setup and teardown code for all tests. Usually an object that persists through the life of running all tests in a spec is created and destroyed with a Fixture, such as repeatedly mutated object.

If you want to specify behavior run before each test and after each test, mixin the `BeforeAndAfter` trait into your `MyStackSpec`. It supports two functions `before` and `after`, each accepting a code block that is executed before and after each test.

Let's say we want to print "Starting" before each test. We have:

``` scala
...
class MyStackSpec extends FlatSpec
  with GivenWhenThen with Matchers with BeforeAndAfter {
  before { println("Starting") }
  after { }
  ...
```

##### Exercise: Print "Ending" after each test.

Now, if we want to run code before running any tests, or code after finishing all tests, we mixin trait `BeforeAndAfterAll`. This trait supports two functions `beforeAll()` and `afterAll()` that need to be overridden.

Suppose we want to print "Starting Whole Suite" before running any tests". We have:

``` scala
class MyStackSpec extends FlatSpec
  with GivenWhenThen with Matchers with BeforeAndAfterAll {
  override def beforeAll() = {
    println("Starting Whole Suite")
  }

  override def afterAll() = { }
...
```

##### Exercise: We can use both `BeforeAndAfter` and `BeforeAndAfterAll` at the same time. Print something after each test and all tests.

---
### Next Steps:

There is one small last thing: you can define your own abstract class of Spec, where you already mixed in our favorite traits. This just saves on some boilerplace. (Cf. [here](http://www.scalatest.org/user_guide/defining_base_classes)).

Learning these constructs should be enough to read this [blog post](http://mkuthan.github.io/blog/2015/03/01/spark-unit-testing/) on Spark Unit testing.

If you want to use a different IDE than IntelliJ, here's a [guide](http://www.scalatest.org/user_guide/running_your_tests) to that (e.g. command line, maven, scala interpreter, etc.)

Part 2 will go into that Spark Unit testing setup.

Enjoy!