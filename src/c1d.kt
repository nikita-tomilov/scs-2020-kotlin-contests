fun main() {
  val N = readLine()!!.toInt()
  println((1..N).map { it * it }.sum())
}