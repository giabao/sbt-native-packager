import com.typesafe.sbt.packager.linux.LinuxPackageMapping

enablePlugins(JavaServerAppPackaging)

name := "rpm-test"

version := "0.1.0"

maintainer := "David Pennell <dpennell@good-cloud.com>"

packageSummary := "Test rpm package"

packageName in Linux := "rpm-package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

rpmRelease := "1"

rpmVendor := "typesafe"

rpmUrl := Some("http://github.com/sbt/sbt-native-packager")

rpmLicense := Some("BSD")

packageArchitecture in Rpm:= "i386"

rpmSetarch := Some("i386")

linuxPackageMappings := {
  val helloMapping = LinuxPackageMapping(Seq(((resourceDirectory in Compile).value / "hello-32bit", "/usr/share/rpm-package/libexec/hello-32bit"))) withPerms "0755"
  linuxPackageMappings.value :+ helloMapping
}

TaskKey[Unit]("check-spec-file") <<= (target, streams) map { (target, out) =>
  val spec = IO.read(target / "rpm" / "SPECS" / "rpm-package.spec")
  out.log.success(spec)
  assert(spec contains "%attr(0644,root,root) /usr/share/rpm-package/lib/rpm-test.rpm-test-0.1.0.jar", "Wrong installation path\n" + spec)
  assert(spec contains "%attr(0755,root,root) /usr/share/rpm-package/libexec/hello-32bit", "Wrong 32-bit exe installation path\n" + spec)
  assert(spec contains "%attr(0755,root,root) /etc/init.d/rpm-package", "Wrong /etc/init.d path\n" + spec)
  assert(spec contains "%config %attr(644,root,root) /etc/default/rpm-package", "Wrong /etc default file\n" + spec)
  assert(spec contains "%dir %attr(755,rpm-package,rpm-package) /var/log/rpm-package", "Wrong logging dir path\n" + spec)
  assert(spec contains "%dir %attr(755,rpm-package,rpm-package) /var/run/rpm-package", "Wrong /var/run dir path\n" + spec)
  out.log.success("Successfully tested rpm-package file")
  ()
}

