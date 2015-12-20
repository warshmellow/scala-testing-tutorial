package dei

/**
 * Created by gSchool on 12/18/15.
 */
class MyStack[T] {
  var items = List.empty[T]

  def pop() = items match {
    case Nil => throw new NoSuchElementException
    case x :: xs => {
      items = xs
      x
    }
  }

  def size() = items.size

  def isEmpty() = items.size == 0

  def push(item:T) = {
    items = item :: items
  }
}
