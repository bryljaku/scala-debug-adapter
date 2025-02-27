package scala.tools.nsc

import java.nio.file.Path
import java.util.function.Consumer
import java.{util => ju}
import scala.jdk.CollectionConverters._
import scala.tools.nsc.reporters.StoreReporter
import scala.util.control.NonFatal

final class ExpressionCompilerBridge {
  def run(
      outDir: Path,
      expressionClassName: String,
      classPath: String,
      options: Array[String],
      sourceFile: Path,
      line: Int,
      expression: String,
      localVariables: ju.Set[String],
      pckg: String,
      errorConsumer: Consumer[String],
      testMode: Boolean
  ): Boolean = {
    val args = List(
      "-d",
      outDir.toString,
      "-classpath",
      classPath
      // Debugging: Print the tree after phases of the debugger
      // "-Xprint:typer,generate-expression",
    ) ++ options :+ sourceFile.toString

    val command = new CompilerCommand(args, errorConsumer.accept(_))
    val reporter = new StoreReporter() // cannot fix because of Scala 2.10
    val global = new ExpressionGlobal(
      command.settings,
      reporter,
      line,
      expression,
      localVariables.asScala.toSet,
      expressionClassName
    )

    try {
      val run = new global.Run()
      run.compile(List(sourceFile.toString))

      val error = reporter.infos.find(_.severity == reporter.ERROR).map(_.msg)
      error.foreach(errorConsumer.accept)
      error.isEmpty
    } catch {
      case NonFatal(t) =>
        t.printStackTrace()
        errorConsumer.accept(t.getMessage())
        false
    }
  }
}
