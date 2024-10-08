// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URL

plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.dokka)
}

allprojects {
    group = "io.github.thibaultbee.srtdroid"
    version = "1.8.3"
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
