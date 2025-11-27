pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Esta es la línea importante para que funcione la librería:
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Mobiamigo"
include(":app")