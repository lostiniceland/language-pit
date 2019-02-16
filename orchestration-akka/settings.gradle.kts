pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.charlesahunt.scalapb") {
                repositories {
                    mavenLocal()
                }
                useModule("com.charlesahunt:scalapb-plugin:1.2.3")
            }
        }
    }
    repositories {
    }
}