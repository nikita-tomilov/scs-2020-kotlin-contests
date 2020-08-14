import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.function.Consumer

private class StreamGobbler(
  val inputStreamFromProcess: InputStream,
  val consumer: Consumer<String>
) : Runnable {
  override fun run() {
    BufferedReader(InputStreamReader(inputStreamFromProcess)).lines()
        .forEach(consumer)
  }
}

fun launchApp(dir: File, command: String): Int {
  return launchApp(dir, command, null, Consumer { s: String? -> println("OUT: $s") })
}

fun launchApp(
  dir: File,
  command: String,
  inputStream: InputStream?,
  outputConsumer: Consumer<String>
): Int {
  val builder = ProcessBuilder()
  builder.command("bash", "-c", command)
  builder.directory(dir)
  println("Running ${builder.command()}")
  val process = builder.start()
  if (inputStream != null) {
    val buffer = ByteArray(1024)
    var len = inputStream.read(buffer)
    while (len != -1) {
      process.outputStream.write(buffer, 0, len)
      len = inputStream.read(buffer)
    }
    process.outputStream.close()
    inputStream.close()
  }
  val streamGobbler = StreamGobbler(process.inputStream, outputConsumer)
  Executors.newSingleThreadExecutor().submit(streamGobbler)
  val errorStreamGobbler = StreamGobbler(process.errorStream,
      Consumer { s: String? -> println("ERR: $s") })
  Executors.newSingleThreadExecutor().submit(errorStreamGobbler)
  val exitCode = process.waitFor()
  println("exit code $exitCode")
  return exitCode
}