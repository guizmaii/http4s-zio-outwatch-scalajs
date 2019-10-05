package com.example.http4sziooutwatchscalajs.backend

import cats.effect.Blocker
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{ Charset, Headers, HttpRoutes, MediaType }
import zio.{ RIO, Runtime }

object FrontendRouter {

  private final val assetsDir             = "assets"
  private final val indexHeaders: Headers = Headers.of(`Content-Type`(MediaType.text.html, Charset.`UTF-8`))

  private final def scripts(env: Env): String =
    env match {
      case Env.Prod(_) =>
        s"""<script type="text/javascript" src="$assetsDir/frontend-opt-bundle.js"></script>"""
      case _ =>
        s"""
           |<script type="text/javascript" src="$assetsDir/frontend-fastopt-library.js"></script>
           |<script type="text/javascript" src="$assetsDir/frontend-fastopt-loader.js"></script>
           |<script type="text/javascript" src="$assetsDir/frontend-fastopt.js"></script>
           |""".stripMargin
    }

  private final def html(env: Env): String =
    s"""
       |<!DOCTYPE html>
       |<html>
       |<head>
       |  <meta charset="UTF-8">
       |  <title>Ze Awesome App</title>
       |  <link rel="shortcut icon" type="image/png" href="$assetsDir/images/favicon.png"/>
       |</head>
       |<body>
       |  <div id="app"></div>
       |
       |  ${scripts(env)}
       |</body>
       |</html>
       |""".stripMargin
}

final class FrontendRouter[R](env: Env)(implicit blocker: Blocker, runtime: Runtime[R]) {
  import FrontendRouter._
  import org.http4s.server.staticcontent._
  import zio.interop.catz._

  type Task[A] = RIO[R, A]

  private val dsl: Http4sDsl[Task] = Http4sDsl[Task]
  import dsl._

  private val resources: HttpRoutes[Task] =
    resourceService[Task](
      ResourceService.Config("/public", blocker, pathPrefix = s"/$assetsDir")
    )

  private val notFound = NotFound()

  val routes: HttpRoutes[Task] =
    HttpRoutes.of[Task] {
      case req @ GET -> Root / FrontendRouter.assetsDir / _ => resources.run(req).getOrElseF(notFound)
      case GET -> Root                                      => Ok(html(env)).map(_.withHeaders(indexHeaders))
    }

}
