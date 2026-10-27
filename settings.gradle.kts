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

rootProject.name = "The Investowl"
include(":app")
include(":core:model")
include(":core:common")
include(":core:designsystem")
include(":core:network")
include(":core:database")
include(":feature:home")
include(":feature:auth")
include(":feature:ticker")
include(":feature:ownership")
include(":feature:konglo")
include(":feature:foreign")
include(":feature:whales")
include(":feature:signal")
include(":feature:backtest")
include(":feature:admin")
