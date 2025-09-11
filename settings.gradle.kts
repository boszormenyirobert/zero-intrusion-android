pluginManagement {
    repositories {

        mavenCentral()
        gradlePluginPortal()
        google()
        maven { url = uri("https://jitpack.io") }
        flatDir {
            dirs("libs")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "ZeroIntrusion"
include(":app")
