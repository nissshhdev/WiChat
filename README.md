# WiChat (Android)

**Package / Application ID:** `com.nissshh.wichat`  
**Launcher Name:** WiChat  
**Typography:** Bundled DM Sans offline typography  
**Design Reference:** Follows the 12 Light and 12 Dark screen design targets from `WiChat_Screen_Previews_DM_Sans_Light_Dark`.

WiChat is a native Android local-network messaging application built for people connected to the same Wi-Fi / Local Area Network. It allows direct peer-to-peer discovery and messaging **without depending on the public internet, cloud accounts, phone number registration, or external backend servers**.

---

## 1. Core Architecture & LAN Limitations

### Architecture
- **UI & Presentation:** 100% Jetpack Compose with Material 3 foundation, customized with WiChat design tokens (Light & Dark AMOLED themes, smooth color interpolation).
- **Architecture Pattern:** Unidirectional Data Flow (UDF) with `ViewModel` / `StateFlow` and decoupled `WiChatRepository` interface.
- **Local Persistence:** Room Database for profiles, contacts, requests, direct chats, groups, and stories. DataStore for preferences (theme, passcode verifier, reduce motion).
- **Security & Keystore:** 
  - Unique RSA-2048 identity keypair generated on first launch via **Android Keystore**.
  - Salted SHA-256 verifier for 6-digit passcode lock (no plaintext PINs stored or logged).
- **Discovery Layer:** Android Network Service Discovery (`NsdManager` / DNS-SD / mDNS) advertising `_wichat._tcp` with Wi-Fi multicast lock management.
- **Transport Layer:** Local TCP Sockets on dynamically bound ports using framed, versioned JSON packets (`WiChatPacket`).

### Fundamental LAN Limitation
> [!IMPORTANT]
> Because WiChat is a pure peer-to-peer local-network application with **zero cloud servers or push relays**:
> - An **offline recipient cannot receive a notification or message while their device is unreachable or disconnected from the Wi-Fi**.
> - WiChat displays truthful states: messages remain queued locally until the peer reconnects to the same Wi-Fi.
> - On networks with **AP / Client Isolation** enabled (e.g., some hotel, university, or guest Wi-Fi networks), the router blocks device-to-device traffic. WiChat includes built-in diagnostics explaining this state.

---

## 2. Project Structure

```
WiChat/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/nissshh/wichat/
│   │   │   ├── WiChatApplication.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── core/
│   │   │   │   ├── designsystem/theme/ (Color.kt, Type.kt, Theme.kt, Motion.kt)
│   │   │   │   ├── model/ (Models.kt)
│   │   │   │   ├── database/ (WiChatDatabase.kt)
│   │   │   │   ├── datastore/ (PreferencesRepository.kt)
│   │   │   │   ├── security/ (SecurityManager.kt)
│   │   │   │   ├── network/
│   │   │   │   │   ├── protocol/ (WiChatPacket.kt)
│   │   │   │   │   ├── nsd/ (WiChatNsdManager.kt)
│   │   │   │   │   └── transport/ (WiChatTransport.kt)
│   │   │   │   └── repository/ (WiChatRepository.kt, WiChatRepositoryImpl.kt)
│   │   │   └── feature/
│   │   │       ├── onboarding/ (PasscodeSetupScreen.kt, PermissionsOnboardingScreen.kt, ProfileCreationScreen.kt)
│   │   │       ├── devices/ (DevicesScreen.kt)
│   │   │       ├── requests/ (RequestsScreen.kt)
│   │   │       ├── chats/ (ChatsScreen.kt, OneToOneChatScreen.kt)
│   │   │       ├── stories/ (StoriesScreen.kt)
│   │   │       ├── profile/ (ProfileSettingsScreen.kt)
│   │   │       ├── diagnostics/ (NetworkDiagnosticsScreen.kt)
│   │   │       └── navigation/ (MainNavigationContainer.kt)
│   │   └── res/
│   │       ├── drawable/ (ic_launcher_background.xml, ic_launcher_foreground.xml)
│   │       ├── font/ (dm_sans_regular.ttf, dm_sans_medium.ttf, dm_sans_bold.ttf, OFL.txt)
│   │       ├── mipmap-*/ (Adaptive & legacy launcher icons)
│   │       └── values/ (strings.xml, themes.xml)
│   └── build.gradle.kts
├── gradle/
│   ├── wrapper/ (gradle-wrapper.properties, gradle-wrapper.jar)
│   └── libs.versions.toml (Version catalog)
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 3. Build & Run Instructions

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or command-line Android SDK
- JDK 17 (verified with OpenJDK 17)
- Android SDK Platform 35 (`compileSdk = 35`, `minSdk = 26`)

### Build Commands
To build the debug APK:
```bash
./gradlew assembleDebug
```
The resulting APK will be placed at:
`app/build/outputs/apk/debug/app-debug.apk`

To run unit tests:
```bash
./gradlew test
```

---

## 4. How to Test with Two Devices (Without Internet)

1. Connect two Android phones to the **same Wi-Fi router** or have one phone turn on its **Personal Wi-Fi Hotspot** and connect the second phone to it.
2. **Turn off the internet uplink** (unplug WAN cable or turn off mobile data).
3. Install and open WiChat on both phones:
   - Complete 6-digit passcode setup.
   - Accept local network permissions.
   - Set a Display Name (e.g., "Aarav" on Phone A, "Maya" on Phone B).
4. Go to the **Devices** tab:
   - Phone A will discover Phone B via mDNS (`_wichat._tcp`).
   - Tap **Message** on Phone A to send a connection request.
5. On Phone B, go to the **Requests** tab:
   - View Phone A's intro request and tap **Accept**.
6. Both phones will open a direct 1-to-1 chat session and exchange text messages in real-time over the local Wi-Fi without contacting any cloud servers.
