
plugins {
  scala
  id("com.charlesahunt.scalapb") version "1.2.4"
  id("com.github.lkishalmi.gatling") version "3.0.2"
}


repositories {
  mavenCentral()
}

val versionSlf4j = "1+"
val versionScala = "2.13.0"
val versionGatling = "3.0.3"
val versionProtobuf = "3.8.0"
val versionScalaPb = "0.9.0"

dependencies {
  compile (group = "org.slf4j", name = "slf4j-simple", version = versionSlf4j)
  compile (group = "org.scala-lang", name = "scala-library", version = versionScala)
  compile (group = "com.google.protobuf", name = "protobuf-java", version = versionProtobuf)
  compile(group = "com.thesamet.scalapb", name = "scalapb-runtime_2.13", version = versionScalaPb)
  gatling(group = "com.thesamet.scalapb", name = "scalapb-json4s_2.13", version = "0.9.2")
//  gatling (group = "io.netty", name = "netty-tcnative", version = "2.0.20.Final")
}

tasks {
  withType<ScalaCompile> {
    dependsOn("scalapb")
  }
}


configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
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


