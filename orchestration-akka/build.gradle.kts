import com.google.cloud.tools.jib.gradle.BuildDockerTask
import com.google.cloud.tools.jib.gradle.BuildImageTask

plugins {
	scala
	id("com.charlesahunt.scalapb") version "1.2.3"
	id("com.google.cloud.tools.jib") version "1.0.0"
}

version = "0.1.0"


val versionScala = "2.12.8"
val versionScalaTest = "3.0.5"
val versionScalaLogging = "3.5.0"
val versionAkka = "2.5.20"
val versionAkkaHttp = "10.1.7"
val versionLogback = "1.1.8"
val versionLevelDb = "0.10"
val versionProtobuf = "3.6.1"
val versionScalaKafka = "2.0.0"
val versionSpecs2 = "4.4.1"


repositories {
	mavenLocal()
	mavenCentral()
	jcenter()
	maven(url = "https://dl.bintray.com/cakesolutions/maven")
}



sourceSets {
	main {
		withConvention(ScalaSourceSet::class) {
			scala {
				srcDir("build/generated/source/proto/main/scala")
			}
		}
	}
}


dependencies {
	compile(group = "org.scala-lang", name = "scala-library", version = versionScala)
	compile(group = "com.typesafe.akka", name = "akka-http_2.12", version = versionAkkaHttp)
	compile(group = "com.typesafe.akka", name = "akka-stream_2.12", version = versionAkka)
	compile(group = "com.typesafe.akka", name = "akka-persistence_2.12", version = versionAkka)
	compile(group = "org.fusesource.leveldbjni", name = "leveldbjni-all", version = "1.8")
	compile(group = "com.google.protobuf", name = "protobuf-java", version = versionProtobuf)
	compile(group = "com.thesamet.scalapb", name = "scalapb-runtime_2.12", version = "0.7.4")
	compile(group = "com.esotericsoftware", name = "kryo", version = "4.0.2")
	compile(group = "com.github.romix.akka", name = "akka-kryo-serialization_2.12", version = "0.5.2")
	compile(group = "net.cakesolutions", name = "scala-kafka-client_2.12", version = versionScalaKafka)
	compile(group = "net.cakesolutions", name = "scala-kafka-client-akka_2.12", version = versionScalaKafka)
	testCompile(group = "junit", name = "junit", version = "4.+")
	testCompile(group = "org.scalatest", name = "scalatest_2.12", version = versionScalaTest)
	testCompile(group = "com.typesafe.akka", name = "akka-testkit_2.12", version = versionAkka)
	testCompile(group = "com.typesafe.akka", name = "akka-http-testkit_2.12", version = versionAkkaHttp)
	testCompile(group = "net.cakesolutions", name = "scala-kafka-client-testkit_2.12", version = versionScalaKafka)
	testCompile(group = "org.specs2", name = "specs2-core_2.12", version = versionSpecs2)
}

tasks {
	withType<ScalaCompile> {
		dependsOn("scalapb")
	}
	withType<BuildDockerTask> {
		dependsOn("compileScala")
	}
	withType<BuildImageTask> {
		dependsOn("compileScala")
	}

}

scalapbConfig {
	grpc = false
	projectProtoSourceDir = "../protobuf/"
	targetDir = "/build/generated/source/proto/main/scala"
	embeddedProtoc = true
}

jib {
	// since we only need a local image, only jibDockerBuild currently works
	from {
		image = "library/openjdk:8-jre-alpine"
//    image = "docker.sdvrz.de:5000/library/openjdk:8-jre-alpine"
		// use the following when behind a corporate proxy with a custom registry
		// image = "<your-custom-registry>/library/openjdk:8-jre-alpine"
	}
	to {
		image = "languagepit/orchestration:akka"
	}
	container {
		mainClass = "orchestration.Orchestration"
		ports = kotlin.collections.listOf("9090")
		jvmFlags = kotlin.collections.listOf("-Xms128m", "-Xdebug")
	}
}
