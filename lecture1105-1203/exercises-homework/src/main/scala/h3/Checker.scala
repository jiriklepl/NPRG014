package h3
import scala.collection.mutable.MutableList

abstract class Event
case class Command(cmdName: String) extends Event
case class Succeed(cmdName: String) extends Event
case class Fail(cmdName: String) extends Event

class Property(val name: String, val func: () => Boolean)

class Monitor[T] {
	val properties = MutableList.empty[Property]

	def property(propName: String)(formula: => Boolean) = {
		properties += new Property(propName, formula _)
	}

	var eventsToBeProcessed = List[T]()

	def check(events: List[T]) {
		for (prop <- properties) {
			eventsToBeProcessed = events

			val result = prop.func()

			println("Property \"" + prop.name + "\" ... " + (if (result) "OK" else "FAILED"))
		}
	}

	def require(func: PartialFunction[T, Boolean]): Boolean = {
		eventsToBeProcessed match {
			case event :: events =>
				if (func.isDefinedAt(event)) {
					eventsToBeProcessed = events
					func(event)
				} else {
					true
				}
			case Nil =>
				throw new NoSuchElementException()
		}
	}
}

class MyMonitor extends Monitor[Event] {
	property("The first command should succeed or fail before it is received again") {
		require {
			case Command(c) =>
				require {
					case Succeed(`c`) => true
					case Fail(`c`) => true
					case Command(`c`) => false
				}
		}
	}

	property("The first command should not get two results") {
		require {
			case Succeed(c) =>
				require {
					case Succeed(`c`) => false
					case Fail(`c`) => false
					case Command(`c`) => true
				}
			case Fail(c) =>
				require {
					case Succeed(`c`) => false
					case Fail(`c`) => false
					case Command(`c`) => true
				}
		}
	}

	property("The first command should succeed") {
		require {
			case Command(c) =>
				require {
					case Succeed(`c`) => true
					case Fail(`c`) => false
				}
		}
	}
}

object Checker {
	def main(args: Array[String]) {
		val events = List(
			Command("take_picture"),
			Command("get_position"),
			Succeed("take_picture"),
			Fail("take_picture")
		)

		val monitor = new MyMonitor
		monitor.check(events)
	}
}
