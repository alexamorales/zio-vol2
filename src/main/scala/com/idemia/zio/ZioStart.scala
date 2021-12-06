package com.idemia.zio
package com.idemia.zio

import zio.console.{Console, getStrLn, putStrLn}
import zio.{ExitCode, URIO, ZIO}

import java.io.IOException

object ZioStart extends zio.App {

  def run(args: List[String]): URIO[Console, ExitCode] =
    myAppLogic.exitCode

  val myAppLogic: ZIO[Console, IOException, Unit] =
    for {
      _ <- putStrLn("Hello! What is your name?")
      name <- getStrLn
      _ <- putStrLn(s"Hello, ${name}, welcome to ZIO!")
    } yield ()

}
