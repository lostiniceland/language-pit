import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
	java
	war
	id("com.google.protobuf") version "0.8.8"
	id("com.bmuschko.docker-remote-api") version "4.4.1"
	groovy
}

version = "0.1.0"

repositories {
	mavenLocal()
	mavenCentral()
	jcenter()
}


val versionCamunda = "7.10.0"



sourceSets.main {
	java {
		srcDirs("build/generated/source/proto/main/java")
	}
}

dependencies {
	compileOnly(group = "javax", name = "javaee-api", version = "8.0")
	compileOnly(group = "org.eclipse.persistence", name = "eclipselink", version = "2.7.1")
	compile(group = "com.google.auto.value", name = "auto-value-annotations", version = "1.6")
	compile(group = "com.google.protobuf", name = "protobuf-java", version = "3.6.1")
	compile(group = "com.googlecode.protobuf-java-format", name = "protobuf-java-format", version = "1.4")
	compileOnly(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.9.5")
	compile(group = "org.slf4j", name = "slf4j-api", version = "1.7.25")
	compile(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
	compile(group = "org.camunda.bpm", name = "camunda-engine", version = versionCamunda)
	compile(group = "org.camunda.bpm", name = "camunda-engine-cdi", version = versionCamunda)
	compile(group = "org.camunda.bpm.javaee", name = "camunda-ejb-client", version = versionCamunda)
	compile(group = "com.h2database", name = "h2", version = "1.4.197")
	compile(group = "org.apache.kafka", name = "kafka-clients", version = "2.0.0")

	testCompile(group = "junit", name = "junit", version = "4.12")
	testCompile(group = "org.codehaus.groovy", name = "groovy-all", version = "2.4.15")
	testCompile(group = "org.spockframework", name = "spock-core", version = "1.1-groovy-2.4")
	testCompile(group = "org.camunda.bpm.extension", name = "camunda-bpm-assert", version = "1.2")
}

configure<JavaPluginConvention> {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

val libertyDockerFolder = "$project/buildDir/docker/openliberty"

tasks {
	register("prepareDocker") {
		dependsOn(war)
		doLast {
			copy {
				from(war.get().archiveFile)
				from(project.projectDir.toString() + "/server")
				into(libertyDockerFolder)
				rename { fileName ->
					fileName.replaceFirst(war.get().archiveFileName.get(), "orchestration-camunda.war", false)
				}
			}
		}
	}

	register<Dockerfile>("createDockerfile") {
		dependsOn("prepareDocker")
		destFile.set(project.file("$libertyDockerFolder/Dockerfile"))
		from("open-liberty:webProfile8")
		environmentVariable("H2_DB_LOCATION", "/tmp/camunda.db")
		addFile("server.xml", "/config/")
		addFile("orchestration-camunda.war", "/config/apps")
		exposePort(9095)
	}

	register<DockerBuildImage>("buildImage") {
		dependsOn("createDockerfile")
		inputDir.set(file(libertyDockerFolder))
		tags.add("languagepit/orchestration:camunda")
	}
}


protobuf {
	// Configure the protoc executable
	protoc {
		// Download from repositories
		artifact = "com.google.protobuf:protoc:3.6.1"
	}
}
