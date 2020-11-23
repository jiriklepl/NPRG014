package h4

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

abstract class Expression
case class Var(name: String) extends Expression
case class Number(num: Double) extends Expression
case class BinOp(operator: String, left: Expression, right: Expression) extends Expression

class ExpressionImplicitsImpl(val c: Context) {
  import c.universe._

  def expr (exprTree: c.Expr[AnyRef]) : c.Expr[Expression] = {
    exprTree.tree match {
      case Function (_, e) => expr(c.Expr(e))
      case Apply (Select (left, TermName ("$times")), right) =>
        c.Expr(q"""new BinOp("*", ${expr(c.Expr(left))}, ${expr(c.Expr(right(0)))})""")
      case Apply (Select (left, TermName ("$plus")), right) =>
        c.Expr(q"""new BinOp("+", ${expr(c.Expr(left))}, ${expr(c.Expr(right(0)))})""")
      case Apply (Select (left, TermName ("$div")), right) =>
        c.Expr(q"""new BinOp("/", ${expr(c.Expr(left))}, ${expr(c.Expr(right(0)))})""")
      case Apply (Select (left, TermName ("$minus")), right) =>
        c.Expr(q"""new BinOp("-", ${expr(c.Expr(left))}, ${expr(c.Expr(right(0)))})""")
      case Ident (TermName (name)) =>
        c.Expr(q"""new Var($name)""")
      case Literal (Constant (num : Int)) =>
        c.Expr(q"""new Number($num)""")
      case Literal (Constant (num : Double)) =>
        c.Expr(q"""new Number($num)""")
      case Literal (Constant (num)) =>
        throw new UnsupportedOperationException(num + " is expected to be Double!")
      case stat =>
        throw new UnsupportedOperationException(stat.toString)
    }
  }

}

object ExpressionImplicits {
  def expr(exprTree: AnyRef): Expression = macro ExpressionImplicitsImpl.expr
}
