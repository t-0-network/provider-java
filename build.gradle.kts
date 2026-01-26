plugins {
    java
    id("org.jreleaser") version "1.18.0"
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
        name.set("provider-java")
        description.set("Java SDK and CLI for T-0 Network providers")
        copyright.set("T-0 Network")
    }
    release {
        github {
            skipRelease.set(true)
            skipTag.set(true)
            changelog {
                enabled.set(false)
            }
        }
    }
    signing {
        active.set(org.jreleaser.model.Active.NEVER)
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active.set(org.jreleaser.model.Active.ALWAYS)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    sign.set(false)
                    stagingRepository("sdk/build/staging-deploy")
                    stagingRepository("cli/build/staging-deploy")
                }
            }
        }
    }
}
