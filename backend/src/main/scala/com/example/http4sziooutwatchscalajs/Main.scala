package com.example.http4sziooutwatchscalajs

import cats.effect.{ Concurrent, ExitCode }
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.{ HttpApp, HttpRoutes }
import zio.console.putStrLn
import zio.{ App, RIO, Runtime, ZIO }

object Main extends App {
  import org.http4s.implicits._
  import zio.interop.catz._

  type AppEnvironment = Environment
  type AppTask[A]     = RIO[AppEnvironment, A]

  private def logged[F[_]: Concurrent](httpRoutes: HttpRoutes[F]): HttpRoutes[F] =
    Logger.httpRoutes(logHeaders = true, logBody = true)(httpRoutes)

  private def app: HttpApp[AppTask] =
    (
      logged(new HelloWorldRouter[AppEnvironment].routes)
    ).orNotFound

  private def server: ZIO[AppEnvironment, Throwable, Unit] =
    for {
      implicit0(runtime: Runtime[AppEnvironment]) <- ZIO.runtime[Environment]
      server <- BlazeServerBuilder[AppTask]
                 .bindHttp(8080, "0.0.0.0")
                 .withHttpApp(app)
                 .serve
                 .compile[AppTask, AppTask, ExitCode]
                 .drain
    } yield server

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    server.foldM(err => putStrLn(s"Execution failed with: $err") *> ZIO.succeed(1), _ => ZIO.succeed(0))

}
