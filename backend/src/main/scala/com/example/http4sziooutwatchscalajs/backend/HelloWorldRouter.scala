package com.example.http4sziooutwatchscalajs.backend

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import zio.RIO

final class HelloWorldRouter[R] {
  import zio.interop.catz._

  type Task[A] = RIO[R, A]

  private val dsl: Http4sDsl[Task] = Http4sDsl[Task]
  import dsl._

  val routes: HttpRoutes[Task] =
    HttpRoutes.of[Task] {
      case GET -> Root / "hello" / name => Ok(s"""{"message":"Hello, $name"}""")
    }

}
