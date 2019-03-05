import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc


plugins {
  groovy
  id ("com.google.protobuf") version "0.8.8"
  id ("com.avast.gradle.docker-compose") version "0.8.14"
}


repositories {
  mavenCentral()
}


dependencies {
  compile (group = "org.slf4j", name ="slf4j-simple", version = "1.7.25")
  compile (group ="com.google.protobuf", name ="protobuf-java", version ="3.6.1")
  compile (group ="com.googlecode.protobuf-java-format", name = "protobuf-java-format", version = "1.4")
  compile (group ="org.apache.kafka", name ="kafka-clients", version = "2.0.0")
  compile(group = "org.codehaus.groovy", name = "groovy-all", version = "2.5.+")
  compile (group="org.spockframework", name ="spock-core", version = "1.2-groovy-2.5")
  testCompile (group= "junit", name ="junit", version ="4.+")
  // Groovy RESTClient
  compile (group= "org.codehaus.groovy.modules.http-builder", name ="http-builder", version = "0.7.1")
  //compile group: =oauth.signpost=, name: =signpost-core=, version: =1.2.1.2=
  //compile group: =oauth.signpost=, name: =signpost-commonshttp=, version: =1.2.1.2=
}


configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}


sourceSets {
  main {
    java.srcDirs("build/generated/source/proto/main/java")
  }
  create("itest") {
    withConvention(GroovySourceSet::class) {
      compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
      runtimeClasspath += sourceSets["main"].output + sourceSets["test"].runtimeClasspath
      groovy.srcDirs("src/itest/groovy")
      resources.srcDir("src/itest/resources")
    }
  }
}


tasks {
  withType<JavaCompile> {
    dependsOn("generateProto")
  }


  register<Test>("integrationTest"){
    group = "verification"
    description = "Run integration tests"
    outputs.upToDateWhen {false}
    testClassesDirs = sourceSets["itest"].output.classesDirs
    classpath = sourceSets["itest"].runtimeClasspath
    testLogging {
      events("passed", "skipped", "failed")
    }
    systemProperty("SERVICE_HOST", "localhost") // adjust when running on non-native Docker host
    systemProperty("KAFKA_HOST", "tux") // adjust when running on non-native Docker host
    systemProperty("ORCHESTRATION_PORT", "9090") // Akka
//    systemProperty("ORCHESTRATION_PORT", "9095") // Camunda
    systemProperty("BIKES_PORT", "8080")
    systemProperty("WIFE_PORT", "8090")
  }

  register<Test>("integrationTestWithContainers"){
    group = "verification"
    description = "Run integration tests using Docker-Compose images"
    outputs.upToDateWhen {false}
    testClassesDirs = sourceSets["itest"].output.classesDirs
    classpath = sourceSets["itest"].runtimeClasspath
    testLogging {
      events("passed", "skipped", "failed")
    }
    systemProperty("SERVICE_HOST", "localhost") // adjust when running on non-native Docker host
    systemProperty("KAFKA_HOST", "tux") // adjust when running on non-native Docker host
//    systemProperty("ORCHESTRATION_PORT", "9090") // Akka
    systemProperty("BIKES_PORT", "8080")
  }
}


protobuf {
  // Configure the protoc executable
  protoc {
    // Download from repositories
    artifact = "com.google.protobuf:protoc:3.6.1"
  }
}


dockerCompose {
  // hooks "dependsOn composeUp" and "finalizedBy composeDown", and exposes environment variables and system properties (if possible)
  isRequiredBy(project.tasks["integrationTestWithContainers"])
  dockerComposeWorkingDirectory = "../../docker-compose"
  projectName = "testing" // use dedicated name in case another compose is running
  useComposeFiles = kotlin.collections.mutableListOf("javaee.yml")
  forceRecreate = true
}
