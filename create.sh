#!/bin/bash
# T-0 Network Provider Starter - Java
# Creates a new provider project from the template

set -e

PROJECT_NAME=${1:-"my-provider"}

echo "🚀 T-0 Network Provider Starter - Java"
echo "======================================="
echo ""

# Check if java is available
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed or not in PATH"
    echo "Please install Java 17 or later and try again"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Error: Java 17 or later is required (found Java $JAVA_VERSION)"
    exit 1
fi

echo "✅ Java $JAVA_VERSION detected"
echo ""

# Create project directory
if [ -d "$PROJECT_NAME" ]; then
    echo "❌ Error: Directory '$PROJECT_NAME' already exists"
    exit 1
fi

mkdir -p "$PROJECT_NAME"
echo "📁 Created project directory: $PROJECT_NAME"

# Download template
echo "📦 Downloading template..."
TEMPLATE_URL="https://github.com/t-0-network/provider-starter-java/releases/latest/download/template.zip"
curl -sL "$TEMPLATE_URL" -o "$PROJECT_NAME/template.zip" 2>/dev/null || {
    echo "⚠️  Could not download template from GitHub, using local copy if available"
    # For local development, copy from the template directory
    if [ -d "starter/template" ]; then
        cp -r starter/template/* "$PROJECT_NAME/"
    else
        echo "❌ Error: Could not find template files"
        exit 1
    fi
}

# Extract if zip was downloaded
if [ -f "$PROJECT_NAME/template.zip" ]; then
    cd "$PROJECT_NAME"
    unzip -q template.zip
    rm template.zip
    cd ..
fi

echo "📦 Template extracted"

# Create .env from example
if [ -f "$PROJECT_NAME/.env.example" ]; then
    cp "$PROJECT_NAME/.env.example" "$PROJECT_NAME/.env"
    echo "📝 Created .env file from template"
fi

# Generate keypair
echo ""
echo "🔐 Generating secp256k1 keypair..."
echo ""

# Use openssl to generate a random 32-byte private key
PRIVATE_KEY=$(openssl rand -hex 32)

echo "Generated keypair:"
echo "Private key: $PRIVATE_KEY"
echo ""
echo "⚠️  IMPORTANT: Save your private key securely!"
echo "The public key will be displayed when you run the application."
echo ""

# Update .env with the private key
if [ -f "$PROJECT_NAME/.env" ]; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s/^PROVIDER_PRIVATE_KEY=.*/PROVIDER_PRIVATE_KEY=$PRIVATE_KEY/" "$PROJECT_NAME/.env"
    else
        sed -i "s/^PROVIDER_PRIVATE_KEY=.*/PROVIDER_PRIVATE_KEY=$PRIVATE_KEY/" "$PROJECT_NAME/.env"
    fi
    echo "📝 Updated .env with private key"
fi

echo ""
echo "✅ Project created successfully!"
echo ""
echo "Next steps:"
echo "  1. cd $PROJECT_NAME"
echo "  2. Get the NETWORK_PUBLIC_KEY from T-0 team and add it to .env"
echo "  3. Run: ./gradlew run"
echo "  4. Share your public key with T-0 team (displayed on startup)"
echo ""
echo "For more information, see the README.md in your project directory."
