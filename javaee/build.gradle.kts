import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.github.kittinunf.fuel.Fuel
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.gradle.kotlin.dsl.support.unzipTo
import java.nio.file.Files
import java.nio.file.Paths

buildscript {
	dependencies {
		classpath(group = "com.github.kittinunf.fuel", name = "fuel", version = "2.0.1")
		classpath(group = "com.github.kittinunf.result", name = "result", version = "2.1.0")
	}
}

plugins {
	java
	war
	groovy
	id("com.google.protobuf") version "0.8.8" 
	id("com.bmuschko.docker-remote-api") version "4.4.1"
}

version = "0.1.0"

repositories {
	mavenLocal()
	mavenCentral()
	jcenter()
}

val versionProtobuf = "3.6.1"


dependencies {
	compileOnly(group = "javax", name = "javaee-api", version = "8.0")
	compileOnly(group = "org.eclipse.persistence", name = "eclipselink", version = "2.7.1")
	compile(group = "com.google.auto.value", name = "auto-value-annotations", version = "1.6")
	annotationProcessor(group = "com.google.auto.value", name = "auto-value", version = "1.6")
	compile(group = "com.google.protobuf", name = "protobuf-java", version = versionProtobuf)
	compile(group = "com.googlecode.protobuf-java-format", name = "protobuf-java-format", version = "1.4")
	compileOnly(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.9.5")
	compile(group = "org.slf4j", name = "slf4j-api", version = "1.7.26")
	compile(group = "org.slf4j", name = "slf4j-simple", version = "1.7.26")
	compile(group = "org.apache.kafka", name = "kafka-clients", version = "2.0.0")
	compile(group = "org.flywaydb", name = "flyway-core", version = "6.0.0-beta") // beta due to Postgres 11

	testCompile(group = "junit", name = "junit", version = "4.+")
	testCompile(group = "org.codehaus.groovy", name = "groovy-all", version = "2.5.+")
	testCompile(group = "org.spockframework", name = "spock-core", version = "1.2-groovy-2.5")
	testCompile(group = "nl.jqno.equalsverifier", name = "equalsverifier", version = "3.1.5")
}


configure<JavaPluginConvention> {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}


tasks {
	withType<JavaCompile> {
		dependsOn("generateProto")
	}
}


sourceSets.main {
	java {
		srcDir("build/generated/source/proto/main/java")
	}
}


protobuf {
	// Configure the protoc executable
	protoc {
		// Download from repositories
		artifact = "com.google.protobuf:protoc:$versionProtobuf"
	}
}


val libertyDockerFolder = "$buildDir/docker/openliberty"
val libertyConfigFolder = "${project.projectDir}/config-resources-openliberty"
val libertyInstallFolder = "${project.projectDir}/server"
val libertyServerFolder = "$libertyInstallFolder/wlp/usr/servers/default"
val libertyServerPostgresFolder = "$libertyServerFolder/lib-postgres"

tasks {

	register("createServer"){
		group = "server setup"
		description = "Downloads and extracts a OpenLiberty server instance and configures the Postgres jdbc-driver"

		outputs.dir("$libertyInstallFolder/wlp")

		doLast{
			println("Download Openliberty from Maven-Central")
			val libertyDownloadZip = File(project.buildDir.absolutePath + "/wlp.zip")
			Fuel.download("http://central.maven.org/maven2/io/openliberty/openliberty-javaee8/19.0.0.2/openliberty-javaee8-19.0.0.2.zip")
					.fileDestination { _, _ -> libertyDownloadZip }
					.response()
			unzipTo(File(libertyInstallFolder), libertyDownloadZip)
		}
	}

	register("configurePostgres"){
		group = "server setup"
		description = "Installs the Postgres-JDBC driver"
		dependsOn("createServer")

		val postgresDownloadJar = File("$libertyServerPostgresFolder/postgresql-42.2.5.jar")

		outputs.file(postgresDownloadJar)

		doLast {
			Files.createDirectories(Paths.get(libertyServerPostgresFolder))
			println("Download Postgres driver from Maven-Central")
			Fuel.download("http://central.maven.org/maven2/org/postgresql/postgresql/42.2.5/postgresql-42.2.5.jar")
					.fileDestination { _, _ -> postgresDownloadJar }
					.response()
		}
	}

	register("updateServer"){
		group = "server setup"
		description = "Copies the configureation-resources to the server directory"
		dependsOn("configurePostgres")
		doLast {
			copy {
				from(libertyConfigFolder)
				into(libertyServerFolder)
			}
		}
	}

	register("prepareDocker") {
		dependsOn(war)
		doLast {
			copy {
				from(war.get().archiveFile)
				from(libertyConfigFolder)
				into(libertyDockerFolder)
				rename { fileName ->
					fileName.replaceFirst(war.get().archiveFileName.get(), "bikes.war", false)
				}
			}
			copy {
				from(libertyServerPostgresFolder)
				into("$libertyDockerFolder/lib-postgres")
			}
		}
	}

	register<Dockerfile>("createDockerfile") {
		dependsOn("prepareDocker")
		destFile.set(project.file("$libertyDockerFolder/Dockerfile"))
		from("open-liberty:webProfile8")
		addFile("lib-postgres", "/config/lib-postgres")
		addFile("server.xml", "/config/")
		addFile("bikes.war", "/config/apps")
		exposePort(8080)
	}

	register<DockerBuildImage>("buildImage") {
		dependsOn("createDockerfile")
//            inputDir(tasks.name<Dockerfile>("createDockerfile").destFile.parentFile)
		inputDir.set(file(libertyDockerFolder))
		tags.add("languagepit/javaee-bikes:openliberty")
	}
}


