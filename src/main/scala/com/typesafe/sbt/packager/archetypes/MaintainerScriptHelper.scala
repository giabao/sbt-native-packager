package com.typesafe.sbt.packager.archetypes

import sbt._
import JavaAppPackaging.autoImport.{ maintainerScripts }

/**
 * == Maintainer Script Helper ==
 *
 * Provides utility methods to configure package maintainerScripts.
 */
trait MaintainerScriptHelper {

  /**
   * Use this method override preexisting configurations with custom file
   * definitions.
   *
   * @example {{{
   * import DebianConstants._
   * maintainerScripts in Debian := maintainerScriptsFromDirectory(
   *   sourceDirectory.value / DebianSource / DebianMaintainerScripts,
   *   Seq(Preinst, Postinst, Prerm, Postrm)
   * )
   * }}}
   * @param dir from where to load files
   * @param scripts - a list of script names that should be used
   * @return filename to content mapping
   */
  def maintainerScriptsFromDirectory(dir: File, scripts: Seq[String]): Map[String, Seq[String]] = {
    scripts.map(dir / _)
      .filter(_.exists)
      .map { script =>
	script.getName -> IO.readLines(script)
      }.toMap
  }

}