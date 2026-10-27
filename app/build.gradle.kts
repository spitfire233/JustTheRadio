plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.justtheradio"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.justtheradio"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.all {
            it.jvmArgs("-javaagent:${mockitoAgent.asPath}")
        }
    }
}

val mockitoAgent = configurations.create("mockitoAgent")

ksp {
    arg("room.generateKotlin", "false")
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.material)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.radiobrowser4j)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.recyclerview)
    implementation(libs.datastore.preferences)
    implementation(libs.glide)
    implementation(libs.datastore.preferences.rxjava3)
    implementation(libs.core.splashscreen)
    implementation(libs.core.common)
    implementation(libs.core.runtime)
    implementation(libs.commons.validator)
    implementation(libs.glide.okhttp3.integration)
    implementation(libs.room.runtime)
    implementation(libs.paging.runtime)
    implementation(libs.paging.rxjava3)
    implementation(libs.room.paging)
    implementation(libs.okhttp)
    implementation(libs.lifecycle.reactivestreams)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.datasource.okhttp)
    implementation(libs.media3.ui)
    implementation(libs.media3.ui.compose.material3)
    implementation(libs.media3.session)
    implementation(libs.media3.exoplayer.hls)
    ksp(libs.room.compiler)
    annotationProcessor(libs.glide.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.espresso.core)
    testImplementation(libs.espresso.contrib)
    testImplementation(libs.core.testing)
    testImplementation(libs.mockito.core)
    add("mockitoAgent", libs.mockito.core.get().toString()) {
        isTransitive = false
    }
    androidTestImplementation(libs.ext.junit)
}