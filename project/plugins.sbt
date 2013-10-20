//logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.0")

libraryDependencies ++= Seq(
  "com.puppycrawl.tools" %  "checkstyle"        % "5.5",
  "net.sourceforge.pmd"  %  "pmd"               % "5.0.0",
  "org.jacoco"           %  "org.jacoco.core"   % "0.5.9.201207300726" artifacts(Artifact("org.jacoco.core",   "jar", "jar")),
  "org.jacoco"           %  "org.jacoco.report" % "0.5.9.201207300726" artifacts(Artifact("org.jacoco.report", "jar", "jar"))
)

addSbtPlugin("de.johoop" % "findbugs4sbt" % "1.2.1")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.1")
