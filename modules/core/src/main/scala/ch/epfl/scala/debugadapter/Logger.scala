package ch.epfl.scala.debugadapter

trait Logger {
  def debug(msg: => String): Unit
  def info(msg: => String): Unit
  def warn(msg: => String): Unit
  def error(msg: => String): Unit
  def trace(t: => Throwable): Unit
}
