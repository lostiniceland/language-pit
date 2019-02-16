import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
	java
	war
	id("com.google.protobuf") version "0.8.8" apply false
	id("com.bmuschko.docker-remote-api") version "4.4.1" apply false
}



subprojects {
	version = "0.1.0"

	repositories {
		mavenLocal()
		mavenCentral()
		jcenter()
	}

	apply(plugin = "war")
	apply(plugin = "groovy")
	apply(plugin = "com.google.protobuf")
	apply(plugin = "com.bmuschko.docker-remote-api")


	dependencies {
		compileOnly(group = "javax", name = "javaee-api", version = "8.0")
		compileOnly(group = "org.eclipse.persistence", name = "eclipselink", version = "2.7.1")
		compile(group = "com.google.auto.value", name = "auto-value-annotations", version = "1.6")
		annotationProcessor(group = "com.google.auto.value", name = "auto-value", version = "1.6")
		compile(group = "com.google.protobuf", name = "protobuf-java", version = "3.5.1")
		compile(group = "com.googlecode.protobuf-java-format", name = "protobuf-java-format", version = "1.4")
		compileOnly(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.9.5")
		compile(group = "org.slf4j", name = "slf4j-api", version = "1.7.25")
		compile(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
		compile(group = "org.apache.kafka", name = "kafka-clients", version = "2.0.0")

		testCompile(group = "junit", name = "junit", version = "4.+")
		testCompile(group = "org.codehaus.groovy", name = "groovy-all", version = "2.4.+")
		testCompile(group = "org.spockframework", name = "spock-core", version = "1.1-groovy-2.4")
		testCompile(group = "nl.jqno.equalsverifier", name = "equalsverifier", version = "2.4.6")
	}


	configure<JavaPluginConvention> {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
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
			artifact = "com.google.protobuf:protoc:3.6.1"
		}
	}
}



project(":jbikes") {

	val libertyDockerFolder = "$buildDir/docker/openliberty"

	tasks {
		register("prepareDocker") {
			dependsOn(war)
			doLast {
				println(war.get().archiveFile)
				copy {
					from(war.get().archiveFile)
					from(project.projectDir.toString() + "/server")
					into(libertyDockerFolder)
					rename { fileName ->
						fileName.replaceFirst(war.get().archiveFileName.get(), "bikes.war", false)
					}
				}
				copy {
					from(project.projectDir.absolutePath + "/../derbyLib/")
					into("$libertyDockerFolder/derbyLib")
				}
			}
		}

		register<Dockerfile>("createDockerfile") {
			dependsOn("prepareDocker")
			destFile.set(project.file("$libertyDockerFolder/Dockerfile"))
			from("open-liberty:webProfile8")
			environmentVariable("DERBY_DB_LOCATION", "/tmp/jbikes-derby.db")
			addFile("server.xml", "/config/")
			addFile("bikes.war", "/config/apps")
			addFile("derbyLib", "/config/derbyLib")
			exposePort(8080)
		}

		register<DockerBuildImage>("buildImage") {
			dependsOn("createDockerfile")
//            inputDir(tasks.name<Dockerfile>("createDockerfile").destFile.parentFile)
			inputDir.set(file(libertyDockerFolder))
			tags.add("languagepit/javaee-bikes:openliberty")
		}
	}


}

project(":jwife") {


	dependencies {
		compile(group = "org.drools", name = "drools-core", version = "7.12.0.Final")
		compile(group = "org.drools", name = "drools-cdi", version = "7.12.0.Final")
		compile(group = "com.sun.xml.bind", name = "jaxb-xjc", version = "2.3.1")
		compile(group = "org.mapstruct", name = "mapstruct-jdk8", version = "1.2.0.Final")
		annotationProcessor(group = "org.mapstruct", name = "mapstruct-processor", version = "1.2.0.Final")
	}

	val libertyDockerFolder = "$buildDir/docker/openliberty"

	tasks {
		register("prepareDocker") {
			dependsOn(war)
			doLast {
				copy {
					from(war.get().archiveFile)
					from(project.projectDir.toString() + "/server")
					into(libertyDockerFolder)
					rename { fileName ->
						fileName.replaceFirst(war.get().archiveFileName.get(), "wife.war", false)
					}
				}
				copy {
					from(project.projectDir.absolutePath + "/../derbyLib/")
					into("$libertyDockerFolder/derbyLib")
				}
			}
		}

		register<Dockerfile>("createDockerfile") {
			dependsOn("prepareDocker")
			destFile.set(project.file("$libertyDockerFolder/Dockerfile"))
			from("open-liberty:webProfile8")
			environmentVariable("DERBY_DB_LOCATION", "/tmp/jwife-derby.db")
			addFile("server.xml", "/config/")
			addFile("wife.war", "/config/apps")
			addFile("derbyLib", "/config/derbyLib")
			exposePort(8080)
		}

		register<DockerBuildImage>("buildImage") {
			dependsOn("createDockerfile")
			inputDir.set(file(libertyDockerFolder))
			tags.add("languagepit/javaee-wife:openliberty")
		}
	}
}
