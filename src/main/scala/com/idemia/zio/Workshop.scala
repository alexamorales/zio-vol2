package com.idemia.zio
package com.idemia.zio

import zio._
import zio.console.Console

object ZIOTypes {
  type ??? = Nothing

  /** ZIO[-R, +E, +A]
    *
    * A = The type with which the effect may succeed
    *     (Corresponds to the `A` in `Future[A]`)
    *
    * E = The type with which the effect may failed
    *     (Corresponds to the `Throwable`)
    *
    * R = The environment type required to run the effect
    * R => Either(E, A]
    *
    * If we don't need R, we can use Any
    * _           wan't to fail or succeed, we can use Nothing
    */

  /** EXERCISE 1
    *
    * Provide definitions for the ZIO type aliases below.
    */
  type Task[+A] = ???
  type UIO[+A] = ???
  type RIO[-R, +A] = ???
  type IO[+E, +A] = ???
  type URIO[-R, +A] = ???
}

object HelloWorld extends App {

  /** EXERCISE 2
    *
    * Implement a simple "Hello World" program using the effect returned by `putStrLn`.
    */
  def run(args: List[String]): URIO[Console, ExitCode] =
    ZIO.succeed(0).exitCode
}

object PrintSequence extends App {

  /** EXERCISE 3
    *
    * Using `*>` (`zipRight`), compose a sequence of `putStrLn` effects to
    * produce an effect that prints three lines of text to the console.
    */
  def run(args: List[String]): URIO[Console, ExitCode] =
    ???
}

object ErrorRecovery extends App {
  val StdInputFailed = 1

  import zio.console._

  val failed =
    putStrLn("About to fail...") *>
      ZIO.fail("Uh oh!") *>
      putStrLn("This will NEVER be printed!")

  /** EXERCISE 4
    *
    * Using `ZIO#orElse` or `ZIO#fold`, have the `run` function compose the
    * preceding `failed` effect into the effect that `run` returns.
    */
  def run(args: List[String]): URIO[Console, ExitCode] = ???
}

object EffectConversion extends App {

  /** EXERCISE 4
   *
   * Using ZIO.effect, convert the side-effecting of `println` into a pure
   * functional effect.
   */
  def myPrintLn(line: String): Task[Unit] = ??? //ZIO.effect

  def run(args: List[String]): URIO[Console, ExitCode] =
    myApplogic.exitCode

  val myApplogic: ZIO[Any, Nothing, Int] = (myPrintLn("Hello Again!") as 0) orElse ZIO.succeed(1)

}

object ErrorNarrowing extends App {
  import java.io.IOException

  /** EXERCISE 5
    *
    * Using `ZIO#refineToOrDie`, narrow the error type of the following
    * effect to IOException.
    */
  val myReadLine: IO[IOException, String] = ???

  def myPrintLn(line: String): UIO[Unit] = UIO(println(line))

  def run(args: List[String]): URIO[Console, ExitCode] =
    myAppLogic.exitCode

  val myAppLogic: ZIO[Any, IOException, Unit] = for {
    _ <- myPrintLn("What is your name?")
    name <- myReadLine
    _ <- myPrintLn(s"Good to meet you, ${name}")
  } yield ()

}

object PromptName extends App {
  val StdInputFailed = 1

  import zio.console._

  /** EXERCISE 6
    *
    * Implement a simple program that asks the user for their name (using
    * `getStrLn`), and then prints it out to the user (using `putStrLn`).
    */
  def run(args: List[String]): URIO[Console, ExitCode] = ???
}

object NumberGuesser extends App {
  import zio.console._

  def analyzeAnswer(random: Int, guess: String) =
    if (random.toString == guess.trim) putStrLn("You guessed correctly!")
    else putStrLn("You did not guess correctly. The answer was ${random}")

  /** EXERCISE 7
    *
    * Choose a random number (using `nextInt`), and then ask the user to guess
    * the number, feeding their response to `analyzeAnswer`, above.
    */
  def run(args: List[String]): URIO[Console, ExitCode] =
    ???
}

object AlarmApp extends App {
  import zio.console._
  import zio.duration._

  import java.io.IOException

  /** EXERCISE 8
    *
    * Create an effect that will get a `Duration` from the user, by prompting
    * the user to enter a decimal number of seconds.
    */
  lazy val getAlarmDuration: ZIO[Console, IOException, Duration] = {
    def parseDuration(input: String): IO[NumberFormatException, Duration] =
      ???

    def fallback(input: String): ZIO[Console, IOException, Duration] =
      ???

    ???
  }

  /** EXERCISE 9
    *
    * Create a program that asks the user for a number of seconds to sleep,
    * sleeps the specified number of seconds, and then prints out a wakeup
    * alarm message.
    */
  def run(args: List[String]): URIO[Console, ExitCode] =
    ???
}

object Cat extends App {
  import zio.blocking._
  import zio.console._

  import java.io.IOException

  /** EXERCISE 10
    *
    * Implement a function to read a file on the blocking thread pool, storing
    * the result into a string.
    */
  def readFile(file: String): ZIO[Blocking, IOException, String] = ???

  /** EXERCISE 11
    *
    * Implement a version of the command-line utility "cat", which dumps the
    * contents of the specified file to standard output.
    */
  def run(args: List[String]): URIO[Console, ExitCode] =
    ???
}
