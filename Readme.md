# TuyuL Online - Android Application

A modern Android application built with Kotlin, featuring secure authentication and lifecycle management.

## 🚀 Quick Start

### Prerequisites
- Android Studio or VS Code with Android extension
- JDK 17+
- Android SDK 34 (API level 34)
- Gradle 8.7+

### Build & Run

#### Debug Build
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Or install directly to connected device
./gradlew installDebug
```

#### Release Build (Signed)
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Project Structure
```
TuyuLOnline/
├── app/
│   ├── src/main/
│   │   ├── java/            # Kotlin source code
│   │   ├── res/             # Resources (layouts, drawables, strings)
│   │   └── AndroidManifest.xml
│   ├── build.gradle         # App-level configuration
│   └── proguard-rules.pro   # Code obfuscation rules
├── keystore/                # Release signing keystore
│   └── tuyulonline-release-key.keystore
├── gradle/                  # Gradle wrapper
├── build.gradle            # Project-level configuration
└── DEPLOYMENT.md           # Deployment & signing guide
```

## 📋 Build Configuration

### Android Settings
- **compileSdk**: 34
- **minSdk**: 28
- **targetSdk**: 34
- **Language**: Kotlin 1.9.24
- **Java Target**: 17

### Dependencies
- AndroidX (Core, AppCompat, Lifecycle, Biometric, Security)
- Material Design Components
- Constraint Layout
- RecyclerView
- JUnit & Espresso (testing)

### Build Types
- **debug**: Debuggable, unoptimized
- **release**: Signed, minify-ready

## 🔐 Release APK Signing

This project is configured with automatic APK signing:

- **Keystore**: `keystore/tuyulonline-release-key.keystore` (RSA 2048-bit, 10,000 days validity)
- **Alias**: `tuyulonline`
- **Configuration**: [app/build.gradle](app/build.gradle#L24-L30)
- **Passwords**: Stored in GitHub Secrets (CI/CD)

**→ See [DEPLOYMENT.md](DEPLOYMENT.md) for full signing & deployment instructions**

## 🔄 Continuous Integration

GitHub Actions automatically:
- ✅ Build release APK on push to `main`
- ✅ Sign APK with keystore
- ✅ Upload artifact for deployment

**Workflow**: [.github/workflows/build.yml](.github/workflows/build.yml)

### GitHub Secrets Required
Configure these in your repository settings:
- `RELEASE_KEYSTORE_BASE64` - Base64-encoded keystore
- `KEYSTORE_PASSWORD` - Keystore password
- `KEY_ALIAS` - Signing key alias
- `KEY_PASSWORD` - Signing key password

[Setup Guide →](DEPLOYMENT.md#-github-secrets-configuration)

## 📦 Deployment

Choose your deployment target:

### Firebase App Distribution
- Internal testing, beta releases
- QR code sharing with testers

### Google Play Console
- Production release
- Staged rollout support

### Direct Download
- GitHub Actions artifacts
- Firebase Hosting or cloud storage

[Complete Guide →](DEPLOYMENT.md)

## 🛠️ Development

### Open in VS Code
```bash
code .
```

### Run Tests
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests
```

### Check Lint Issues
```bash
./gradlew lint
```

### Clean Build
```bash
./gradlew clean assembleDebug
```

## 📝 Gradle Tasks

```bash
# Build tasks
./gradlew assembleDebug         # Build debug APK
./gradlew assembleRelease       # Build signed release APK
./gradlew installDebug          # Install debug APK to device
./gradlew installRelease        # Install release APK to device

# Testing
./gradlew test                  # Run unit tests
./gradlew connectedAndroidTest # Run instrumented tests

# Analysis
./gradlew lint                  # Run lint checks
./gradlew lintRelease          # Lint for release build

# Cleanup
./gradlew clean                 # Remove build artifacts
./gradlew bundleRelease        # Build Android App Bundle
```

## 🐛 Troubleshooting

### Build fails with SDK not found
```bash
# Set Android SDK path
export ANDROID_HOME=/path/to/android/sdk
```

### Gradle sync fails
```bash
./gradlew --refresh-dependencies
```

### APK signature error
See [DEPLOYMENT.md - Troubleshooting](DEPLOYMENT.md#-troubleshooting)

## 📄 License

[Add your license here]

## 👤 Author

Bot Dev (@bot-ku2025)

---

**Last Updated**: 2026-08-29

