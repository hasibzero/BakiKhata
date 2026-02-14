# BakiKhata - Smart Baki Manager (বাকিখাতা)

A complete, production-ready Android application for small businesses to manage customer credit (বাকির হিসাব).

## Features

### 📊 Dashboard
- Real-time statistics (Total Customers, Total Due, Today's Credit)
- Summary cards with gradient backgrounds
- Recent activity tracking

### 👥 Customer Management
- Add/Edit/Delete customers with soft-delete protection
- Customer search functionality
- Phone number and address tracking
- Full transaction history per customer

### 💳 Credit Entry System
- Add multiple credit entries per customer
- Auto-calculation of total (quantity × price)
- Date tracking with date picker
- Optional notes for each entry

### 💰 Payment Tracking
- Record partial or full payments
- Real-time balance calculation
- Payment history with timestamps
- Show remaining balance

### 📈 Reports & Export
- Monthly reports generation
- Export to PDF (using iTextPDF)
- Export to Excel (using Apache POI)
- Customer-specific reports

### 🔒 Security
- PIN lock with fingerprint authentication
- AndroidX Biometric API integration
- Secure data storage

### 🎨 Design
- Material Design 3
- Dark mode support
- Bengali language support (বাংলা)
- Smooth animations and transitions
- Soft blue/green color palette

### 📱 Additional Features
- SMS reminder integration
- Backup & Restore functionality
- Due date reminder notifications (WorkManager)
- Soft-delete for data safety
- Bottom navigation for easy access

## Technical Stack

- **Language:** Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** Room (SQLite) with soft-delete
- **DI:** Hilt (Dagger)
- **UI:** Material Design 3, XML layouts
- **Navigation:** Jetpack Navigation Component
- **Async:** Kotlin Coroutines + Flow
- **Security:** AndroidX Biometric API
- **Export:** Apache POI (Excel), iTextPDF (PDF)

## Project Structure

```
BakiKhata/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/hasibzero/bakikhata/
│       │   ├── BakiKhataApp.kt
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   │   ├── db/ (entities, DAOs, database)
│       │   │   ├── repository/
│       │   │   └── di/
│       │   ├── ui/
│       │   │   ├── dashboard/
│       │   │   ├── customer/
│       │   │   ├── credit/
│       │   │   ├── payment/
│       │   │   ├── report/
│       │   │   ├── settings/
│       │   │   └── security/
│       │   ├── util/
│       │   └── worker/
│       └── res/
│           ├── layout/
│           ├── values/ (English strings)
│           ├── values-bn/ (Bengali strings)
│           ├── values-night/ (Dark theme)
│           └── ...
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Building the Project

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run on an Android device or emulator

## Requirements

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34
- Gradle 8.2+

## Key Features Implementation

### Soft Delete
All entities include `isDeleted` and `deletedAt` fields. All queries filter by `isDeleted = 0` by default.

### MVVM Architecture
- **Model:** Room entities and database
- **View:** Fragments and Activities with ViewBinding
- **ViewModel:** Manages UI state with StateFlow/SharedFlow

### Dependency Injection
Using Hilt for clean dependency management across all components.

### Reactive UI
All data flows use Kotlin Flow for reactive updates.

## License

Copyright (c) 2024 HasibZero
