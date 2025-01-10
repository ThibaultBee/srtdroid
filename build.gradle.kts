// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URL

plugins {
    id(libs.plugins.android.application.get().pluginId).apply(false)
    id(libs.plugins.android.library.get().pluginId).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.dokka)
}

allprojects {
    group = "io.github.thibaultbee.srtdroid"
    version = "1.9.0"
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")

    tasks.withType<DokkaTaskPartial>().configureEach {
        dokkaSourceSets.configureEach {
            documentedVisibilities.set(
                setOf(
                    Visibility.PUBLIC,
                    Visibility.PROTECTED
                )
            )

            sourceLink {
                localDirectory.set(projectDir.resolve("src"))
                remoteUrl.set(URL("https://github.com/ThibaultBee/srtdroid/${project.name}/src"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}
