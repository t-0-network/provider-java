import com.google.protobuf.gradle.*

plugins {
    `java-library`
    id("com.google.protobuf") version "0.9.6"
    id("me.champeau.jmh") version "0.7.3"
    `maven-publish`
    signing
}

val grpcVersion = "1.78.0"
val protobufVersion = "4.33.4"
val bouncyCastleVersion = "1.83"

dependencies {
    // gRPC dependencies
    api("io.grpc:grpc-okhttp:$grpcVersion")
    api("io.grpc:grpc-netty-shaded:$grpcVersion")
    api("io.grpc:grpc-protobuf:$grpcVersion")
    api("io.grpc:grpc-stub:$grpcVersion")


    // Protobuf
    api("com.google.protobuf:protobuf-java:$protobufVersion")

    // BouncyCastle for crypto (secp256k1, Keccak-256)
    implementation("org.bouncycastle:bcprov-jdk18on:$bouncyCastleVersion")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.17")

    // javax.annotation for generated code
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.2")
    testImplementation("org.assertj:assertj-core:3.27.6")
    testImplementation("io.grpc:grpc-testing:$grpcVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.25")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
            }
        }
    }
}

sourceSets {
    main {
        proto {
            srcDir("src/main/proto")
        }
    }
}

// Don't include proto source files in the JAR - only compiled classes are needed
tasks.named<ProcessResources>("processResources") {
    // The protobuf plugin adds proto files to resources for reflection support.
    // This causes duplicates when proto srcDir overlaps with resources.
    // Exclude proto files since we only need the compiled Java classes.
    exclude("**/*.proto")
}

// JMH benchmark configuration
jmh {
    warmupIterations.set(3)
    iterations.set(5)
    fork.set(1)
    // Uncomment for shorter runs during development:
    // warmupIterations.set(1)
    // iterations.set(1)
}

// Publishing configuration
java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<Javadoc> {
    // Suppress warnings for generated protobuf code
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "provider-sdk-java"
            from(components["java"])

            pom {
                name.set("T-0 Provider SDK")
                description.set("Java SDK for T-0 Network providers")
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
        sign(publishing.publications["mavenJava"])
    }
}
