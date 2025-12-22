# 📱 InternHub Mobile - APK Build Guide

This guide will help you build an Android APK file for the InternHub mobile application that can be distributed via MediaFire or other file-sharing services.

## 📋 Prerequisites

- Flutter SDK installed (version 3.x or higher)
- Android SDK and Android Studio installed
- Java JDK 17 or higher
- A physical Android device or emulator for testing

## 🔧 Pre-Build Configuration

### Step 1: Update API Endpoint

Before building, update the API endpoint to point to your Azure backend:

1. Open `mobile/lib/utils/constants.dart`
2. Update the `baseUrl` to your Azure backend URL:

```dart
class ApiConstants {
  // Replace with your Azure backend URL
  static const String baseUrl = 'https://internhub-backend.YOUR_REGION.azurecontainerapps.io/api';
  
  // ... rest of the file
}
```

### Step 2: Update App Information (Optional)

Edit `mobile/android/app/build.gradle.kts`:

```kotlin
android {
    namespace = "com.internhub.mobile"
    
    defaultConfig {
        applicationId = "com.internhub.mobile"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

### Step 3: Configure App Signing (For Release Build)

#### Option A: Using Android Studio (Recommended for beginners)

1. Open the `mobile/android` folder in Android Studio
2. Go to `Build` → `Generate Signed Bundle / APK`
3. Follow the wizard to create a keystore and sign your APK

#### Option B: Manual Keystore Creation

```bash
# Navigate to android/app directory
cd mobile/android/app

# Generate keystore
keytool -genkey -v -keystore internhub-release.keystore -alias internhub -keyalg RSA -keysize 2048 -validity 10000

# You'll be prompted for:
# - Keystore password (remember this!)
# - Key password (remember this!)
# - Your name, organization, etc.
```

Create `mobile/android/key.properties`:

```properties
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=internhub
storeFile=internhub-release.keystore
```

**⚠️ IMPORTANT: Never commit `key.properties` or `*.keystore` to Git!**

Update `mobile/android/app/build.gradle.kts` to use the keystore:

```kotlin
// Add at the top of the file
import java.util.Properties
import java.io.FileInputStream

