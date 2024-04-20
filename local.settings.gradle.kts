val mapsCompose = Lib("../android-maps-compose", "com.google.maps.android:maps-compose" to ":maps-compose")

val localLibs = listOf<Lib>(
    //mapsCompose
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