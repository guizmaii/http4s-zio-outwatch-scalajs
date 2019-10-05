ThisBuild / organization := "com.example"
ThisBuild / name := "http4s-zio-outwatch-scalajs"
ThisBuild / version := "0.0.1-SNAPSHOT"
// ThisBuild / scalaVersion := "2.13.1" // Can't use Scala 2.13 for now because of https://github.com/scala/bug/issues/11457
ThisBuild / scalaVersion := "2.12.10"

// #### Dependencies ####

val logback            = "ch.qos.logback"        % "logback-classic"   % "1.2.3"
val zio                = "dev.zio"               %% "zio"              % "1.0.0-RC14"
val `zio-cats-interop` = "dev.zio"               %% "zio-interop-cats" % "2.0.0.0-RC4"
val pureconfig         = "com.github.pureconfig" %% "pureconfig"       % "0.12.1"
val specs2             = "org.specs2"            %% "specs2-core"      % "4.7.1" % Test

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
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
)

// #### Projects ####

lazy val root =
  (project in file("."))
    .disablePlugins(RevolverPlugin)
    .aggregate(backend, frontend)
    .dependsOn(backend, frontend)

lazy val backend =
  project
    .enablePlugins(WebScalaJSBundlerPlugin, BuildEnvPlugin, JavaAppPackaging)
    .settings(commonSettings: _*)
    .settings(Revolver.enableDebugging(port = 5005, suspend = false)) // Activate debugging with the `reStart` command. Because it's handy. :)
    .settings(
      // scalacOptions := scalacOptions.value.filter(_ != "-Xfatal-warnings"),
      libraryDependencies ++= Seq(logback, specs2, zio, `zio-cats-interop`, pureconfig) ++ http4s,
    )
    .settings(
      /*
          #### Frontend related settings ####

          See: https://scalacenter.github.io/scalajs-bundler/getting-started.html#sbt-web
       */
      scalaJSProjects := Seq(frontend),
      Assets / pipelineStages := Seq(scalaJSPipeline),
      // -- Hack n°0 --
      // The two following settings change how and where the `sbt-web-scalajs-bundler` plugin produces the assets sources.
      // The default behavior is to produce a WebJar directory hierarchy.
      // It's problematic because this hierarchy contains the name and version of the project and
      // that's not easy to reference in our Http4s router we'll use to serve the assets.
      // So, we simplify the directory hierachy.
      // Everything will be in a "public" dir, accessible via the project resources.
      Assets / WebKeys.packagePrefix := "public/",
      Assets / WebKeys.exportedMappings := (
        for ((file, path) <- (Assets / mappings).value)
          yield file -> ((Assets / WebKeys.packagePrefix).value + path)
      ),
      // The project compilation will trigger the ScalaJS compilation
      Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
      // The `scalaJSPipeline` tends to be called too often with the prod mode ("fullOpt"), especially when using the Intellij sbt console.
      scalaJSPipeline / devCommands ++=
        Seq("~reStart", "~compile", "~test:compile", "set", "session", "*/*:dumpStructureTo")
    )

lazy val frontend =
  project
    .enablePlugins(ScalaJSBundlerPlugin)
    .disablePlugins(RevolverPlugin)
    .settings(commonSettings: _*)
    .settings(
      resolvers += "jitpack" at "https://jitpack.io",
      libraryDependencies ++= Seq(
        "com.github.outwatch.outwatch" %%% "outwatch"  % "a332851",
        "org.scalatest"                %%% "scalatest" % "3.0.8" % Test
      ),
      // Example of how to add a NPM dependency to the ScalaJS module.
      Compile / npmDependencies += "bulma" -> "0.7.5"
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
