package com.idemia.zio
package com.idemia.zio

import zio.console.{Console, putStrLn}
import zio.{ExitCode, IO, Ref, Schedule, Task, UIO, URIO, ZIO}

object Sandbox extends zio.App with TestRuntime {

  unsafeRun(putStrLn("Hello World!"))

  //creating Effects

  val s1: UIO[Int] = ZIO.succeed(42)

  /*Companion objects methods*/
  val s2: Task[Int] = Task.succeed(42)

  // lazy Effects
  lazy val bigList: List[Int] = (0 to 1000000).toList
  lazy val bigString: String = bigList.map(_.toString).mkString("\n")

  val s3: UIO[String] = ZIO.effectTotal(bigString)

  //failing Effects
  val f1: IO[String, Nothing] = ZIO.fail("Uh oh!")
  val f2: Task[Nothing] = Task.fail(new Exception("Uh oh!"))

  //From Scala values
  val zoption: IO[Option[Nothing], Int] = ZIO.fromOption(Some(2))
  val zoption2: ZIO[Any, String, Int] =
    zoption.mapError(_ => "It wasn't there!")

  //from Either
  val zeither: IO[Nothing, String] = ZIO.fromEither(Right("Success!"))

  // from Try
  import scala.util.Try

  val ztry: Task[Int] = ZIO.fromTry(Try(42 / 0))

  //from Function
  val zfun: ZIO[Int, Nothing, Int] =
    ZIO.fromFunction((i: Int) => i * i)

  //from Future
  import scala.concurrent.Future

  lazy val future: Future[String] = Future.successful("Hello!")

  val zfuture: Task[String] =
    ZIO.fromFuture { implicit ec =>
      future.map(_ => "Goodbye!")
    }

  //from Side-Effects

//  Synchronous
  import scala.io.StdIn

  val getStrLn: Task[Unit] =
    ZIO.effect(StdIn.readLine())

  import java.io.IOException

  val getStrLn2: IO[IOException, String] =
    ZIO.effect(StdIn.readLine()).refineToOrDie[IOException]

//  Asynchronous

  case class User()
  case class AuthError()

  object legacy {
    def login(onSuccess: User => Unit, onFailure: AuthError => Unit): Unit =
      println("Failure!")
  }

  val login: IO[AuthError, User] =
    IO.effectAsync[AuthError, User] { callback =>
      legacy.login(
        user => callback(IO.succeed(user)),
        err => callback(IO.fail(err))
      )
    }

  // from blocking Side-Effects

  import zio.blocking._

  val sleeping: ZIO[Blocking, Throwable, Unit] =
    effectBlocking(Thread.sleep(Long.MaxValue))

//  The resulting effect will be executed on a separate thread pool designed specifically for blocking effects.

//Basic Operations

  //mapping
  //mention that UIO - infalliable.
  val succeded: UIO[Int] = IO.succeed(21).map(_ * 2)

  val failed: IO[Exception, Unit] =
    IO.fail("No no!").mapError(msg => new Exception(msg))

  //chaining
  val sequenced: ZIO[Console, Throwable, Unit] =
    getStrLn.flatMap(input => putStrLn(s"You entered: $input"))

  //For Comperhensions
  //ZIO implements both map and flatMap

  val program: ZIO[Console, Throwable, Unit] =
    for {
      _ <- putStrLn("Hello! What is your name?")
      name <- getStrLn
      _ <- putStrLn(s"Hello, ${name}, welcome to ZIO!")
    } yield ()

  //Error handling

  //again UIO inffaliable
  //  ZIO[R, E, A] => ZIO[R, Nothing, Either[E, A]]

  val zeither2: UIO[Either[String, Int]] =
    IO.fail("Uh oh!").either

  //  Catching all errors
  val catchAll: IO[IOException, Array[Byte]] =
    openFile("primary.json").catchAll(_ => openFile("backup.json"))

  //  Fallback
//  You can try one effect, or, if it fails, try another effect, with the orElse combinator:

  val primaryOrBackupData: IO[IOException, Array[Byte]] =
    openFile("primary.data").orElse(openFile("backup.data"))

//Folding
  lazy val DefaultData: Array[Byte] = Array(0, 0)

  val primaryOrDefaultData: UIO[Array[Byte]] =
    openFile("primary.data").fold(_ => DefaultData, data => data)

  // you can effectfully handle both failure and success, by supplying an effectful (but still pure) handler for each case:
  val primaryOrSecondaryData: IO[IOException, Array[Byte]] =
    openFile("primary.data").foldM(
      _ => openFile("secondary.data"),
      data => ZIO.succeed(data)
    )

//Retrying effects

  import zio.clock._

  val retriedOpenFile: ZIO[Clock, IOException, Array[Byte]] =
    openFile("primary.data").retry(Schedule.recurs(5))

  /*  Ref
    Ref[A] models a mutable reference to a value of type A.
    The two basic operations are set, which fills the Ref with a new value, and get, which retrieves its current content.
    All operations on a Ref are atomic and thread-safe, providing a reliable foundation for synchronizing concurrent programs.
   */

  val refValue: ZIO[Any, Nothing, Ref[Int]] = for {
    ref <- Ref.make(100)
    v1 <- ref.get
    v2 <- ref.set(v1 - 50)
  } yield ref

  //updating a Ref
  def repeat[E, A](n: Int)(io: IO[E, A]): IO[E, Unit] =
    Ref.make(0).flatMap { iRef =>
      def loop: IO[E, Unit] = iRef.get.flatMap { i =>
        if (i < n)
          io *> iRef.update(_ + 1) *> loop
        else
          IO.unit
      }
      loop
    }

  private def openFile(str: String): IO[IOException, Array[Byte]] = ???

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = ???
}
