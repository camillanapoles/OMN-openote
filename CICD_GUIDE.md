# CI/CD and Deployment Guide

## Overview

This project now includes automated CI/CD workflows using GitHub Actions for building and deploying Android APKs automatically.

## Workflows

### 1. Android CI Build (`.github/workflows/android-build.yml`)

**Triggers:**
- Push to main, master, develop branches
- Push to any copilot/* branch
- Pull requests to main, master, develop
- Manual trigger (workflow_dispatch)

**What it does:**
- Builds Debug and Beta APK variants
- Runs unit tests
- Stores APK artifacts for 30 days
- Names APKs with version and commit SHA

**APK Files Generated:**
- `OpenMarkdownNotes-{version}-debug-{commit}.apk`
- `OpenMarkdownNotes-{version}-beta-{commit}.apk`

### 2. Release Workflow (`.github/workflows/release.yml`)

**Triggers:**
- Push a version tag (e.g., `git tag v0.35.1 && git push origin v0.35.1`)
- Manual trigger with version input

**What it does:**
- Builds Debug, Beta, and General APK variants
- Creates a GitHub Release
- Attaches all APK files to the release
- Stores artifacts for 90 days
- Generates release notes automatically

**APK Files Generated:**
- `OpenMarkdownNotes-{version}-debug-{commit}.apk`
- `OpenMarkdownNotes-{version}-beta-{commit}.apk`
- `OpenMarkdownNotes-{version}-general-{commit}.apk`

## How to Use

### Automatic Builds on Every Push

Simply push your code to any of the monitored branches:

```bash
git add .
git commit -m "Your changes"
git push
```

GitHub Actions will automatically build APKs and run tests.

### Creating a Release

To create a new release with versioned APKs:

```bash
# Tag the release
git tag v0.35.1
git push origin v0.35.1
```

This will:
1. Trigger the release workflow
2. Build all APK variants
3. Create a GitHub Release with the tag
4. Attach APKs to the release for download

### Manual Release

You can also trigger a release manually:

1. Go to the repository on GitHub
2. Click on "Actions" tab
3. Select "Create Release" workflow
4. Click "Run workflow"
5. Enter the version number (e.g., 0.35.1)
6. Click "Run workflow"

### Downloading APKs

#### From Workflow Runs:

1. Go to "Actions" tab
2. Click on a workflow run
3. Scroll down to "Artifacts" section
4. Download the APK artifacts

#### From Releases:

1. Go to "Releases" section
2. Find your release
3. Download APK files from "Assets" section

## APK Variants

### Debug APK
- **Purpose**: Development and debugging
- **Signing**: Debug keystore
- **Features**: Includes debug symbols, verbose logging
- **Identifier**: `.debug` suffix

### Beta APK
- **Purpose**: Beta testing with users
- **Signing**: Debug keystore (in CI), production keystore (for distribution)
- **Features**: Close to production but with beta markers
- **Identifier**: `.b` package suffix

### General APK
- **Purpose**: General release candidate
- **Signing**: Production keystore
- **Features**: Production-ready build
- **Identifier**: `.r` package suffix

## Version Numbering

The project uses the following version scheme:

- **Version Name**: Defined in `app/build.gradle` (e.g., "00.35.00")
- **Version Code**: Numeric version (e.g., 3500)
- **APK Filename**: Includes version name and git commit SHA

Example: `OpenMarkdownNotes-00.35.00-beta-a1b2c3d.apk`

## Testing APKs

### On Android Device:

1. Download the APK from GitHub Actions artifacts or Releases
2. Transfer to your Android device
3. Enable "Install from unknown sources" in Settings
4. Open the APK file to install
5. Grant necessary permissions when prompted

### Using ADB:

```bash
# Install APK
adb install OpenMarkdownNotes-*.apk

# If already installed, reinstall
adb install -r OpenMarkdownNotes-*.apk

# Check logs
adb logcat | grep OMN
```

## Build Requirements

The CI/CD workflow handles all build requirements automatically:

- JDK 17
- Node.js 18
- Gradle 8.3.2
- Android SDK (API 35)
- Mustache CLI
- Build dependencies (highlight.js, marked.js, Material Icons)

## Troubleshooting

### Build Failures

If a build fails:

1. Check the "Actions" tab for error logs
2. Review the specific step that failed
3. Common issues:
   - Missing dependencies (handled automatically by CI)
   - Test failures (check test results artifact)
   - Gradle configuration issues

### APK Installation Issues

If APK won't install on Android:

1. Check Android version compatibility (minimum API 17)
2. Uninstall previous version if package ID conflicts
3. Enable "Install from unknown sources"
4. Check device storage space

### Version Conflicts

If you see version conflicts:

1. Ensure version code is incremented in `app/build.gradle`
2. Use `-r` flag with adb install to replace existing app
3. Or uninstall the old version first

## Security Notes

- Debug APKs use a debug keystore (not secure for distribution)
- Production releases should use a secure keystore
- Store keystore credentials as GitHub Secrets for production use
- Never commit keystore files or passwords to the repository

## Future Enhancements

- Add automated testing on emulators
- Deploy to Google Play Store automatically
- Add F-Droid release automation
- Implement code quality checks
- Add security scanning
