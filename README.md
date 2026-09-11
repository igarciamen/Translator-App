# AiTranslator

A native Android translation and language-assistance app built with Kotlin and Jetpack Compose. AiTranslator goes beyond plain text translation, combining text, voice, camera, dictionary, and phrasebook tools into a single app.

## Demo



https://github.com/user-attachments/assets/de0d8aed-0fb1-45e4-8bec-544cf6218588



## Features

### 🔤 Translate
- Text translation across 16 languages, with on-demand model downloads and a Wi-Fi-only option
- Automatic source language detection
- Voice input (online speech recognition) or keyboard typing
- Text-to-speech playback of the translation
- Translation history with favorites

### 🗣️ Conversation
- Split-screen, two-speaker layout, one language per side, with a quick language swap
- Each side speaks in their own language; the translation is shown **and read aloud automatically** on the listening side, a hands-free, face-to-face conversation flow with no extra taps

### 📷 Camera
- Extract text from a photo or gallery image (OCR) and translate it
- Live translation mode: translated text overlaid directly on the camera preview in real time, with text-block tracking across frames

### 📖 Dictionary
- Word **definitions** (not translations) in the word's own language
- **Online**: Spanish, English, and French, each backed by a dedicated Wiktionary wikitext parser, includes synonyms, usage examples, and pronunciation where available
- **Offline**: the same three languages, backed by bundled SQLite databases, fully functional without an internet connection
- Distinct error handling for "word not found", "no connection" (with retry), and "unsupported language"

### 💬 Phrases
- Common phrases grouped by category (Greetings, Restaurant, Travel, Shopping, Emergency), translatable into any supported language and readable aloud
- Add and delete your own custom phrases per category, persisted locally

### ⚙️ More (Settings)
- App interface language, applied immediately and fully translated into Spanish
- Visual theme: Light, Dark, or follow system, with a dedicated color palette for each

## Tech stack

| Layer | Technology |
|---|---|
| Language / UI | Kotlin, Jetpack Compose |
| Architecture | MVVM + Repository, dependency injection with Hilt |
| Persistence | Room (translation history, custom phrases), DataStore Preferences (settings) |
| Translation | ML Kit Translate (Google), with per-language model downloads |
| Speech recognition | Android's native `SpeechRecognizer` (online) |
| Text-to-speech | Android's native `TextToSpeech` |
| Camera & OCR | CameraX, ML Kit Text Recognition, ML Kit Language ID |
| Online dictionary | Official MediaWiki API (Wiktionary), custom wikitext parsing per language |
| Offline dictionary | Bundled SQLite databases (app assets) |
| Networking | OkHttp |
| Testing | JUnit 4, MockK, coroutines-test |

## Getting started

### Requirements
- Android Studio (latest stable)
- JDK 17
- An Android device or emulator running API 26 (Android 8.0) or higher

### Setup
1. Clone the repository.
2. Open the project in Android Studio and let Gradle sync.
3. Run the `app` configuration on a device or emulator.

No API keys or additional configuration are required to build and run the app, translation, speech, and OCR all run through Google's on-device ML Kit and Android's native APIs.

### Running tests
```
./gradlew testDebugUnitTest
```
The project includes unit tests across repositories, ViewModels, and parsing logic for every feature area.

## Project structure

The codebase follows a standard MVVM + Repository layering:
- `domain/`  interfaces and plain data models, independent of Android framework classes
- `data/`  repository implementations, Room entities/DAOs, network and file-backed data sources
- `ui/`  Compose screens and ViewModels, one package per feature (`translate`, `conversation`, `ocr`, `dictionary`, `phrases`, `settings`, ...)
- `di/`  Hilt modules wiring interfaces to implementations

## Limitations

- Speech recognition is online-only. Two offline speech-recognition engines were evaluated in depth during development but were ultimately dropped after failing to meet acceptable accuracy and latency standards on target hardware.
- The dictionary (online and offline) currently covers Spanish, English, and French. The architecture supports adding more languages, but each one requires dedicated research into its data source's format before it can be integrated.
- The app has no user account or login system, this is an explicit, deliberate scope decision, not a missing feature.
- Interface localization is fully implemented and verified for Spanish; extending it to additional languages is a content task (translating string resources), not an architectural one.
