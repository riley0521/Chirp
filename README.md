# Chirp
Chirp is a chat application that uses Kotlin Multiplatform to support Android & iOS.

![Chirp screenshot](https://github.com/riley0521/Chirp/blob/master/previews/screenshot.png)

# Download
Go to the [Releases](https://github.com/riley0521/Chirp/releases) to download app. Note: iOS is not available since I don't have an apple developer account and cannot setup push notification.

# Features
- Authentication
    - Sign up
    - Sign in
    - Email verification
    - Forgot password
- Chat
    - Chat list
    - Chat detail
    - Create chat
    - Add/Remove members
    - Send images and voice message

## Security
For storing sensitive information like authentication token & user information, we used DataStore for storing key-value pairs + Keystore for Android & Keychain for iOS to ensure that no information will be compromised.

# Tech stack & Open-source libraries
- Minimum SDK Level 26 for Android.
- [Kotlin](https://kotlinlang.org/) based, utilizing [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/) for asynchronous operations.
- Jetpack Libraries:
    - Jetpack compose: Android’s modern toolkit for declarative UI development.
    - Lifecycle: Observes Android lifecycles and manages UI states upon lifecycle changes.
    - ViewModel: Manages UI-related data and is lifecycle-aware, ensuring data survival through configuration changes.
    - Navigation: Facilitates screen navigation, complemented by [Koin](https://insert-koin.io/) for dependency injection.
    - Room: Constructs a database with an SQLite abstraction layer for seamless database access.
    - DataStore: Jetpack DataStore is a data storage solution that lets you store key-value pairs or typed objects with [protocol buffers](https://developers.google.com/protocol-buffers).
- Architecture:
    - MVVM Architecture (View - ViewModel - Model): Facilitates separation of concerns and promotes maintainability.
    - Repository Pattern: Acts as a mediator between different data sources and the application's business logic.
- [KotlinX Serialization](https://github.com/Kotlin/kotlinx.serialization) - Used for serialization/deserialization of JSON and for navigation as well.
- [KotlinX DateTime](https://github.com/Kotlin/kotlinx-datetime) - Pure kotlin implementation of DateTime because we cannot use Java DateTime in KMP.
- [ksp](https://github.com/google/ksp): Kotlin Symbol Processing API for code generation and analysis.
- [Koin](https://insert-koin.io/) - Used for dependency injection.
- [Ktor](https://github.com/ktorio/ktor) - Used for networking and making API calls and WebSocket for real-time communication.
- [Coil](https://coil-kt.github.io/coil/) - Image loading library
- [Moko](https://github.com/icerockdev/moko-resources) (Modern Kotlin) - Used for handling permissions
- [BuildKonfig](https://github.com/yshrsmz/BuildKonfig) - Android have BuildConfig, but for iOS? We don't, and BuildKonfig solves that problem.
- [Kermit](https://github.com/touchlab/Kermit) - Logging library for KMP.
- [Diglol Crypto](https://github.com/diglol/crypto) - Used for encryption/decryption.

# Build and run
You can fork this repository and follow the steps below:
- For Android - No additional setup required. Just hit run in your [Android Studio](https://developer.android.com/studio).
- For iOS - You need macbook to run this, and you can also use Android Studio, just switch the run widget from androidApp to iosApp. If you want to use [XCode](https://developer.apple.com/xcode/), open the iosApp/iosApp.xcodeproj file, and run the app from your XCode.

Happy coding :D

# License
```xml
Copyright 2026 Riley Farro

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
