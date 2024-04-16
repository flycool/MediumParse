import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://gitlab.com/api/v4/projects/38224197/packages/maven")
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)

    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.seleniumhq.selenium:selenium-java:4.19.1")

    implementation("com.github.winterreisender:webviewko:0.6.0")

}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "MediumParse"
            packageVersion = "1.0.0"

            modules("java.instrument", "java.management", "java.net.http", "java.scripting", "jdk.unsupported")
        }
    }
}

//tasks.jar {
//    manifest {
//        attributes["Main-Class"] = "MainKt"
//    }
//
//    // 下方的依赖打包可能会有重复文件，设置排除掉重复文件
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//
//    // 将依赖一起打包进jar
//    configurations["compileClasspath"].forEach { file: File ->
//        from(zipTree(file.absoluteFile))
//    }
//}
//
