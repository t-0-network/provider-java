# CLAUDE.md - Critical Project Requirements

## ⚠️ CRITICAL CRYPTOGRAPHIC REQUIREMENT - READ FIRST ⚠️

**SIGNATURE VERIFICATION AND SIGNING MUST USE RAW PAYLOAD BYTES**

This is the single most important requirement in this entire codebase. Failure to follow this will break the entire SDK.

### The Problem

Due to differences in protobuf message versions between client and server, re-encoding a message does NOT produce identical binary output. The binary representation can differ even when the logical message content is the same.

### The Requirement

1. **Signature Verification (Server/Interceptor Side):**
   - You **MUST** verify signatures against the **raw bytes received in the request**
   - You **MUST NOT** deserialize the message and then re-serialize it for verification
   - The interceptor must capture the original wire bytes before any deserialization occurs

2. **Signature Generation (Client Side):**
   - You **MUST** calculate signatures over the **actual outgoing payload bytes**
   - You **MUST NOT** encode the message twice (once for signing, once for sending)
   - Sign the exact bytes that will be transmitted on the wire

### Why This Matters

- Protobuf encoding is not canonicalized - field ordering, unknown fields, and encoding choices can vary
- Different protobuf library versions may encode the same logical message differently
- Re-encoding loses the original byte representation

### Implementation Pattern

```java
// WRONG - DO NOT DO THIS
Message msg = parseFrom(bytes);
byte[] toVerify = msg.toByteArray(); // Re-encoded - WILL FAIL
verifySignature(toVerify, signature);

// CORRECT - DO THIS
byte[] rawBytes = getOriginalRequestBytes(); // From interceptor/transport
verifySignature(rawBytes, signature); // Verify against original bytes
```

---

## Build Commands

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Build without tests
./gradlew build -x test
```

## Project Structure

- `sdk/` - Core SDK library
- `starter/template/` - Provider template project

## Code Style

- Java 17+
- Follow existing code patterns in the codebase
