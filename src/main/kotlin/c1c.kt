fun main() {
  val a = readLine()!!
  println(a.split(" ").map { if (it.reversed() == it) 1 else 0 }.sum())
}