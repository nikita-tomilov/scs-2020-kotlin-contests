data class Person(
  val surname: String,
  val name: String
) {
  override fun toString(): String {
    return "$name $surname"
  }
}

fun main() {
  val n = readLine()!!.toInt()
  val a = (0 until n).map { Person(readLine()!!, readLine()!!) }
  a.sortedBy { it.surname }.forEach {
    println(it)
  }
}