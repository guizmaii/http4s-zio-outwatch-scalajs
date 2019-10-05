package com.example.http4sziooutwatchscalajs.backend

import cats.effect.{ Blocker, Concurrent, ExitCode }
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.{ HttpApp, HttpRoutes }
import zio.blocking.Blocking
import zio.console.putStrLn
import zio.{ App, RIO, Runtime, Task, ZIO }

sealed trait Env extends Product with Serializable
object Env {
  case object Prod extends Env
  case object Dev  extends Env
  case object Test extends Env
}

object Main extends App {
  import cats.implicits._
  import org.http4s.implicits._
  import zio.interop.catz._

  type AppEnvironment = Environment
  type AppTask[A]     = RIO[AppEnvironment, A]

  private def logged[F[_]: Concurrent](httpRoutes: HttpRoutes[F]): HttpRoutes[F] =
    Logger.httpRoutes(logHeaders = true, logBody = true)(httpRoutes)

  private def app(env: Env)(implicit blocker: Blocker, runtime: Runtime[AppEnvironment]): HttpApp[AppTask] =
    (
      new FrontendRouter[AppEnvironment](env).routes <+>
        logged(new HelloWorldRouter[AppEnvironment].routes)
    ).orNotFound

  private def server: ZIO[AppEnvironment, Throwable, Unit] =
    for {
      implicit0(runtime: Runtime[AppEnvironment]) <- ZIO.runtime[Environment]
      implicit0(env: Env) <- ("dev" match { // TODO Jules
                              case "production" | "staging" => Env.Prod
                              case "dev"                    => Env.Dev
                              case "test"                   => Env.Test
                            }).pure[Task]
      implicit0(blocker: Blocker) <- ZIO
                                      .environment[Blocking]
                                      .flatMap(_.blocking.blockingExecutor)
                                      .map(_.asEC)
                                      .map(Blocker.liftExecutionContext)
      server <- BlazeServerBuilder[AppTask]
                 .bindHttp(8080, "0.0.0.0")
                 .withHttpApp(app(env))
                 .serve
                 .compile[AppTask, AppTask, ExitCode]
                 .drain
    } yield server

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    server.foldM(err => putStrLn(s"Execution failed with: $err") *> ZIO.succeed(1), _ => ZIO.succeed(0))

}
