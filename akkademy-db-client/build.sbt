name := """akkademy-db-client"""

version := "1.0"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "com.akkademy-db"   %% "akkademy-db"  % "0.0.1-SNAPSHOT",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.6" % "test",
  "junit"             % "junit"           % "4.11"  % "test",
  "com.novocode"      % "junit-interface" % "0.10"  % "test",
  "com.typesafe.akka" %% "akka-remote" % "2.3.6"
)
  
