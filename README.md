# BakiKhata - Smart Baki Manager (বাকিখাতা)

A complete, production-ready Android application for small businesses to manage customer credit (বাকির হিসাব).

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-blue.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📱 Features

### 📊 Dashboard
- Real-time statistics (Total Customers, Total Due, Today's Credit)
- Summary cards with gradient backgrounds
- Recent activity tracking
- Color-coded status indicators (🟢 Paid, 🔴 Due)

### 👥 Customer Management
- Add/Edit/Delete customers with soft-delete protection
- Customer search functionality
- Phone number and address tracking
- Full transaction history per customer
- Automatic balance calculation

### 💳 Credit Entry System
- Add multiple credit entries per customer
- Auto-calculation of total (quantity × price)
- Date tracking with date picker
- Optional notes for each entry
- Product name, quantity, and price tracking

### 💰 Payment Tracking
- Record partial or full payments
- Real-time balance calculation
- Payment history with timestamps
- Show remaining balance
- Payment notes support

### 📈 Reports & Export
- Monthly reports generation
- Export to PDF (using iTextPDF)
- Export to Excel (using Apache POI)
- Customer-specific reports
- Summary statistics

### 🔒 Security
- PIN lock with fingerprint authentication
- AndroidX Biometric API integration
- Secure data storage with encryption
- Lock screen on app launch

### 🎨 Design
- Material Design 3
- Dark mode support
- Bengali language support (বাংলা)
- Smooth animations and transitions
- Soft blue/green color palette
  - Primary: `#1976D2` (Blue)
  - Secondary: `#43A047` (Green)
  - Background: `#F5F7FA`

### 📱 Additional Features
- SMS reminder integration
- Backup & Restore functionality
- Due date reminder notifications (WorkManager)
- Soft-delete for data safety
- Bottom navigation for easy access
- Search functionality
- Empty states with helpful messages

## 🏗️ Technical Stack

- **Language:** Kotlin 1.9.20
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** Room 2.6.1 (SQLite) with soft-delete
- **DI:** Hilt 2.48 (Dagger)
- **UI:** Material Design 3, XML layouts with ViewBinding
- **Navigation:** Jetpack Navigation Component 2.7.6
- **Async:** Kotlin Coroutines + Flow
- **Security:** AndroidX Biometric API 1.2.0-alpha05
- **Export:** Apache POI 5.2.3 (Excel), iTextPDF 7.2.5 (PDF)
- **Build System:** Gradle 8.2 with Kotlin DSL

## 📁 Project Structure

```
BakiKhata/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/hasibzero/bakikhata/
│       │   ├── BakiKhataApp.kt (Application class with Hilt)
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   │   ├── db/
│       │   │   │   ├── AppDatabase.kt
│       │   │   │   ├── dao/ (3 DAOs)
│       │   │   │   └── entity/ (3 entities)
│       │   │   ├── repository/ (3 repositories)
│       │   │   └── di/ (Hilt module)
│       │   ├── ui/
│       │   │   ├── dashboard/ (Fragment, ViewModel, Adapter)
│       │   │   ├── customer/ (4 Fragments, 4 ViewModels, Adapter)
│       │   │   ├── credit/ (Fragment, ViewModel, Adapter)
│       │   │   ├── payment/ (Fragment, ViewModel, Adapter)
│       │   │   ├── report/ (Fragment, ViewModel)
│       │   │   ├── settings/ (Fragment, ViewModel)
│       │   │   └── security/ (Activity, ViewModel)
│       │   ├── util/ (5 utility classes)
│       │   └── worker/ (ReminderWorker)
│       └── res/
│           ├── layout/ (14 layouts)
│           ├── drawable/ (6 vector drawables)
│           ├── menu/ (1 bottom navigation menu)
│           ├── navigation/ (1 nav graph)
│           ├── values/ (colors, strings, themes, dimens, styles)
│           ├── values-bn/ (Bengali strings)
│           ├── values-night/ (Dark theme)
│           └── xml/ (backup rules)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 📊 Project Statistics

- **41** Kotlin source files
- **34** XML resource files
- **76** total files in src/main
- **3** database entities with soft-delete
- **3** DAOs with reactive queries
- **3** repositories
- **8** Fragments
- **8** ViewModels
- **4** RecyclerView Adapters
- **14** XML layouts
- **2** languages (English, Bengali)
- **2** themes (Light, Dark)

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Gradle 8.2+

### Installation

1. Clone the repository:
```bash
git clone https://github.com/hasibzero/BakiKhata.git
```

2. Open the project in Android Studio

3. Sync Gradle files (File → Sync Project with Gradle Files)

4. Build and run on an Android device or emulator

### Building

```bash
./gradlew assembleDebug
```

### Running Tests

```bash
./gradlew test
```

## 🎯 Key Features Implementation

### Soft Delete
All entities include `isDeleted` and `deletedAt` fields. All queries filter by `isDeleted = 0` by default, ensuring data is never permanently lost.

### MVVM Architecture
- **Model:** Room entities and database
- **View:** Fragments and Activities with ViewBinding
- **ViewModel:** Manages UI state with StateFlow/SharedFlow

### Dependency Injection
Using Hilt for clean dependency management across all components with `@HiltViewModel` and `@AndroidEntryPoint`.

### Reactive UI
All data flows use Kotlin Flow for reactive updates. Changes in the database automatically update the UI.

### Material Design 3
Complete implementation of Material Design 3 guidelines with proper theming, colors, and components.

## 📱 Screens

1. **Dashboard** - Overview with statistics and recent activity
2. **Customer List** - Searchable list of all customers
3. **Customer Detail** - Detailed view with transaction history
4. **Add/Edit Customer** - Customer information form
5. **Add Credit** - Credit entry form with auto-calculation
6. **Add Payment** - Payment recording form
7. **Reports** - Monthly reports with export options
8. **Settings** - App settings and backup/restore
9. **Lock Screen** - PIN/Fingerprint authentication

## 🔐 Security Features

- PIN-based authentication
- Biometric (fingerprint) authentication
- Secure data storage
- Soft-delete protection
- Backup encryption

## 🌐 Internationalization

- **English** (default)
- **Bengali** (বাংলা) - Complete translation

## 📄 License

Copyright (c) 2024 HasibZero

## 👨‍💻 Developer

**HasibZero**

## 🙏 Acknowledgments

- Material Design 3 guidelines
- Android Jetpack libraries
- Apache POI for Excel generation
- iTextPDF for PDF generation

---

**Note:** This is a complete, production-ready application with all features implemented. The app can be opened and built in Android Studio without any additional setup or configuration.
