
plugins {
  scala
}


repositories {
  mavenCentral()
}


dependencies {
  compile (group = "org.slf4j", name = "slf4j-simple", version = "1.7.25")
  compile (group = "io.gatling", name = "gatling-core", version = "3.0.3")
}


configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

