# Intro to Scala Testing and TDD, Part 2 (ScalaTest and Spark)
- Covering ScalaTest, IntelliJ, Spark
- Previous Parts covered:
	* Basic ScalaTest with IntelliJ
- Later Parts will cover:
	* Mocking with EasyMock
	* Property Based testing with ScalaCheck

## ScalaTest and Spark
We'll follow this [tutorial](http://mkuthan.github.io/blog/2015/03/01/spark-unit-testing/) for Spark Unit testing, loosely.


Here for reference: [ScalaTest User Guide](http://www.scalatest.org/user_guide).

## ScalaTest, Spark and IntelliJ
### Setup
##### Start a new project in IntelliJ with maven quickstart template
##### Add the following code to the pom.xml file:

		<dependency>
    	<groupId>org.apache.spark</groupId>
    	<artifactId>spark-core_2.10</artifactId>
    	<version>1.5.2</version>
  	</dependency>

	<dependency>
		<groupId>org.scalatest</groupId>
		<artifactId>scalatest_2.10</artifactId>
		<version>2.2.4</version>
		<scope>test</scope>
	</dependency>

##### Add the folder "src/test/scala/[name e.g. same as only directory in src/main/java]". Right click "src/test/scala" and Mark Directory As -> Test Sources Root

##### Add the folder "src/main/scala/[name]". Right click "src/main/scala" and Mark Directory As -> Sources Root
---
### Basic Spark and Testing Exercise: Batch Word Count
We'll start with the most basic example of a batch Spark job: word count!

#### Create the file test file "src/test/scala/[name]/SparkExampleSpec.scala".
For the rest of this exercise, [name] will assumed to be "dei" (Change yours accordingly). You may need to let IntelliJ add a Scala SDK to your project. Choose Scala 2.10.x. Put in the following code:

``` scala
package dei // package [name]

import org.apache.spark._
import org.scalatest._

trait SparkSpec extends BeforeAndAfterAll {
  this: Suite =>

  private val master = "local[4]"
  private val appName = this.getClass.getSimpleName

  private var _sc: SparkContext = _

  def sc = _sc

  val conf: SparkConf = new SparkConf()
    .setMaster(master)
    .setAppName(appName)

  override def beforeAll(): Unit = {
    super.beforeAll()
    _sc = new SparkContext(conf)
  }

  override def afterAll(): Unit = {
    if (_sc != null) {
      _sc.stop()
      _sc = null
    }
    super.afterAll()
  }
}
```
We are defining a trait `SparkSpec` that we will ultimately mixin to your actual \*Spec classes. ScalaTest encourages you to define mixins and subclasses of \*Spec to make your own starting templates for your test suites. (Cf. [here](http://www.scalatest.org/user_guide/defining_base_classes)).

Notice we use `BeforeAndAfterAll` to define a Fixture. Namely, we use it to define a `SparkContext` called `sc` that is created before all tests and then destroyed after all tests. (Cf. [here](http://www.scalatest.org/user_guide/sharing_fixtures))

#### Add an actual test

``` scala
...
trait SparkSpec ... {
	...
}

class SparkExampleSpec extends FlatSpec
with SparkSpec with GivenWhenThen with Matchers {
  behavior of "A batch Spark word count"

  it should "be empty if we feed it an empty document" in {
    Given("A an empty document")
    val emptyDoc = Array("")
    val emptyDocRDD = sc.parallelize(emptyDoc)

    When("we run word count on it")
    val result = WordCount.counts(emptyDocRDD)
    Then("we should see an empty list of words")
    result shouldBe empty
  }
}
```
This test assumes we're going to put all of our counting logic in an object called `WordCount`, exposing a single function `counts`.


#### Create a file src/main/scala/[name]/WordCount.scala and pass the test
Put in the following:

``` scala
package dei // package [name]

import org.apache.spark.rdd.RDD

object WordCount {
  def counts(lines: RDD[String]) = List()
}

```
Run ScalaExampleSpec again and you'll see all the tests pass.

##### Exercise: Add the following to your test and make it pass:

``` scala
it should "correctly count the words of a famous quote by Shakespeare" in {
    Given("the quote 'To be or, not to be, that is the question")
    val quote = "To be or, not to be, that is the question"
    val quoteRDD = sc.parallelize(Array(quote))

    When("we run word count on it")
    Then("we should see the right counts of words")
    assertResult(
      Array(
        ("the", 1),
        ("be", 2),
        ("is", 1),
        ("not", 1),
        ("or", 1),
        ("question", 1),
        ("that", 1),
        ("to", 2)).
        toSet) {
      WordCount.counts(quoteRDD).collect.toSet
    }
  }
```

---
### Basic Spark SQL and Testing Exercise: Batch Employee Table Processing

Now we'll add HiveQL to our batch processing.

#### Add the Hive package to pom.xml with the following.

		<dependency>
				<groupId>org.apache.spark</groupId>
				<artifactId>spark-hive_2.10</artifactId>
				<version>1.5.2</version>
		</dependency>

#### Create the file src/test/scala/[name]/SparkSQLExampleSpec.scala

``` scala
package dei // package [name]

import org.apache.spark.sql.hive.HiveContext
import org.scalatest._

trait SparkSqlSpec extends SparkSpec {
  this: Suite =>

  private var _sqlc: HiveContext = _

  def sqlc = _sqlc

  override def beforeAll(): Unit = {
    super.beforeAll()

    _sqlc = new HiveContext(sc)
  }

  override def afterAll(): Unit = {
    _sqlc = null

    super.afterAll()
  }

}
```

This creates a new trait `SparkSqlSpec` that defines a Fixture, the `HiveContext`, named `sqlc`.

---
### Basic Spark Streaming Exercise
---
### Next Steps:

If you want to use a different IDE than IntelliJ, here's a [guide](http://www.scalatest.org/user_guide/running_your_tests) to that (e.g. command line, maven, scala interpreter, etc.)

Part 3 will into Mocking with EasyMock.

Enjoy!
