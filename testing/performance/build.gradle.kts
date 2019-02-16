
plugins {
  scala
  id("com.charlesahunt.scalapb") version "1.2.3"
  id("com.github.lkishalmi.gatling") version "3.0.2"
}


repositories {
  mavenCentral()
}

val versionSlf4j = "1+"
val versionScala = "2.12.8"
val versionGatling = "3.0.3"
val versionProtobuf = "3.6.1"
val versionScalaPb = "0.8.4"

dependencies {
  compile (group = "org.slf4j", name = "slf4j-simple", version = versionSlf4j)
  compile (group = "org.scala-lang", name = "scala-library", version = versionScala)
  gatling (group = "com.google.protobuf", name = "protobuf-java", version = versionProtobuf)
  gatling (group = "com.thesamet.scalapb", name = "scalapb-runtime_2.12", version = versionScalaPb)
  gatling (group = "com.thesamet.scalapb", name = "scalapb-json4s_2.12", version = "0.7.2")
//  gatling (group = "io.netty", name = "netty-tcnative", version = "2.0.20.Final")
}

tasks {
  withType<ScalaCompile> {
    dependsOn("scalapb")
  }
}


configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

val protobufCompileDir = "build/generated/source/proto/main/scala"

the<SourceSetContainer>()["gatling"].withConvention(ScalaSourceSet::class){
  scala {
    srcDir(protobufCompileDir)
  }
}


scalapbConfig {
  grpc = false
  projectProtoSourceDir = "../../protobuf/"
  targetDir = protobufCompileDir
  embeddedProtoc = true
}

