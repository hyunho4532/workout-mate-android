import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.workoutmate.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.workoutmate.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        // AI 루틴 서버 주소. USB로 연결한 기기에서 "adb reverse tcp:8788 tcp:8788" 후 사용.
        // 와이파이로 붙이려면 gradle.properties에 workout.serverUrl=http://<PC IP>:8788
        val serverUrl = (project.findProperty("workout.serverUrl") as String?) ?: "http://localhost:8788"
        buildConfigField("String", "SERVER_URL", "\"$serverUrl\"")
    }

    buildTypes {
        debug {
            // 디버그 빌드는 서버 없이 앱 안에서 샘플 루틴을 만듭니다 (요금 0원).
            // 서버(실제 Claude)로 테스트하려면 gradle.properties에 workout.localMock=false
            val localMock = (project.findProperty("workout.localMock") as String?)?.toBoolean() ?: true
            buildConfigField("boolean", "LOCAL_MOCK", localMock.toString())
        }
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "LOCAL_MOCK", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
