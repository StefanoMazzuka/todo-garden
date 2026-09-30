plugins { kotlin("jvm") }
kotlin { jvmToolchain(17) }
dependencies {
    implementation("com.badlogicgames.gdx:gdx:1.14.0")
    testImplementation(kotlin("test-junit"))
}
