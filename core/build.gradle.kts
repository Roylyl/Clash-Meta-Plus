import android.databinding.tool.ext.capitalizeUS
import com.github.kr328.golang.GolangBuildTask
import com.github.kr328.golang.GolangPlugin
import java.util.Properties

plugins {
    kotlin("android")
    id("com.android.library")
    id("kotlinx-serialization")
    id("golang-android")
}

val golangSource = file("src/main/golang/native")
val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}
val configuredGoRoot = localProperties.getProperty("go.dir")?.let { configuredPath ->
    if (configuredPath.isBlank()) {
        throw GradleException("go.dir in local.properties must point to a patched Go installation, or be omitted to use Go from PATH.")
    }
    rootProject.file(configuredPath.trim()).canonicalFile
}
val configuredGoExecutable = configuredGoRoot?.let { goRoot ->
    val executableName = if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) "go.exe" else "go"
    goRoot.resolve("bin/$executableName").also { executable ->
        if (!executable.isFile || !executable.canExecute()) {
            throw GradleException("Invalid go.dir in local.properties: expected an executable at $executable. Set go.dir to the patched Go installation root, not its bin directory.")
        }
    }
}

golang {
    sourceSets {
        create("alpha") {
            tags.set(listOf("foss","with_gvisor","cmfa"))
            srcDir.set(file("src/foss/golang"))
        }
        create("meta") {
            tags.set(listOf("foss","with_gvisor","cmfa"))
            srcDir.set(file("src/foss/golang"))
        }
        all {
            fileName.set("libclash.so")
            packageName.set("cfa/native")
        }
    }
}

android {
    productFlavors {
        all {
            externalNativeBuild {
                cmake {
                    arguments("-DGO_SOURCE:STRING=${golangSource}")
                    arguments("-DGO_OUTPUT:STRING=${GolangPlugin.outputDirOf(project, null, null)}")
                    arguments("-DFLAVOR_NAME:STRING=$name")
                }
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
}

dependencies {
    implementation(project(":common"))

    implementation(libs.androidx.core)
    implementation(libs.kotlin.coroutine)
    implementation(libs.kotlin.serialization.json)
}

afterEvaluate {
    tasks.withType(GolangBuildTask::class.java).forEach {
        it.inputs.dir(golangSource)
        if (configuredGoExecutable != null) {
            // Use an absolute executable because desktop IDEs may not inherit the shell PATH.
            it.executable = configuredGoExecutable.absolutePath
            it.environment("GOROOT", configuredGoRoot!!.absolutePath)
            it.environment("GOTOOLCHAIN", "local")
        }
    }
}

val abis = listOf("arm64-v8a" to "Arm64V8a", "armeabi-v7a" to "ArmeabiV7a", "x86" to "X86", "x86_64" to "X8664")

androidComponents.onVariants { variant ->
    val cmakeName = if (variant.buildType == "debug") "Debug" else "RelWithDebInfo"

    abis.forEach { (abi, goAbi) ->
        tasks.configureEach {
            if (name.startsWith("buildCMake$cmakeName[$abi]")) {
                dependsOn("externalGolangBuild${variant.name.capitalizeUS()}$goAbi")
                println("Set up dependency: $name -> externalGolangBuild${variant.name.capitalizeUS()}$goAbi")
            }
        }
    }
}
