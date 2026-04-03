pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "liegestuetz-app"

include(":app")

// Core modules (shared across features)
include(":core:core-common")
include(":core:core-domain")
include(":core:core-data")
include(":core:core-ui")

// Feature modules
include(":features:feature-auth")
include(":features:feature-home")
include(":features:feature-challenge")
include(":features:feature-profile")
