import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.archetypes.scripts.{ BashStartScriptPlugin, BatStartScriptPlugin }
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

/**
 *  Sets the build environment.
 *
 *  Inspired by from: https://sbt-native-packager.readthedocs.io/en/stable/recipes/package_configuration.html#sbt-parameters-and-build-environment
 *
 *  The proposed solution didn't work for me so I tweak it a bit.
 *  It's not a perfect solution but it's good enough for me.
 */
object BuildEnvPlugin extends AutoPlugin {

  // make sure it triggers automatically
  override def requires = JvmPlugin && BashStartScriptPlugin

  object autoImport {
    sealed trait BuildEnv extends Product with Serializable
    object BuildEnv {
      case object Production extends BuildEnv
      case object Staging    extends BuildEnv
      case object Test       extends BuildEnv
      case object Dev        extends BuildEnv
    }

    val buildEnv = settingKey[BuildEnv]("the current build environment")
  }
  import BashStartScriptPlugin.autoImport._
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    buildEnv := {
      import BuildEnv._
      sys.props
        .get("env")
        .orElse(sys.env.get("BUILD_ENV"))
        .flatMap {
          case "prod"       => Some(Production)
          case "production" => Some(Production)
          case "staging"    => Some(Staging)
          case "test"       => Some(Test)
          case "dev"        => Some(Dev)
          case _            => None
        }
        .getOrElse(Dev)
    },
    // Give feed back
    // Jules Remark: Overriding `onLoadMessage` comes from the `sbt-native-packager` example but I never saw it works, sadly.
    onLoadMessage := {
      // depend on the old message as well
      val defaultMessage = onLoadMessage.value
      val env            = buildEnv.value

      s"""|$defaultMessage
          |Running in build environment: $env""".stripMargin
    },
    bashScriptExtraDefines += {
      val conf =
        buildEnv.value match {
          case BuildEnv.Dev        => "application.conf"
          case BuildEnv.Test       => "test.conf"
          case BuildEnv.Staging    => "staging.conf"
          case BuildEnv.Production => "production.conf"
        }
      s"""addJava "-Dconfig.file=$${app_home}/../conf/$conf""""
    },
    mappings in Universal ++= {
      import com.typesafe.sbt.packager.MappingsHelper._

      val currentEnv = buildEnv.value

      contentOf((resourceDirectory in Compile).value).flatMap {
        case (_, "production.conf") if currentEnv != BuildEnv.Production => None
        case (_, "staging.conf") if currentEnv != BuildEnv.Staging       => None
        case (_, "test.conf") if currentEnv != BuildEnv.Test             => None
        case (file, fileName)                                            => Some(file -> s"conf/$fileName")
      }
    }
  )
}
