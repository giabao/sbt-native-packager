enablePlugins(JavaServerAppPackaging)

name := "rpm-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test rpm package"

packageDescription := "Description"

rpmRelease := "1"

rpmVendor := "typesafe"

rpmUrl := Some("http://github.com/sbt/sbt-native-packager")

rpmLicense := Some("BSD")

rpmGroup := Some("test-group")

rpmDaemonLogFile := "test.log"

mainClass in (Compile, run) := Some("com.example.MainApp")

TaskKey[Unit]("unzipAndCheck") <<= (baseDirectory, packageBin in Rpm, streams) map { (baseDir, rpmFile, streams) =>
    val rpmPath = Seq(rpmFile.getAbsolutePath)
    Process("rpm2cpio" , rpmPath) #| Process("cpio -i --make-directories") ! streams.log
    val scriptlets = Process("rpm -qp --scripts " + rpmFile.getAbsolutePath) !! streams.log
    assert(scriptlets contains "addGroup rpm-test", "addGroup not present in \n" + scriptlets)
    assert(scriptlets contains "addUser rpm-test", "Incorrect useradd command in \n" + scriptlets)
    assert(scriptlets contains "deleteGroup rpm-test", "deleteGroup not present in \n" + scriptlets)
    assert(scriptlets contains "deleteUser rpm-test", "deleteUser rpm not present in \n" + scriptlets)

    val startupScript = IO.read(baseDir / "etc" / "init.d" / "rpm-test")
    assert(startupScript contains
      """
        |INSTALL_DIR="/usr/share/rpm-test"
        |[ -n "${PACKAGE_PREFIX}" ] && INSTALL_DIR="${PACKAGE_PREFIX}/rpm-test"
        |cd $INSTALL_DIR
        |""".stripMargin, "Ensuring application is running on the install directory is not present in \n" + startupScript)
    assert(startupScript contains """logfile="test.log"""", "Setting key rpmDaemonLogFile not present in \n" + startupScript)

    // TODO check symlinks
    ()
}

TaskKey[Unit]("check-spec-file") <<= (target, streams) map { (target, out) =>
    val spec = IO.read(target / "rpm" / "SPECS" / "rpm-test.spec")
    assert(spec contains "addGroup rpm-test", "addGroup not present in \n" + spec)
    assert(spec contains "addUser rpm-test", "Incorrect useradd command in \n" + spec)
    assert(spec contains "deleteGroup rpm-test", "deleteGroup not present in \n" + spec)
    assert(spec contains "deleteUser rpm-test", "deleteUser rpm not present in \n" + spec)
    assert(spec contains
      """
        |if [ -e /etc/sysconfig/rpm-test ] ;
        |then
        |  sed -i 's/PACKAGE_PREFIX\=.*//g' /etc/sysconfig/rpm-test
        |fi
        |
        |if [ -n "$RPM_INSTALL_PREFIX" ] ;
        |then
        |  echo "PACKAGE_PREFIX=${RPM_INSTALL_PREFIX}" >> /etc/sysconfig/rpm-test
        |fi
        |""".stripMargin, "Persisting $RPM_INSTALL_PREFIX not present in \n" + spec)
    ()
}
