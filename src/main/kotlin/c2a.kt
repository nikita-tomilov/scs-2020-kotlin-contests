fun main() {
  val n = readLine()!!.toInt()
  println((0 until n).map { readLine()!!.toInt() }.sum())
}