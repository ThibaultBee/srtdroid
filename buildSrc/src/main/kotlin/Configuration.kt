object AndroidVersions {
    const val MIN_SDK = 21
    const val COMPILE_SDK = 35
}

object Publication {
    object Repository {
        val username: String?
            get() = Property.get(Property.CentralPortalUsername)
        val password: String?
            get() = Property.get(Property.CentralPortalPassword)
    }

    object Pom {
        const val PACKAGING = "aar"
        const val URL = "https://github.com/ThibaultBee/srtdroid"

        object Scm {
            const val CONNECTION = "scm:git:git://github.com/ThibaultBee/srtdroid.git"
            const val DEVELOPER_CONNECTION =
                "scm:git:ssh://github.com/ThibaultBee/srtdroid.git"
            const val URL = "https://github.com/ThibaultBee/srtdroid"
        }

        object License {
            const val NAME = "Apache License, Version 2.0"
            const val URL = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            const val DISTRIBUTION = "repo"
        }

        object Developer {
            const val URL = "https://github.com/ThibaultBee"
            const val NAME = "Thibault B."
        }
    }

    object Signing {
        val hasKey: Boolean
            get() = key != null && keyId != null && password != null

        val key: String?
            get() = Property.get(Property.GpgKey)
        val password: String?
            get() = Property.get(Property.GpgPassword)
        val keyId: String?
            get() = Property.get(Property.GpgKeyId)
    }
}