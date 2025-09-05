
plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"

}

group = "com.github.lazygamer1111"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.slf4j:slf4j-reload4j:2.0.17")
    implementation("ch.qos.reload4j:reload4j:1.2.26")

    implementation("io.avaje:avaje-config:4.0")

    implementation("io.javalin:javalin:6.5.0")

    implementation("org.jctools:jctools-core:4.0.5")

    implementation("com.pi4j:pi4j-core:${gradle.extra["pi4j-ver"]}")
    implementation("com.pi4j:pi4j-plugin-raspberrypi:${gradle.extra["pi4j-ver"]}")
    implementation("com.pi4j:pi4j-plugin-linuxfs:${gradle.extra["pi4j-ver"]}")
    implementation("com.pi4j:pi4j-plugin-gpiod:${gradle.extra["pi4j-ver"]}")
    implementation("com.fazecast:jSerialComm:[2.00,3.00)")
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "com.github.lazygamer1111.Main" // Change this to your main class
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // Prevent the -all suffix on the shadowjar file.
    }
}



tasks.test {
    useJUnitPlatform()
}