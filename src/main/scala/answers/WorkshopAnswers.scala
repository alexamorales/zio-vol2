package com.idemia.zio
package answers

import answers.ErrorRecovery.myAppLogic

import zio._
import zio.console.Console

import java.io.IOException
import scala.io.Source

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
  type Task[+A] = ZIO[Any, Throwable, A]
  type UIO[+A] = ZIO[Any, Nothing, A]
  type RIO[-R, +A] = ZIO[R, Throwable, A]
  type IO[+E, +A] = ZIO[Any, E, A]
  type URIO[-R, +A] = ZIO[R, Nothing, A]
}

object WorkshopAnswers extends App {
  import zio.console._

  /** EXERCISE 2
    *
    * Implement a simple "Hello World" program using the effect returned by `putStrLn`.
    */
  def run(args: List[String]): URIO[Console, ExitCode] =
    myAppLogic.exitCode

  val myAppLogic: ZIO[Console, IOException, Unit] = for {
    _ <- putStrLn("Hello! What is your name?")
    name <- getStrLn
    _ <- putStrLn(s"Entered ${name}")
  } yield ()

//    unsafeRunSync(myAppLogic).fold(_ => ZIO.succeed(1), _ => ZIO.succeed(1))

}

object PrintSequence extends App {
  import zio.console._

  /** EXERCISE 3
    *
    * Using `*>` (`zipRight`), compose a sequence of `putStrLn` effects to
    * produce an effect that prints three lines of text to the console.
    */
  def run(args: List[String]): URIO[Console, ExitCode] =
    myappLogic.exitCode

  val myappLogic: ZIO[Console, IOException, Int] = putStrLn("Hello World!") *>
    putStrLn("Goodbye World!") *>
    ZIO.succeed(0)

}

object ErrorRecovery extends App {
  val StdInputFailed = 1

  import zio.console._

  val failed: ZIO[Console, Serializable, Unit] =
    putStrLn("About to fail...") *>
      ZIO.fail("Uh oh!") *>
      putStrLn("This will NEVER be printed!")

  /** EXERCISE 3
    *
    * Using `ZIO#orElse` or `ZIO#fold`, have the `run` function compose the
    * preceding `failed` effect into the effect that `run` returns.
    */
  def run(args: List[String]): URIO[Console, ExitCode] =
    myAppLogic.exitCode

  val myAppLogic: URIO[Console, Int] = failed.fold(_ => 1, _ => 0)
  //  failed.map(_ => 0).orElse(ZIO.succeed(1))
}

object EffectConversion extends App {

  /** EXERCISE 4
    *
    * Using ZIO.effect, convert the side-effecting of `println` into a pure
    * functional effect.
    */
  def myPrintLn(line: String): Task[Unit] = ZIO.effect(println(line))

  def run(args: List[String]): URIO[Console, ExitCode] =
    myAppLogic.exitCode

  val myAppLogic: ZIO[Any, Nothing, Int] =
    (myPrintLn("Hello Again!") as 0) orElse ZIO.succeed(1)
}

object ErrorNarrowing extends App {
  import java.io.IOException
  import scala.io.StdIn.readLine
  implicit class Unimplemented[A](v: A) {
    def ? = ???
  }

  /** EXERCISE 5
    *
    * Using `ZIO#refineToOrDie`, narrow the error type of the following
    * effect to IOException.
    */
  val myReadLine: IO[IOException, String] =
    ZIO.effect(readLine()).refineToOrDie[IOException]

  def myPrintLn(line: String): UIO[Unit] = UIO(println(line))

  def run(args: List[String]): URIO[Console, ExitCode] =
    myAppLogic.exitCode

  val myAppLogic: ZIO[Any, Nothing, Int] = (for {
    _ <- myPrintLn("What is your name?")
    name <- myReadLine
    _ <- myPrintLn(s"Good to meet you, ${name}")
  } yield 0) orElse ZIO.succeed(1)

}

object PromptName extends App {
  val StdInputFailed = 1

  import zio.console._

