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

        maven {
            setUrl("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/")
        } //카카오맵 v2저장소
    }
}

rootProject.name = "rootmap"
include(":app")
