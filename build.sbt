ThisBuild / organization := "com.example"
ThisBuild / name := "http4s-zio-outwatch-scalajs"
ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.1"

// #### Dependencies ####

val logback            = "ch.qos.logback" % "logback-classic"   % "1.2.3"
val zio                = "dev.zio"        %% "zio"              % "1.0.0-RC13"
val `zio-cats-interop` = "dev.zio"        %% "zio-interop-cats" % "2.0.0.0-RC4"
val specs2             = "org.specs2"     %% "specs2-core"      % "4.7.1" % Test

val http4s = (
  (version: String) =>
    Seq(
      "org.http4s" %% "http4s-blaze-server" % version,
      "org.http4s" %% "http4s-blaze-client" % version,
      "org.http4s" %% "http4s-circe"        % version,
      "org.http4s" %% "http4s-dsl"          % version,
    )
)("0.21.0-M5")

// #### Settings ####

val commonSettings = Seq(
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
  libraryDependencies ++= Seq(logback, specs2, zio, `zio-cats-interop`) ++ http4s,
)

// #### Projects ####

lazy val root =
  (project in file("."))
    .disablePlugins(RevolverPlugin)
    .aggregate(backend, frontend)
    .dependsOn(backend, frontend)

lazy val backend =
  project
    .settings(commonSettings: _*)
    .settings(
      // scalacOptions := scalacOptions.value.filter(_ != "-Xfatal-warnings"),
    )

lazy val frontend =
  project
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      resolvers += "jitpack" at "https://jitpack.io",
      libraryDependencies ++= Seq(
        "com.github.outwatch.outwatch" %%% "outwatch"  % "a332851",
        "org.scalatest"                %%% "scalatest" % "3.0.8" % Test
      )
    )
    .settings(
      scalacOptions += "-P:scalajs:sjsDefinedByDefault",
      useYarn := true, // makes scalajs-bundler use yarn instead of npm
      requireJsDomEnv in Test := true,
      scalaJSUseMainModuleInitializer := true,
      // configure Scala.js to emit a JavaScript module instead of a top-level script
      scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
      // https://scalacenter.github.io/scalajs-bundler/cookbook.html#performance
      webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly()
    )
