val mapsCompose = Lib("../android-maps-compose", "com.google.maps.android:maps-compose" to ":maps-compose")
val bottomsheet = Lib("../advanced-bottomsheet-compose", "io.morfly.compose:advanced-bottomsheet-material3" to ":advanced-bottomsheet-material3")
val composables = Lib("../compose-unstyled", "com.composables:core" to ":core")

val localLibs = listOf<Lib>(
    //mapsCompose
    //bottomsheet,
    //composables
)


localLibs.forEach {
    includeBuild(it.path) {
        dependencySubstitution {
            it.dependencies.forEach { (module, project) ->
                substitute(module(module)).using(project(project))
            }
        }
    }
}

class Lib(val path: String, vararg val dependencies: Pair<String, String>)
