package h1

class Complex (private val re: Int, private val im: Int) {
	def this(n: Int) = this(n, 0)

	def + (other: Complex) = new Complex(re + other.re, im + other.im)
	def - (other: Complex) = new Complex(re - other.re, im - other.im)
	def * (other: Complex) = new Complex(re * other.re - im * other.im, re * other.im + im * other.re)
	def * (scalar: Int) = new Complex(re * scalar, im * scalar)
	def / (other: Complex) : Complex = (this * other.bar) / other.sqAbs
	def / (scalar: Int) = new Complex(re / scalar, im / scalar)
	def unary_- = new Complex(-re, -im)
	def bar = new Complex(re, -im)

	override def toString =
		if (im != 0)
			if (re != 0)
				re + (if (im > 0) "+" else "") + im + "i"
			else
				im + "i"
		else
			re.toString
	private def sqAbs = re * re + im * im
}

object Complex {
	def apply(n: Int) = new Complex(n)
	def apply(n: Int, m: Int) = new Complex(n, m)

	implicit def intToComplex(x: Int) = new Complex(x)
}

object ComplexNumbers {
	def I = new Complex(0, 1)

	def main(args: Array[String]) {

		println(Complex(1,2)) // 1+2i

		println(1 + 2*I + I*3 + 2) // 3+5i

		val c = (2+3*I + 1 + 4*I) * I
		println(-c) // 7-3i
	}
}