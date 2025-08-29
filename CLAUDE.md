# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the 小红书 (XiaoHongShu) OAuth Login SDK for Android, which provides third-party applications with a way to integrate XiaoHongShu login functionality via App-to-App authorization flow. The SDK follows OAuth 2.0 Authorization Code flow with PKCE security enhancement.

**⚠️ IMPORTANT: This project is now fully written in Kotlin. All implementation should be in Kotlin unless specifically maintaining Java compatibility.**

## Build Commands

### Building the Project
```bash
./gradlew build                    # Build entire project (SDK + demo)
./gradlew :xhslogin:build         # Build only the SDK library
./gradlew :demo:build             # Build only the demo app
```

### Running Tests  
```bash
./gradlew test                    # Run all unit tests
./gradlew :xhslogin:test         # Run SDK unit tests only
./gradlew :demo:test             # Run demo app unit tests only
./gradlew connectedAndroidTest   # Run instrumented tests on connected device
```

### Lint and Code Quality
```bash
./gradlew lint                   # Run lint on entire project
./gradlew :xhslogin:lint        # Run lint on SDK only
```

### Clean Build
```bash
./gradlew clean                  # Clean all build artifacts
```

## Architecture Overview

The SDK is organized into two main modules:

### 1. Core SDK (`xhslogin` module)
- **`XHSLoginManager`**: Main singleton entry point for all SDK operations
- **`AuthManager`**: Core authorization logic handling App-to-App flow
- **Network Layer**: `ApiClient` and `AuthService` for OAuth token exchange and user info retrieval
- **Security Layer**: `PKCEHelper` for PKCE implementation, `CryptoUtils` for token encryption
- **Model Layer**: `AuthRequest/AuthResponse` for App-to-App communication, `XHSUser/XHSError` for public API
- **Utils**: `AppUtils` for XHS app detection, `StorageUtils` for encrypted local storage

### 2. Demo Application (`demo` module)
- Example integration showing complete OAuth flow (written in Kotlin)
- `XHSEntryActivity`: Required callback activity for App-to-App flow
- `MainActivity`: Demo UI showing login, user info retrieval, token refresh

## Key Architecture Patterns

### App-to-App Authorization Flow
The SDK uses Android Intent-based communication with XiaoHongShu app:
1. SDK creates `AuthRequest` with PKCE parameters
2. Launches XHS app via `ComponentName` targeting specific OAuth activity
3. XHS app returns `AuthResponse` via result Intent
4. SDK exchanges authorization code for access token via HTTPS

### Security Implementation
- **PKCE (Proof Key for Code Exchange)**: Prevents authorization code interception
- **State Parameter**: CSRF protection during OAuth flow
- **Token Encryption**: Local storage uses AES encryption via `CryptoUtils`
- **Component Verification**: Only allows launching verified XHS app components

### Network Architecture
- `ApiClient` handles all HTTPS communication with XHS OAuth endpoints
- Uses OkHttp for reliable networking with proper error handling
- All responses parsed via Gson with structured error handling
- Callbacks run on main thread for UI integration

## Development Notes

### Configuration Requirements
The SDK requires initialization with:
```kotlin
val loginManager = XHSLoginManager.getInstance()
loginManager.configure(context, appId, appSecret)
```

### Required AndroidManifest.xml Setup
Third-party apps must include `XHSEntryActivity` for callback handling:
```xml
<activity
    android:name=".XHSEntryActivity"
    android:exported="true"
    android:launchMode="singleTop"
    android:taskAffinity="${applicationId}.diff"
    android:theme="@android:style/Theme.Translucent.NoTitleBar" />
```

### Error Handling Strategy
- `XHSError` provides structured error codes for different failure scenarios
- Network errors, app not installed, user cancellation, and OAuth failures handled separately
- All errors include descriptive messages for debugging

### Storage Security
- Access tokens encrypted using `StorageUtils` with device-specific keys
- PKCE verifiers stored temporarily only during active authorization flow
- User data cached locally with encryption for offline access