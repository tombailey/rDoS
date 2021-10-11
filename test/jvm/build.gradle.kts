plugins {
    kotlin("jvm") version "1.3.31"
}

apply(plugin = "kotlin")
apply(plugin = "application")

dependencies {
    compile(kotlin("stdlib-jdk8"))
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
}

repositories {
    mavenCentral()
}

tasks {
    withType<Jar> {
        manifest {
            attributes(mapOf(
                    "Main-Class" to "com.example.AppKt"
            ))
        }
        from(sourceSets.main.get().output) {
            include("**/App.class")
        }
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }
}
