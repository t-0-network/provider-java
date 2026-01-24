plugins {
    application
    id("com.gradleup.shadow") version "9.3.1"
    `maven-publish`
    signing
}

val picocliVersion = "4.7.7"

dependencies {
    implementation(project(":sdk"))
    implementation("info.picocli:picocli:$picocliVersion")
    annotationProcessor("info.picocli:picocli-codegen:$picocliVersion")
}

application {
    mainClass.set("network.t0.cli.InitCommand")
}

// Copy template files from starter/template to resources during build
tasks.register<Sync>("copyTemplateResources") {
    from("../starter/template")
    into(layout.buildDirectory.dir("resources/main/template"))
    exclude(
        ".gradle",
        "build",
        ".env",
        "libs/",
        "*.class",
        ".idea",
        "*.iml"
    )
}

tasks.processResources {
    dependsOn("copyTemplateResources")
}

// Generate version.properties with current version
tasks.register("generateVersionProperties") {
    val outputDir = layout.buildDirectory.dir("resources/main")
    outputs.dir(outputDir)
    doLast {
        val propsFile = outputDir.get().file("version.properties").asFile
        propsFile.parentFile.mkdirs()
        propsFile.writeText("version=${project.version}\n")
    }
}

tasks.processResources {
    dependsOn("generateVersionProperties")
}

// Shadow JAR configuration
tasks.shadowJar {
    archiveBaseName.set("provider-init")
    archiveClassifier.set("")
    archiveVersion.set("${project.version}")

    manifest {
        attributes(
            "Main-Class" to "network.t0.cli.InitCommand",
            "Implementation-Title" to "T-0 Provider Init",
            "Implementation-Version" to project.version
        )
    }

    // Minimize JAR size by excluding unused classes
    minimize {
        exclude(dependency("org.bouncycastle:.*:.*"))
    }
}

// Make shadowJar the default artifact
tasks.build {
    dependsOn(tasks.shadowJar)
}

// Publishing configuration
java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "provider-init"

            // Use shadow JAR as the main artifact
            artifact(tasks.shadowJar)
            artifact(tasks.named("sourcesJar"))

            pom {
                name.set("T-0 Provider Init CLI")
                description.set("CLI tool to initialize new T-0 Network provider projects")
                url.set("https://github.com/t-0/provider-sdk-java")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("t-0")
                        name.set("T-0 Network")
                        email.set("dev@t-0.network")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/t-0/provider-sdk-java.git")
                    developerConnection.set("scm:git:ssh://github.com/t-0/provider-sdk-java.git")
                    url.set("https://github.com/t-0/provider-sdk-java")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl

            credentials {
                username = System.getenv("OSSRH_USERNAME") ?: ""
                password = System.getenv("OSSRH_PASSWORD") ?: ""
            }
        }
    }
}

signing {
    // Try env var first, then file
    val keyFile = rootProject.file(".signing-key.gpg")
    val signingKey: String? = System.getenv("GPG_PRIVATE_KEY")
        ?: if (keyFile.exists()) keyFile.readText() else null

    if (signingKey != null) {
        useInMemoryPgpKeys(signingKey, "")
        sign(publishing.publications["maven"])
    }
}
