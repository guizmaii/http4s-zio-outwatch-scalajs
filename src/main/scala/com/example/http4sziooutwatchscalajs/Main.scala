package com.example.http4sziooutwatchscalajs

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    Http4sziooutwatchscalajsServer.stream[IO].compile.drain.as(ExitCode.Success)
}