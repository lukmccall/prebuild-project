pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}

include(
  ":publish"
)

rootProject.name = "expo-prebuild-plugins"
