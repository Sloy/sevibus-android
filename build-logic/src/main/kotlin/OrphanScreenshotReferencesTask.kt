import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Finds the reference images no generated screenshot test uses anymore, because their preview was deleted,
 * renamed or lost its dark variant.
 *
 * It fails listing them, or deletes them when [delete] is set. It always writes them to [reportFile] for the CI.
 */
@DisableCachingByDefault(because = "Reads and deletes files in the source tree")
abstract class OrphanScreenshotReferencesTask : DefaultTask() {

    /** Expected references written by [GenerateScreenshotTestsTask]. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val expectedReferences: RegularFileProperty

    @get:Internal
    abstract val referenceDir: DirectoryProperty

    /** Base of the paths in messages and [reportFile], the repository root. */
    @get:Internal
    abstract val baseDir: DirectoryProperty

    @get:Input
    abstract val delete: Property<Boolean>

    /** Task suggested in the failure message to delete the orphans. */
    @get:Internal
    abstract val deleteTaskName: Property<String>

    /** Orphan references, one path relative to [baseDir] per line. */
    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    init {
        doNotTrackState("The reference images are edited outside of Gradle")
    }

    @TaskAction
    fun run() {
        val expected = expectedReferences.get().asFile.readLines().filter { it.isNotBlank() }.toSet()
        val root = referenceDir.get().asFile
        val referencePaths = root.walk().filter { it.isFile }.map { it.relativeTo(root).invariantSeparatorsPath }.toList()
        val orphans = ScreenshotTestsGenerator.findOrphans(expected, referencePaths).map { root.resolve(it) }
        val paths = orphans.map { it.relativeTo(baseDir.get().asFile).invariantSeparatorsPath }
        reportFile.get().asFile.writeText(paths.joinToString("") { "$it\n" })
        if (orphans.isEmpty()) return

        if (delete.get()) {
            orphans.forEach { it.delete() }
            logger.lifecycle("Deleted ${orphans.size} orphan screenshot reference(s):\n${paths.joinToString("\n")}")
        } else {
            throw GradleException(
                "${orphans.size} screenshot reference(s) don't belong to any @ScreenshotTest preview:\n" +
                    paths.joinToString("\n") +
                    "\nDelete them with ./gradlew ${deleteTaskName.get()}, " +
                    "or the update-screenshots label in a PR."
            )
        }
    }
}
