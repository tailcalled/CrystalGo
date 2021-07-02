ThisBuild / scalaVersion := "2.13.6"
ThisBuild / organization := "crystalgo"

// Unusual repository structure due to not having originally made it with sbt in mind
Compile / scalaSource := baseDirectory.value
Compile / javaSource := baseDirectory.value

// Again unusual repository structure requires explicitly copying server resources

lazy val copyServerImages = taskKey[Unit]("Copying images for the server application")
copyServerImages := {
  val src = (Compile / scalaSource).value / "server"
  val images = (src ** "*.png").get()
  val src_dest_pairs = images pair Path.rebase(src, (Compile / crossTarget).value / "classes")
  IO.copy(src_dest_pairs, CopyOptions.apply(overwrite = true, preserveLastModified = true, preserveExecutable = false))
}
(Compile / compile) := ((Compile / compile) dependsOn copyServerImages).value

// Need OS-specific dependencies for JavaFX natives
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map( m=>
  "org.openjfx" % s"javafx-$m" % "11" classifier osName
)



lazy val crystalGoClient = (project in file("client"))
  .settings(
    name := "CrystalGo Client"
  )

lazy val crystalGoServer = (project in file("server"))
  .settings(
    name := "CrystalGo Client"
  )
lazy val crystalGo = (project in file("."))
  .aggregate(crystalGoClient).dependsOn(crystalGoClient)
  .aggregate(crystalGoServer).dependsOn(crystalGoServer)
  .settings(
    name := "CrystalGo"
  )