// Load keystore properties
val keystorePropertiesFile = rootProject.file("key.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    // ... existing configuration
    
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

## 🏗️ Building the APK

### Option 1: Build Release APK (Recommended)

```bash
# Navigate to mobile directory
cd mobile

# Clean previous builds
flutter clean

# Get dependencies
flutter pub get

# Build release APK
flutter build apk --release

# Or build split APKs per ABI (smaller file sizes)
flutter build apk --split-per-abi --release
```

The APK will be located at:
- **Universal APK**: `mobile/build/app/outputs/flutter-apk/app-release.apk`
- **Split APKs** (if using --split-per-abi):
  - `mobile/build/app/outputs/flutter-apk/app-armeabi-v7a-release.apk` (32-bit ARM)
  - `mobile/build/app/outputs/flutter-apk/app-arm64-v8a-release.apk` (64-bit ARM - most modern phones)
  - `mobile/build/app/outputs/flutter-apk/app-x86_64-release.apk` (Intel 64-bit)

### Option 2: Build Debug APK (For Testing Only)

```bash
flutter build apk --debug
```

**Note:** Debug APKs are larger and slower. Only use for testing!

## 🎨 Optional: Customize App Icon and Name

### Update App Name

Edit `mobile/android/app/src/main/AndroidManifest.xml`:

```xml
<application
    android:label="InternHub"
    ...>
```

### Update App Icon

Replace the launcher icons in:
- `mobile/android/app/src/main/res/mipmap-*/ic_launcher.png`

Or use the flutter_launcher_icons package:

```bash
# Add to pubspec.yaml
dev_dependencies:
  flutter_launcher_icons: ^0.13.1

flutter_launcher_icons:
  android: true
  image_path: "assets/images/logo.png"

# Then run
flutter pub get
flutter pub run flutter_launcher_icons
```

## 📦 Testing the APK

### Test on Emulator

```bash
# List available emulators
flutter emulators

# Start an emulator
flutter emulators --launch <emulator_id>

# Install and run the APK
flutter install
```

### Test on Physical Device

1. Enable Developer Options on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   
2. Enable USB Debugging:
   - Settings → Developer Options → USB Debugging

3. Connect device via USB and install:

```bash
flutter devices  # Check if device is detected
flutter install  # Install the built APK
```

## 🌐 Uploading to MediaFire

### Option 1: Via Web Interface

1. Go to [MediaFire](https://www.mediafire.com/)
2. Create a free account (if you don't have one)
3. Upload the APK file: `app-release.apk`
4. Get the shareable link
5. Share the link with your users

### Option 2: Via MediaFire CLI

```bash
# Install MediaFire CLI
pip install mediafire-cli

# Login
mediafire login your-email@example.com

# Upload APK
mediafire upload mobile/build/app/outputs/flutter-apk/app-release.apk

# Get share link
mediafire share app-release.apk
```

### Alternative File Sharing Services:

- **Google Drive**: https://drive.google.com
- **Dropbox**: https://www.dropbox.com
- **WeTransfer**: https://wetransfer.com (no account needed)
- **FileTransfer.io**: https://filetransfer.io (no account needed)

## 📝 Creating Installation Instructions

Create a simple guide for your users:

```markdown
# InternHub Mobile App Installation

## Download
📥 [Download InternHub APK](YOUR_MEDIAFIRE_LINK)

## Installation Steps

1. **Download** the APK file from the link above
2. **Enable Unknown Sources**:
   - Go to Settings → Security
   - Enable "Install from Unknown Sources" or "Allow from this source"
3. **Install**:
   - Open the downloaded APK file
   - Tap "Install"
   - Wait for installation to complete
4. **Launch**:
   - Open InternHub from your app drawer
   - Login with your credentials

## Requirements
- Android 5.0 (Lollipop) or higher
- ~50MB storage space
- Internet connection

## Troubleshooting

**"App not installed"**
- Check if you have enough storage space
- Try uninstalling any previous version first

**"Installation blocked"**
- Make sure "Unknown Sources" is enabled
- Some devices require enabling it per-browser

## Support
For issues, contact: your-email@example.com
```

## 🔍 APK Information

### Check APK Size and Details

```bash
# View APK info
flutter build apk --analyze-size --release

# Get APK size
ls -lh mobile/build/app/outputs/flutter-apk/app-release.apk
```

### Optimize APK Size

If your APK is too large:

1. **Use split APKs**: `flutter build apk --split-per-abi --release`
2. **Remove unused resources**: Audit your assets
3. **Compress images**: Use WebP format
4. **Enable ProGuard** (code shrinking)

## ✅ Pre-Distribution Checklist

- [ ] API endpoint updated to Azure backend URL
- [ ] App tested on at least one physical device
- [ ] Release APK built and signed
- [ ] APK file size is reasonable (<50MB)
- [ ] App icon and name are correct
- [ ] Version number updated
- [ ] APK uploaded to MediaFire (or similar)
- [ ] Installation guide prepared
- [ ] Share link tested and working
- [ ] Backup of APK saved locally

## 🎯 For Your Presentation

**What to share:**
1. The MediaFire download link
2. Installation instructions document
3. Demo credentials (if needed)
4. Expected app size: ~20-40MB
5. Minimum Android version: 5.0 (21)

**Demo tips:**
- Test the download link beforehand
- Have the APK pre-installed on a device for live demo
- Prepare screenshots/screen recordings as backup
- Test with poor internet connection (common in presentations)

## 🆘 Common Issues and Solutions

### Build Fails with "Gradle Error"

```bash
# Clear Gradle cache
cd mobile/android
./gradlew clean

# Try building again
cd ..
flutter clean
flutter pub get
flutter build apk --release
```

### "Execution failed for task ':app:lintVitalRelease'"

Add to `mobile/android/app/build.gradle.kts`:

```kotlin
android {
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}
```

### "Keystore file not found"

Make sure the path in `key.properties` is correct:
- Use `internhub-release.keystore` if the file is in the `android/app` directory
- Use full path if the file is elsewhere

## 📞 Need Help?

- Flutter Documentation: https://docs.flutter.dev/deployment/android
- Stack Overflow: https://stackoverflow.com/questions/tagged/flutter
- Flutter Discord: https://discord.gg/flutter

---

**Ready to build? Run: `flutter build apk --release`** 🚀
