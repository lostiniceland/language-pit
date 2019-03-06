import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc


plugins {
  java
  id ("com.google.protobuf") version "0.8.8"
}


repositories {
  mavenCentral()
}


dependencies {
  compile (group = "org.slf4j", name ="slf4j-simple", version = "1.7.25")
  compile (group ="com.google.protobuf", name ="protobuf-java", version ="3.6.1")
  compile (group ="com.googlecode.protobuf-java-format", name = "protobuf-java-format", version = "1.4")
  compile (group ="org.apache.kafka", name ="kafka-clients", version = "2.0.0")
}


configure<JavaPluginConvention> {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}


sourceSets {
  main {
    java.srcDirs("build/generated/source/proto/main/java")
  }
}


tasks {
  withType<JavaCompile> {
    dependsOn("generateProto")
  }
}


protobuf {
  // Configure the protoc executable
  protoc {
    // Download from repositories
    artifact = "com.google.protobuf:protoc:3.6.1"
  }
}
