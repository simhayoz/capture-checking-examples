// Probably linked to capture tunneling
class TestTemp {
  def testFunction: PartialFunction[Int, {*} String] = {
    case t: Int if t == 3 =>
      getString(t)

    case t: Int if t == 5 =>
      getString(t)
  }
  
  def getString(i: Int): {*} String = i.toString
}

// -------------------------------------------------------------
// Working example
// class TestTemp {
//   def testFunction: Int => {*} String = _ match {
//     case t: Int if t == 3 =>
//       getString(t)

//     case t: Int if t == 5 =>
//       getString(t)
//   }
  
//   def getString(i: Int): {*} String = i.toString
// }

// -------------------------------------------------------------
// Initial reduction
// class TestTemp {
//   def testFunction: PartialFunction[Int, {*} Int] = {
//     case t if t == 3 =>
//       val tt: {*} Int = 3
//       tt

//     case t if t == 5 =>
//       val tt: {*} Int = 5
//       tt
//   }
// }

// -------------------------------------------------------------
// [error] Tree: matchResult2[{*} B1]: 
// [error]   {
// [error]     case val x2: (x : A1) @unchecked @unchecked = 
// [error]       x:(x : A1) @unchecked:(x : A1) @unchecked @unchecked
// [error]     {
// [error]       case val t: Int = x2
// [error]       if t.==(3) then 
// [error]         return[matchResult2] 
// [error]           {
// [error]             val tt: {*} String = "3"
// [error]             tt:String
// [error]           }
// [error]        else ()
// [error]     }
// [error]     {
// [error]       case val t: Int = x2
// [error]       if t.==(5) then 
// [error]         return[matchResult2] 
// [error]           {
// [error]             val tt: {*} String = "5"
// [error]             tt:String
// [error]           }
// [error]        else ()
// [error]     }
// [error]     return[matchResult2] default.apply(x)
// [error]   }

