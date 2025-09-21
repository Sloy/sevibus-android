import java.io.File
import java.util.Properties

object VersionConfig {
    private val versionProperties = Properties().apply {
        val versionFile = File("version.properties")
        if (versionFile.exists()) {
            load(versionFile.inputStream())
        } else {
            throw IllegalStateException("version.properties file not found")
        }
    }

    val major: Int = versionProperties.getProperty("major").toInt()
    val minor: Int = versionProperties.getProperty("minor").toInt()
    val patch: Int = versionProperties.getProperty("patch").toInt()
    val snapshot: Boolean = versionProperties.getProperty("snapshot").toBoolean()

    val versionName: String = buildString {
        append("$major.$minor.$patch")
        if (snapshot) {
            append("-snapshot")
        }
    }

    val versionCode: Int = run {
        val majorPart = major.toString().padStart(3, '0')
        val minorPart = minor.toString().padStart(3, '0')
        val patchPart = patch.toString().padStart(3, '0')
        val snapshotPart = if (snapshot) "0" else "1"

        "$majorPart$minorPart$patchPart$snapshotPart".toInt()
    }
}