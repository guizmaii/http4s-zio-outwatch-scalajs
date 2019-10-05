# http4s-zio-outwatch-scalajs

This project is an example of how to integrate a ScalaJS / Outwatch module into a multi modules Http4s app, 
which use ZIO as its IO monad implementation, and serve the compiled assets with the Http4s server.

If you want to see how I serve the assets, just take a look at the `com.example.http4sziooutwatchscalajs.backend.FrontendRouter` code.

## Goals

 - Use the less possible hand crafted sbt configs to configure the sbt project.

The only "hack" in the ScalaJS config is that I modify the generated directory hierachy of the ScalaJS app.
For more details, see the comment flaggued as `-- Hack n°0 --` in the `build.sbt` file.

The ScalaJS sbt settings in the frontend module come from the generated Outwatch app. I didn't invented anything.
Just removed the one I don't need, as the Webpack ones for example.

 - Propose the simpler example possible, yet the more complete possible.

This project can be compiled to be put in production, as is.

 - Use the less possible hand crafted code.

less ad-hoc solution = Less code = less maintenance = less bug

 - Document each step

I took care that each commit is as atomic as possible so if you want to follow how I did everything, just look at the commits.

## Run in dev mode (with hot reload of both Scala and ScalaJS code)

In a *sbt console*:

`root> ~reStart`

## Build the app for a given env

In a *bash console*:

`$ sbt -Denv=prod clean stage`

Then to launch your app, in a *bash console*:

`$ ./backend/target/universal/stage/bin/backend`

## You think that this README can be improved ?
 
Feel free to:
 - talk to me on Twitter 
 - open open an issue
 - propose a PR 
 
I'm minimalist here because I think that the code talk to himself, but I can be wrong.

## Have an idea to improve this example ?

Feel free to:
 - talk to me on Twitter 
 - open open an issue
 - propose a PR 
 
 🙂
 
 
## Sources

 - https://http4s.org/
 - https://outwatch.github.io/?lang=scala
 - https://scalacenter.github.io/scalajs-bundler/
 - https://github.com/ChristopherDavenport/http4s-scalajsexample



Have fun!

Cheers,    
Jules