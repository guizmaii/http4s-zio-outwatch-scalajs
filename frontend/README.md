frontend
====

## Prerequisites

You should make sure that the following components are pre-installed on your machine:

 - [Node.js](https://nodejs.org/en/download/)
 - [Yarn](https://yarnpkg.com/en/docs/install)

## Create a module
in sbt shell: `fastOptJS::webpack` or `fullOptJS::webpack`

## Working in dev mode
In sbt shell, run `dev`. Then open `http://localhost:8080/` in your browser.

This sbt-task will start webpack dev server, compile your code each time it changes
and auto-reload the page.  
webpack dev server will close automatically when you stop the `dev` task
(e.g by hitting `Enter` in the sbt shell while you are in `dev` watch mode).

If you existed ungracefully and your webpack dev server is still open (check with `ps -aef | grep -v grep | grep webpack`),
you can close it by running `fastOptJS::stopWebpackDevServer` in sbt.
