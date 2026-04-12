// Pure JVM module — no Android framework dependency.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)

    // javax.inject allows @Inject constructor in pure-JVM domain classes
    // without pulling in Android or Hilt as a compile dependency.
    compileOnly(libs.javax.inject)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk.core)
    testImplementation(libs.turbine)
}

tasks.test {
    useJUnitPlatform()
}
