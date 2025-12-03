package expo.modules.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

class ExpoModulePublishPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply("maven-publish")

    project.afterEvaluate { project ->
      if (project.extensions.findByType(AndroidComponentsExtension::class.java) == null) {
        println("Skipping ${project.name} as it does not have AndroidComponentsExtension")
        return@afterEvaluate
      }

      if (project.extensions.findByType(LibraryExtension::class.java) == null) {
        println("Skipping ${project.name} as it does not have LibraryExtension")
        return@afterEvaluate

      }

      project.extensions.getByType(AndroidComponentsExtension::class.java).finalizeDsl {
        val libraryExtension = project.extensions.getByType(LibraryExtension::class.java)
        libraryExtension.applyPublishingVariant()

        // Move publication creation to after components are registered
        project.afterEvaluate {
          val publicationExtension = project.extensions.getByType(PublishingExtension::class.java)
          println("Configuring publishing for ${project.name}")

          println("List of components ${project.components.map { x -> x.name }}")
          println("groupId: ${project.group}")
          println("artifactId: ${libraryExtension.namespace}")
          println("version: ${libraryExtension.defaultConfig.versionName}")

          publicationExtension.publications.create(
            "release",
            MavenPublication::class.java
          ) { mavenPublication ->
            with(mavenPublication) {
              from(project.components.getByName("release"))
              groupId = project.group.toString()
              artifactId = requireNotNull(libraryExtension.namespace)
              version = requireNotNull(libraryExtension.defaultConfig.versionName)
            }
          }
        }
      }
    }
  }
}

class ExpoPublishPlugin : Plugin<Project> {
  override fun apply(rootProject: Project) {
    println("Applying ExpoPublishPlugin to ${rootProject.name}")

    rootProject.subprojects { project ->
      project.plugins.apply(ExpoModulePublishPlugin::class.java)
    }
  }
}


internal fun LibraryExtension.applyPublishingVariant() {
  publishing { publishing ->
    publishing.singleVariant("release") {
      withSourcesJar()
    }
  }
}
