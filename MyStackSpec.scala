package dei

import org.scalatest._

class MyStackSpec extends FlatSpec
  with GivenWhenThen with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  val emptyStack = new MyStack[String]
  before {
    println("Starting")
  }

  after {
    println("Test Done")
  }

  override def beforeAll() = {
    println("Starting Whole Suite")
  }

  override def afterAll() = {
    println("End whole suite")
  }

  behavior of "A MyStack"

  it should "throw NoSuchElementException if an empty stack is popped" in {
    Given("an empty stack")
    When("trying to pop")
    Then("should throw NoSuchElementException")
    intercept[NoSuchElementException] {
      emptyStack.pop()
    }
  }

  it should "have size 0 when first instantiated" in {
    emptyStack shouldBe empty
  }

  it should "pop values in last-in-first-out order" in {
    val stack = new MyStack[Int]
    stack.push(1)
    assert(stack.size === 1)
    stack.push(2)
    assert(stack.size === 2)
    assert(stack.pop() === 2)
    assert(stack.size === 1)
    assert(stack.pop() === 1)
    assert(stack.size === 0)
    assertResult(0) { stack.size }
  }

  ignore should "some dumb behavior we're ignoring" in { assert(0 == 1) }
  it should "some dumb behavior that's pending" in (pending)
}