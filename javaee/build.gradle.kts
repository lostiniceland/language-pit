import com.github.kittinunf.fuel.Fuel
import com.google.cloud.tools.jib.gradle.JibTask
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
	id("com.google.cloud.tools.jib") version "1.2.0"
//	id("info.solidsoft.pitest") version "1.4.0" doesnt work with Spock
}

version = "0.1.0"

repositories {
	mavenLocal()
	mavenCentral()
	jcenter()
}

val versionJEE = "8.0.1"
val versionSLF4J = "1.7.26"
val versionProtobuf = "3.8.0"
val versionProtobufJavaFormat = "1.4"
val versionZeebe = "0.20.0"
val versionAutoValue = "1.6"
val versionKafkaClient = "2.3.0"
val versionFlyway = "6.0.0-beta2"

val versionOpenlibertyEclipselink = "2.7.1"
val versionOpenlibertyJackson = "2.9.5"
val versionOpenlibertyTransactionApi = "1.1.25"

// testing
val versionJUnit = "4.+"
val versionGroovy = "2.5.+"
val versionSpock = "1.3-groovy-2.5"
val versionSpockReports = "1.6.1"
val versionCglib = "3.2.+"
val versionObjenesis = "3.0.+"
val versionEqualsVerifier = "3.+"


dependencies {
	compileOnly(group = "javax", name = "javaee-api", version = versionJEE)
	compileOnly(group = "org.eclipse.persistence", name = "eclipselink", version = versionOpenlibertyEclipselink)
	compileOnly(group = "com.ibm.websphere.appserver.api", name = "com.ibm.websphere.appserver.api.transaction", version = versionOpenlibertyTransactionApi)
	compile(group = "com.google.auto.value", name = "auto-value-annotations", version = versionAutoValue)
	annotationProcessor(group = "com.google.auto.value", name = "auto-value", version = versionAutoValue)
	compile(group = "com.google.protobuf", name = "protobuf-java", version = versionProtobuf)
	compile(group = "com.googlecode.protobuf-java-format", name = "protobuf-java-format", version = versionProtobufJavaFormat)
	compileOnly(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = versionOpenlibertyJackson)
	compile(group = "org.slf4j", name = "slf4j-api", version = versionSLF4J)
	compile(group = "org.slf4j", name = "slf4j-simple", version = versionSLF4J)
	compile(group = "org.apache.kafka", name = "kafka-clients", version = versionKafkaClient)
	compile(group = "org.flywaydb", name = "flyway-core", version = versionFlyway) // beta due to Postgres 11
	compile(group = "io.zeebe", name = "zeebe-client-java", version = versionZeebe)

	testCompile(group = "junit", name = "junit", version = versionJUnit)
	testCompile(group = "org.codehaus.groovy", name = "groovy-all", version = versionGroovy)
	testCompile(group = "org.spockframework", name = "spock-core", version = versionSpock)
	testCompile(group = "com.athaydes", name = "spock-reports", version = versionSpockReports) { isTransitive = false }
	testCompile(group = "cglib", name = "cglib-nodep", version = versionCglib)
	testCompile(group = "org.objenesis", name = "objenesis", version = versionObjenesis)
	testCompile(group = "nl.jqno.equalsverifier", name = "equalsverifier", version = versionEqualsVerifier)
	testCompile(group = "io.zeebe", name = "zeebe-test", version = versionZeebe)
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

	withType<JibTask> {
		dependsOn("prepareDocker")
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


jib {
	from.image = "open-liberty:webProfile8-java12"
	to.image = "languagepit/javaee-bikes:openliberty"
	container {
		appRoot = "/opt/ol/wlp/usr/servers/defaultServer/apps/bikes.war"
		ports = mutableListOf("8080")
	}
	extraDirectories {
		setPaths(mutableListOf(Paths.get(libertyDockerFolder)))
	}
}

//pitest {
// doesnt work with Spock
//	targetClasses = mutableSetOf("bikes.domain.*")  //by default "${project.group}.*"
//	threads = 4
//	outputFormats = mutableSetOf("XML", "HTML")
//	timestampedReports = false
//}

val libertyVersion = "19.0.0.7"
val libertyConfigFolder = "${project.projectDir}/config-resources-openliberty"
val libertyInstallFolder = "${project.projectDir}/server/wlp-$libertyVersion"
val libertyServerFolder = "$libertyInstallFolder/wlp/usr/servers/default"
val libertyServerPostgresFolder = "$libertyServerFolder/lib-postgres"

tasks {

	register("createServer") {
		group = "server setup"
		description = "Downloads and extracts a OpenLiberty server instance"

		outputs.dir("$libertyInstallFolder/")
		onlyIf {
			!File(libertyServerFolder).exists()
		}
		doLast {
			println("Download Openliberty from Maven-Central")
			val libertyDownloadZip = File(project.buildDir.absolutePath + "/wlp.zip")
			var url = "http://central.maven.org/maven2/io/openliberty/openliberty-javaee8/$libertyVersion/openliberty-javaee8-$libertyVersion.zip"
			Fuel.download(url)
					.fileDestination { _, _ -> libertyDownloadZip }
					.response()
			unzipTo(File(libertyInstallFolder), libertyDownloadZip)
		}
	}

	register("configurePostgres") {
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

	register("updateServer") {
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
				from(libertyConfigFolder)
				into("$libertyDockerFolder/opt/ol/wlp/usr/servers/defaultServer/")
				// add application-deployment within container at last line before </server>
				filter { line: String ->
					if (line.contains("</server>")) "  <application id=\"bikes\" context-root=\"/\" location=\"\${server.config.dir}/apps/bikes.war\"/>\n</server>" else line
				}
			}
			copy {
				from(libertyServerPostgresFolder)
				into("$libertyDockerFolder/opt/ol/wlp/usr/servers/defaultServer/lib-postgres")
			}
		}
	}
}