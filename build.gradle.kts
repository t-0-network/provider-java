plugins {
    java
    id("org.jreleaser") version "1.22.0"
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

jreleaser {
    project {
        name = "provider-java"
        description = "Java SDK and CLI for T-0 Network providers"
        copyright = "T-0 Network"
    }
    release {
        github {
            skipRelease = true
            skipTag = true
            changelog {
                enabled = false
            }
        }
    }
    signing {
        active = org.jreleaser.model.Active.NEVER
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active = org.jreleaser.model.Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    sign = false
                    skipPublicationCheck = true
                    stagingRepository("sdk/build/staging-deploy")
                    stagingRepository("cli/build/staging-deploy")
                }
            }
        }
    }
}
