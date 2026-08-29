# Deployment Guide - TuyuL Online APK

## 📋 Release APK Signing Setup

This guide explains how to set up automatic signed APK releases using GitHub Actions.

### Prerequisites

- ✅ Release keystore: `keystore/tuyulonline-release-key.keystore` (already created)
- ✅ Keystore password: `TuyuL@2025`
- ✅ Key alias: `tuyulonline`
- ✅ Key password: `TuyuL@2025`

### 🔐 GitHub Secrets Configuration

To enable automatic signed APK builds on the `main` branch, configure these GitHub Secrets:

#### 1. **RELEASE_KEYSTORE_BASE64**
   - Encode the keystore file to base64:
   ```bash
   base64 -w 0 keystore/tuyulonline-release-key.keystore > /tmp/keystore.b64
   cat /tmp/keystore.b64
   ```
   - Copy the entire base64 output
   - Go to: GitHub Repository → Settings → Secrets and Variables → Actions → New repository secret
   - Name: `RELEASE_KEYSTORE_BASE64`
   - Value: Paste the base64 string

#### 2. **KEYSTORE_PASSWORD**
   - Name: `KEYSTORE_PASSWORD`
   - Value: `TuyuL@2025`

#### 3. **KEY_ALIAS**
   - Name: `KEY_ALIAS`
   - Value: `tuyulonline`

#### 4. **KEY_PASSWORD**
   - Name: `KEY_PASSWORD`
   - Value: `TuyuL@2025`

### 🔄 Build & Release Process

#### Automatic (GitHub Actions)
- **Trigger**: Push to `main` branch
- **Output**: Signed release APK
- **Location**: Actions → Latest run → Artifacts → `tuyulonline-release-apk`

#### Manual (Local)
```bash
# Build signed release APK
./gradlew assembleRelease \
  -PKEYSTORE_PASSWORD='TuyuL@2025' \
  -PKEY_ALIAS='tuyulonline' \
  -PKEY_PASSWORD='TuyuL@2025'

# Output: app/build/outputs/apk/release/app-release.apk
```

#### Verify Signature
```bash
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk
```

### 📱 Deployment Targets

#### Option 1: Firebase App Distribution
1. Install Firebase CLI: `npm install -g firebase-tools`
2. Authenticate: `firebase login`
3. Upload APK:
   ```bash
   firebase appdistribution:distribute app/build/outputs/apk/release/app-release.apk \
     --app 1:XXXXX:android:XXXXX
   ```

#### Option 2: Google Play Console
1. Go to [Google Play Console](https://play.google.com/console)
2. Select TuyuL Online app
3. Release → Production
4. Upload signed APK: `app/build/outputs/apk/release/app-release.apk`

#### Option 3: Direct Download
- Download from GitHub Actions artifacts
- Share signed APK via Firebase hosting or cloud storage

### 🔍 Troubleshooting

**Error: Keystore file not found**
- ✅ Verify keystore exists: `ls -la keystore/tuyulonline-release-key.keystore`
- ✅ Check path in [app/build.gradle](app/build.gradle#L24)

**Error: Invalid password**
- ✅ Verify secrets in GitHub: Settings → Secrets and Variables → Actions
- ✅ Re-encode keystore base64: `base64 -w 0 keystore/tuyulonline-release-key.keystore`

**Build fails with signature error**
- ✅ Keystore validity: `keytool -list -v -keystore keystore/tuyulonline-release-key.keystore -storepass TuyuL@2025`
- ✅ Validity: 10,000 days (expires in ~27 years)

### 📊 Version Management

Current release version:
- **versionCode**: 1
- **versionName**: "1.0"

To increment for next release:
```gradle
// In app/build.gradle
versionCode 2          // Integer, used internally
versionName "1.1"      // String, displayed to users
```

Then rebuild and upload to app stores.

### ✨ Next Steps

1. [✅] Configure GitHub Secrets (see above)
2. [⏳] Push a commit to `main` branch
3. [📊] Monitor build in GitHub Actions
4. [⬇️] Download signed APK from artifacts
5. [🚀] Deploy to Firebase or Play Store

---

**Last Updated**: 2026-08-29
