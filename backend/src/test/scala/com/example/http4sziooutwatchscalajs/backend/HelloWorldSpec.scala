package com.example.http4sziooutwatchscalajs.backend

import org.http4s._
import org.specs2.matcher.MatchResult
import zio.console.Console
import zio.{ DefaultRuntime, RIO, ZIO }

class HelloWorldSpec extends org.specs2.mutable.Specification {
  import org.http4s.implicits._
  import zio.interop.catz._

  type TestEnv = Console
  type Task[A] = RIO[TestEnv, A]

  private val runtime = new DefaultRuntime {}
  private val service = new HelloWorldRouter[TestEnv]

  private def request(name: String): Request[Task] =
    Request[Task](Method.GET, Uri.fromString(s"/hello/$name").toOption.get)

  private def call(request: Request[Task]): Task[Response[Task]] = service.routes.orNotFound(request)

  private def run[T](task: ZIO[TestEnv, Throwable, T]): T = runtime.unsafeRun(task)

  "HelloWorld" >> {
    "return 200" >> {
      uriReturns200()
    }
    "return hello world" >> {
      uriReturnsHelloWorld()
    }
  }

  private[this] def uriReturns200(): MatchResult[Status] =
    run(call(request("World"))).status must beEqualTo(Status.Ok)

  private[this] def uriReturnsHelloWorld(): MatchResult[String] = {
    val name = "World"
    run(call(request(name)).flatMap(_.as[String])) must beEqualTo(s"""{"message":"Hello, $name"}""")
  }
}
