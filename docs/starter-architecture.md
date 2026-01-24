# Starter System Architecture

This document describes how the project initialization system works for the T-0 Provider SDK.

## Overview

The starter system provides a one-liner experience for developers to bootstrap new provider projects:

```bash
curl -fsSL https://raw.githubusercontent.com/t-0/provider-sdk-java/master/init.sh | bash
```

## Components

### 1. Bootstrap Script (`init.sh`)

The entry point shell script that users execute via curl. It:

1. Checks prerequisites (Java 17+)
2. Downloads the CLI JAR from Maven Central
3. Executes the CLI with passed arguments
4. Cleans up the temporary JAR

**Version Selection:**
```bash
# Use default version
curl -fsSL .../init.sh | bash

# Use specific version
T0_SDK_VERSION=1.2.0 curl -fsSL .../init.sh | bash
```

### 2. CLI Module (`cli/`)

A self-contained Java CLI tool published to Maven Central as `network.t0:provider-init`.

**Structure:**
```
cli/
├── build.gradle.kts           # Shadow JAR + publishing config
└── src/main/java/network/t0/cli/
    ├── InitCommand.java       # Main entry point (picocli)
    ├── TemplateExtractor.java # Extracts template from JAR resources
    ├── KeyGenerator.java      # Generates secp256k1 keypair
    ├── EnvFileWriter.java     # Creates .env file
    └── Version.java           # Reads embedded version
```

**Key Features:**
- **Shadow JAR**: All dependencies bundled (SDK, BouncyCastle, picocli)
- **Embedded Template**: Template files are packaged as JAR resources
- **Placeholder Substitution**: `${PROJECT_NAME}` and `${SDK_VERSION}` replaced during extraction

### 3. Template Source (`starter/template/`)

The template project that gets extracted for new providers. Contains:

- Complete Gradle project structure
- Sample handler implementations with TODO markers
- Configuration files (build.gradle.kts, Dockerfile, etc.)

**Important:** This directory is kept in the repo for:
1. CI verification (ensures template compiles)
2. Development/testing of template changes
3. Documentation reference

### 4. Starter Build (`starter/build.gradle.kts`)

A Gradle build file for the starter module that provides:
- `generateKeys` task for local development
- Compilation checks for the template

## Build Process

### Template Packaging

During CLI build, template files are copied to resources:

```kotlin
// cli/build.gradle.kts
tasks.register<Sync>("copyTemplateResources") {
    from("../starter/template")
    into(layout.buildDirectory.dir("resources/main/template"))
    exclude(".gradle", "build", ".env", "libs/")
}
```

### Version Embedding

The SDK version is embedded in `version.properties`:

```kotlin
tasks.register("generateVersionProperties") {
    doLast {
        File("version.properties").writeText("version=${project.version}")
    }
}
```

### Shadow JAR

The CLI is packaged as a fat JAR with all dependencies:

```kotlin
tasks.shadowJar {
    archiveBaseName.set("provider-init")
    minimize {
        exclude(dependency("org.bouncycastle:.*:.*"))
    }
}
```

## Initialization Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                         User runs curl                          │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    init.sh (bootstrap script)                    │
│  1. Check Java version                                          │
│  2. Download provider-init-{version}.jar from Maven Central     │
│  3. Execute: java -jar provider-init.jar [args]                 │
│  4. Cleanup temp JAR                                            │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    InitCommand.java (CLI)                        │
│  1. Prompt for project name (if not provided)                   │
│  2. Validate and sanitize project name                          │
│  3. Create project directory                                    │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                 TemplateExtractor.java                           │
│  1. Read template files from JAR resources (/template/*)        │
│  2. Walk resource tree                                          │
│  3. Copy files, replacing placeholders:                         │
│     - ${PROJECT_NAME} → user's project name                     │
│     - ${SDK_VERSION}  → embedded SDK version                    │
│  4. Make gradlew executable                                     │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   KeyGenerator.java                              │
│  1. Generate 32 random bytes (SecureRandom)                     │
│  2. Create Signer from bytes (SDK)                              │
│  3. Derive public key                                           │
│  4. Return hex-encoded keypair                                  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   EnvFileWriter.java                             │
│  1. Create .env file with:                                      │
│     - PROVIDER_PRIVATE_KEY (generated)                          │
│     - NETWORK_PUBLIC_KEY (empty, user fills)                    │
│     - TZERO_ENDPOINT (default sandbox)                          │
│     - PORT, QUOTE_PUBLISHING_INTERVAL (defaults)                │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Git Initialization                             │
│  1. git init                                                    │
│  2. Add .env to .gitignore                                      │
│  3. git add . && git commit                                     │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Print Success Message                          │
│  - Show project path                                            │
│  - Display public key (for sharing with T-0 team)               │
│  - Show next steps                                              │
└─────────────────────────────────────────────────────────────────┘
```

## Placeholder Substitution

Template files with these extensions are processed for placeholders:
- `.java`, `.kt`, `.kts`, `.gradle`
- `.properties`, `.xml`, `.md`, `.txt`
- `.yaml`, `.yml`, `.json`, `.toml`
- `.sh`, `.bat`, `.env`
- `gradlew`, `Dockerfile`, `.gitignore`

**Placeholders:**

| Placeholder | Replaced With | Example |
|-------------|--------------|---------|
| `${PROJECT_NAME}` | User's project name | `my-provider` |
| `${SDK_VERSION}` | Current SDK version | `1.0.0` |

## Generated Project

The generated project uses the SDK from Maven Central:

```kotlin
// build.gradle.kts (generated)
dependencies {
    implementation("network.t0:provider-sdk-java:1.0.0")
    // ...
}
```

This means:
- No local `libs/sdk.jar` in generated projects
- Users get SDK updates by changing the version number
- Transitive dependencies are resolved automatically

## CI Verification

The CI pipeline verifies the entire flow:

```yaml
jobs:
  build:
    steps:
      # 1. Build SDK
      - run: ./gradlew :sdk:build

      # 2. Verify template compiles (catches syntax errors)
      - run: cd starter/template && ./gradlew build

      # 3. Build CLI
      - run: ./gradlew :cli:shadowJar

      # 4. E2E test: generate project and compile it
      - run: |
          java -jar cli/build/libs/provider-init.jar test-project --no-git
          cd test-project
          ./gradlew compileJava
```

## Development Workflow

### Modifying the Template

1. Edit files in `starter/template/`
2. Test locally:
   ```bash
   ./gradlew :cli:shadowJar
   java -jar cli/build/libs/provider-init-*.jar test-project --no-git
   cd test-project && ./gradlew build
   ```
3. Commit changes

### Adding New Placeholders

1. Add placeholder in template file: `${NEW_PLACEHOLDER}`
2. Update `TemplateExtractor.java` to replace it
3. Document in this file

### Testing Changes

```bash
# Quick test
./gradlew :cli:shadowJar && \
  rm -rf /tmp/test-project && \
  java -jar cli/build/libs/provider-init-*.jar /tmp/test-project --no-git && \
  cd /tmp/test-project && ./gradlew build
```

## Publishing

See [Maintainer Guide](../README.md#maintainer-guide) for release instructions.
