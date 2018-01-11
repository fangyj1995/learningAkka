name := """akkademaid"""

version := "1.0"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  // Uncomment to use Akka
  //"com.typesafe.akka" % "akka-actor_2.11" % "2.3.9",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.7.0",
  "com.akkademy-db"   %% "akkademy-db"      % "0.0.1-SNAPSHOT",
  "com.typesafe.akka" % "akka-http-experimental_2.11" % "1.0-M4",
  "com.typesafe.akka" % "akka-http-core-experimental_2.11" % "1.0-M4",
  "com.syncthemall" % "boilerpipe" % "1.2.2",
  "junit"             % "junit"           % "4.12"  % "test",
  "com.novocode"      % "junit-interface" % "0.11"  % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.6" % "test"
)
