fun main() {
  val n = readLine()!!.toInt()
  val a = (0 until n).map { readLine()!!.toInt() }
  println(a.max())
  println(a.min())
  println(a.average())
  println(a.sorted()[a.size / 2])
}