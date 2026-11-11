plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

group = "io.github.alexosnach"
version = "1.0.2"

android {
    namespace = "io.github.alexosnach.analyticskit"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    // Only for `LocalAnalytics`; the rest of the library does not use Compose.
    api(libs.androidx.compose.runtime)

    testImplementation(libs.junit)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            artifactId = "analyticskit"
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
