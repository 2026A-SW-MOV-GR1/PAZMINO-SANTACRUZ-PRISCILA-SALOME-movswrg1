buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(
            "com.google.android.libraries.mapsplatform.secrets-gradle-plugin:" +
                    "secrets-gradle-plugin:2.0.1"
        )
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}