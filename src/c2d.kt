data class Book(
  val author: String,
  val title: String
)

fun main() {
  val n = readLine()!!.toInt()
  val a = (0 until n).map {
    val line = readLine()!!.split(" ")
    Book(line[0], line[1])
  }
  val g = a.groupBy { it.author }
  g.keys.toList().sorted().forEach { author ->
    println(author)
    println(g[author]!!.map { it.title }.sorted().joinToString { it })
  }
}