  /** EXERCISE 6
    *
    * Implement a simple program that asks the user for their name (using
    * `getStrLn`), and then prints it out to the user (using `putStrLn`).
    */
  def run(args: List[String]): URIO[Console, ExitCode] =
    myAppLogic.exitCode

  val myAppLogic: URIO[Console, Int] = (for {
    _ <- putStrLn("Please enter your name: ")
    name <- getStrLn
    _ <- putStrLn(s"Hello, $name!")
  } yield ()).fold(_ => StdInputFailed, _ => 0)
}

object NumberGuesser extends App {
  import zio.console._
  import zio.random._

  def analyzeAnswer(random: Int, guess: String) =
    if (random.toString == guess.trim) putStrLn("You guessed correctly!")
    else putStrLn("You did not guess correctly. The answer was ${random}")

  /** EXERCISE 7
    *
    * Choose a random number (using `nextInt`), and then ask the user to guess
    * the number, feeding their response to `analyzeAnswer`, above.
    */
  def run(args: List[String]): URIO[Console, ExitCode] =
    myAppLogic.exitCode

  val myappLogic: Any = (for {
    random <- nextIntBounded(10)
    _ <- putStrLn("Guess a number from 0 to 10: ")
    guess <- getStrLn
    _ <- analyzeAnswer(random, guess)
  } yield ()).fold(_ => 1, _ => 0)
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
      ZIO
        .effect(input.toInt)
        .map(_.seconds)
        .refineToOrDie[NumberFormatException]

    def fallback(input: String): ZIO[Console, IOException, Duration] =
      parseDuration(input).catchAll(error =>
        putStrLn(
          s"You enter $input, which is not a decimal: ${error.toString}"
        ) *>
          getAlarmDuration
      )
    // parseDuration(input).foldM(
    //   error => putStrLn(s"You enter $input, which is not a decimal: ${error.toString}") *>
    //   getAlarmDuration,
    //   duration => ZIO.succeed(duration)
    // )
    // parseDuration(input).tapError(error =>
    //   putStrLn(s"You enter $input, which is not a decimal: ${error.toString}")
    // ) orElse getAlarmDuration*/

    for {
      _ <- putStrLn("Please enter the number of seconds to sleep (e.g. 1.5)")
      input <- getStrLn
      duration <- fallback(input)
    } yield duration
  }

  /** EXERCISE 9
    *
    * Create a program that asks the user for a number of seconds to sleep,
    * sleeps the specified number of seconds, and then prints out a wakeup
    * alarm message.
    */
  def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    myAppLogic.exitCode

  val myAppLogic = (for {
    _ <- putStrLn("Welcome to ZIO Alarm!")
    duration <- getAlarmDuration
    _ <- putStrLn(s"You will be awoken after $duration")
    _ <- ZIO.sleep(duration)
    _ <- putStrLn("Time to wake up!")
  } yield ()).fold(_ => 1, _ => 0)

}

object Cat extends App {
  import zio.blocking._

  import java.io.IOException

  /** EXERCISE 10
    *
    * Implement a function to read a file on the blocking thread pool, storing
    * the result into a string.
    */
  def readFile(file: String): ZIO[Blocking, IOException, String] =
    /*  blocking {
      ZIO.effect()
      import scala.io.Source
      val open: Task[Source] = ZIO.effect(Source.fromFile(file))
      val close = (s: Source) => ZIO.effect(s.close()).orDie

      // bracket ensures that cleanup will be done if the resource was opened (even in the face of interruption)
      open
        .bracket(close(_)) { source => ZIO.effect(source.mkString) }
        .refineToOrDie[IOException]
    }*/

    blocking {
      ZIO
        .effect(Source.fromFile(file).mkString("\n"))
        .refineToOrDie[IOException]
    }

  /** EXERCISE 11
    *
    * Implement a version of the command-line utility "cat", which dumps the
    * contents of the specified file to standard output.
    */
  def run(args: List[String]) = ???
  /*
      if (args.isEmpty) putStrLn("Supply a file as an argument") as()
      else readFile(args.head).flatMap(putStrLn).fold(_ => 2, _ => 0)
      }
   */
}
