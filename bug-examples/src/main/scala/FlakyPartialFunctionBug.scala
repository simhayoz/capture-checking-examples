// Probably linked to capture tunneling
// class FlakyPartialFunctionBug {
//   def testFunction: PartialFunction[Int, {*} String] = {
//     case t: Int if t == 3 =>
//       getString(t)

//     case t: Int if t == 5 =>
//       getString(t)
//   }
  
//   def getString(i: Int): {*} String = i.toString
// }

// -------------------------------------------------------------
// Working example
// class FlakyPartialFunctionBug {
//   def testFunction: Int => {*} String = _ match {
//     case t: Int if t == 3 =>
//       val tt: {*} String = "3"
//       tt

//     case t: Int if t == 5 =>
//       val tt: {*} String = "5"
//       tt
//   }
// }

// -------------------------------------------------------------
// Working reduction
// class FlakyPartialFunctionBug(val capString: {*} String = "") {
//   def testFunction: PartialFunction[Int, {capString} String] = {
//     case t if t == 3 =>
//       capString + "3"

//     case t if t == 5 =>
//       capString + "5"
//   }
// }

// -------------------------------------------------------------
// Initial reduction
class FlakyPartialFunctionBug(val capString: {*} String = "") {
  def testFunction: PartialFunction[Int, {capString} String] = {
    case t if t == 3 =>
      val v: {capString} String = "3"
      v

    case t if t == 5 =>
      val v: {capString} String = "5"
      v
  }
}

object FlakyPartialFunctionBug {
  def main(args: Array[String]) = 
    val temp = FlakyPartialFunctionBug()
    println(temp.testFunction(3))
}
