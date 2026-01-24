#!/usr/bin/env bash
#
# T-0 Network Provider SDK - Java Project Initializer
#
# Usage: curl -fsSL https://raw.githubusercontent.com/t-0/provider-sdk-java/master/init.sh | bash
#
# This script downloads the init CLI from Maven Central and runs it.
# You can specify a version using the T0_SDK_VERSION environment variable:
#   T0_SDK_VERSION=1.0.0 curl -fsSL ... | bash
#

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
print_info "Checking prerequisites..."

# Check Java
if ! command -v java &> /dev/null; then
    print_error "Java is not installed. Please install Java 17 or later."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ] 2>/dev/null; then
    print_error "Java 17 or later is required. Found: Java $JAVA_VERSION"
    exit 1
fi
print_success "Java $JAVA_VERSION found"

# Get version (default to latest stable)
VERSION="${T0_SDK_VERSION:-1.0.0}"

# Maven Central artifact location
GROUP_PATH="network/t0"
ARTIFACT_ID="provider-init"
JAR_NAME="${ARTIFACT_ID}-${VERSION}.jar"
JAR_URL="https://repo1.maven.org/maven2/${GROUP_PATH}/${ARTIFACT_ID}/${VERSION}/${JAR_NAME}"

# Download location
TEMP_JAR="/tmp/t0-provider-init-${VERSION}.jar"

# Download CLI JAR
print_info "Downloading T-0 Provider Init CLI v${VERSION}..."

if curl -fsSL "$JAR_URL" -o "$TEMP_JAR" 2>/dev/null; then
    print_success "Downloaded CLI tool"
else
    print_error "Failed to download CLI tool from Maven Central."
    print_error "URL: $JAR_URL"
    print_error ""
    print_error "This could mean:"
    print_error "  - The version '$VERSION' doesn't exist"
    print_error "  - Maven Central is temporarily unavailable"
    print_error ""
    print_error "Try specifying a version: T0_SDK_VERSION=1.0.0 curl ... | bash"
    exit 1
fi

# Run the CLI tool
java -jar "$TEMP_JAR" "$@"
EXIT_CODE=$?

# Cleanup
rm -f "$TEMP_JAR"

exit $EXIT_CODE
