import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.io.FileInputStream
import java.util.function.Consumer

class Tester {

  private val currentDir = "."
  private val solutionsDir = "$currentDir/src/main/kotlin"
  private val testsDir = "$currentDir/src/test/resources"
  private val kotlinc = "/home/hotaro/.sdkman/candidates/kotlin/current/bin/kotlinc"

  @TestFactory
  fun `test all tasks against prebuilt tests`(): Collection<DynamicTest> {
    return getSolutionSources()
        .asSequence()
        .map { it.toSolution() }
        .map { it.toTests() }
        .flatten()
        .sortedBy { it.testHumanName }
        .map {
          DynamicTest.dynamicTest(it.testHumanName) {
            performTest(it)
          }
        }
        .toList()
  }

  data class Solution(
    val humanName: String,
    val file: File,
    var compiledJar: File? = null
  )

  data class SolutionTest(
    val testHumanName: String,
    val solution: Solution,
    val input: File,
    val output: File
  )

  private fun getSolutionSources(): List<File> =
      File(solutionsDir).listFiles()?.toList()
        ?: error("solutionsDir $solutionsDir empty or broken")

  private fun File.toSolution(): Solution {
    val humanName =
        "Contest ${getContestIdForSolutionFile(this)}, task ${getSolutionIdByFile(this)}"
    return Solution(humanName, this)
  }

  private fun Solution.toTests(): List<SolutionTest> {
    val contestId = getContestIdForSolutionFile(this.file)
    val solutionId = getSolutionIdByFile(this.file)
    val testsDir = File("$testsDir/$contestId/$solutionId")
    val testsFiles = testsDir.listFiles() ?: return emptyList()

    val testOutputFiles = testsFiles.filter { it.name.endsWith(".a") }
    return testOutputFiles.map {
      val testInputFile = File(it.toString().substring(0 until (it.toString().length - 2)))
      SolutionTest(
          "$humanName, test $testInputFile",
          this,
          testInputFile,
          it)
    }
  }

  private fun performTest(test: SolutionTest) {
    if (test.solution.compiledJar == null) {
      compile(test.solution)
    }

    val expectedOutput = test.output.readLines()
    val actualOutput = ArrayList<String>()

    val exitCode = launchApp(
        File(currentDir),
        "java -jar ${test.solution.compiledJar}",
        FileInputStream(test.input),
        Consumer { actualOutput.add(it) })

    assert(exitCode == 0) { "Execution was not successful " }

    assert(expectedOutput.size == actualOutput.size)
    (expectedOutput.indices).forEach {
      val a = expectedOutput[it]
      val b = actualOutput[it]
      assert(a == b) { "Line $it mismatch, expected $a, found $b" }
    }
  }

  private fun compile(solution: Solution) {
    println("Compiling ${solution.file}")
    val target = "/tmp/solution-${solution.file.name}.jar"
    val exitCode = launchApp(
        File(currentDir),
        "$kotlinc ${solution.file.path} -include-runtime -d $target")
    assert(exitCode == 0) { "Compilation was not successful " }
    solution.compiledJar = File(target)
  }

  private fun getContestIdForSolutionFile(solution: File): String {
    return solution.name.substring(0..1)
  }

  private fun getSolutionIdByFile(solution: File): String {
    return solution.name.substring(2, 3)
  }
}