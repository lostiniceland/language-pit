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
val versionCamunda = "7.10.0"


dependencies {
	compileOnly(group = "javax", name = "javaee-api", version = "8.0")
	compileOnly(group = "org.eclipse.persistence", name = "eclipselink", version = "2.7.1")
	compileOnly(group = "com.ibm.websphere.appserver.api", name = "com.ibm.websphere.appserver.api.transaction", version = "1.1.25")
	compile(group = "com.google.auto.value", name = "auto-value-annotations", version = "1.6")
	annotationProcessor(group = "com.google.auto.value", name = "auto-value", version = "1.6")
	compile(group = "com.google.protobuf", name = "protobuf-java", version = versionProtobuf)
	compile(group = "com.googlecode.protobuf-java-format", name = "protobuf-java-format", version = "1.4")
	compileOnly(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.9.5")
	compile(group = "org.slf4j", name = "slf4j-api", version = "1.7.26")
	compile(group = "org.slf4j", name = "slf4j-simple", version = "1.7.26")
	compile(group = "org.apache.kafka", name = "kafka-clients", version = "2.0.0")
	compile(group = "org.flywaydb", name = "flyway-core", version = "6.0.0-beta") // beta due to Postgres 11
	compile(group = "org.camunda.bpm", name = "camunda-engine", version = versionCamunda)
	compile(group = "org.camunda.bpm", name = "camunda-engine-cdi", version = versionCamunda)

	testCompile(group = "junit", name = "junit", version = "4.+")
	testCompile(group = "org.codehaus.groovy", name = "groovy-all", version = "2.5.+")
	testCompile(group = "org.spockframework", name = "spock-core", version = "1.3-groovy-2.5")
	testCompile(group = "com.athaydes", name = "spock-reports", version = "1.6.1") { setTransitive(false) }
	testCompile(group = "cglib", name = "cglib-nodep", version = "3.2.10")
	testCompile(group = "org.objenesis", name = "objenesis", version = "3.0.1")
	testCompile(group = "nl.jqno.equalsverifier", name = "equalsverifier", version = "3.1.5")
	testCompile(group = "org.camunda.bpm.extension", name = "camunda-bpm-assert", version = "1.2")
	testCompile(group = "com.h2database", name = "h2", version = "1.4.198")
}


configure<JavaPluginConvention> {
	sourceCompatibility = JavaVersion.VERSION_12
	targetCompatibility = JavaVersion.VERSION_12
}


tasks {
	withType<JavaCompile> {
		dependsOn("generateProto")
		options.compilerArgs.add("--enable-preview")
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


val libertyVersion = "19.0.0.5"
val libertyDockerFolder = "$buildDir/docker/openliberty"
val libertyConfigFolder = "${project.projectDir}/config-resources-openliberty"
val libertyInstallFolder = "${project.projectDir}/server/wlp-$libertyVersion"
val libertyServerFolder = "$libertyInstallFolder/wlp/usr/servers/default"
val libertyServerPostgresFolder = "$libertyServerFolder/lib-postgres"

tasks {

	register("createServer"){
		group = "server setup"
		description = "Downloads and extracts a OpenLiberty server instance"

		outputs.dir("$libertyInstallFolder/")
		onlyIf {
			!File(libertyServerFolder).exists()
		}
		doLast{
			println("Download Openliberty from Maven-Central")
			val libertyDownloadZip = File(project.buildDir.absolutePath + "/wlp.zip")
			var url = "http://central.maven.org/maven2/io/openliberty/openliberty-javaee8/$libertyVersion/openliberty-javaee8-$libertyVersion.zip"
			Fuel.download(url)
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
		onlyIf {
			!File(libertyServerPostgresFolder).exists()
		}

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


