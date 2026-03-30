rootProject.name = "app-properties"

include("plugin", "lib")
project(":plugin").name = "app-properties-plugin"
project(":lib").name = "app-properties-lib"
