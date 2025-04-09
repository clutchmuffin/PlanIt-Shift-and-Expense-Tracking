# PlanIt - Shift and Expense Tracking

## Overview
**PlanIt** is an Android application that helps users manage their work schedule, track expenses, and maintain budgets. The app includes features such as shift tracking, notifications, calendar integration, and financial planning tools.

## Members
- **Daniel Laszlo** (job548)
- **Sayed Farhaan Rafi Bhat** (bcl568)
- **Sayed Farzaan Rafi Bhat** (kfn036)
- **Parker Tait** (evz405)
- **Tadiwanashe Chipunza** (xfo904)

## Product Features

### Shift Scheduling
- [x] Color Coded
- [x] Add and Delete Shifts
- [x] Get notified for Shifts
- [x] Shift Conflict Detection

### Expense Tracking
- [x] Gross Pay Tracking
- [x] Job Related Expense Tracking
- [x] Net Pay Tracking Including expenses

### Calendar Sharing
- [x] Sharing between users
- [x] Export Personal Calendar
- [x] Shift Visibility

### Budgeting
- [x] Monthly Budget setting
- [x] Extracurricular expense tracking
- [x] Get alerted when budget is exceeded

## Prerequisites
Before installing and running PlanIt, ensure you have the following:

- **Android Studio** (latest version recommended)
- **Java Development Kit**
- A physical **Android device** or **Emulator** for testing (Installed alongside Android Studio)
- **Internet connection** for initial setup and Firebase functionality
- **Git** Version Control

## Installation

### Step 1: Clone the Repository
```
git clone https://git.cs.usask.ca/evz405/cmpt370team14.git
```

### Step 2: Set Up Android Studio
- Download and install [Android Studio](https://developer.android.com/studio)
    - Use the standard installation instructions.
    - Installs the Android Emulator by default. 
- Open Android Studio
- Select "Open an Existing Project"
- Navigate to the cloned repository and select it

### Step 3: Install Dependencies
The project uses Gradle for dependency management. The necessary dependencies will be downloaded automatically when you sync the project in Android Studio.

**To manually sync the project**:
- Click on "File" > "Sync Project with Gradle Files"
- Wait for the sync to complete (progress is shown in the bottom status bar)

### Step 4: Running the application
- Select `Run 'app'` (Play Button) on the toolbar
- It will run on your default/chosen emulator.


## Dependencies
The application uses the following dependencies:

#### Main Dependencies
- **AndroidX Libraries**
    - AppCompat
    - ConstraintLayout
    - RecyclerView
    - Activity
    - CardView
    - Core KTX (v1.15.0)

- **UI Components**
    - Material Design Components
    - Calendar View (com.kizitonwose.calendar:view:2.6.2)
    - MPAndroidChart (v3.1.0)

- **Firebase**
    - Firebase BoM
    - Firebase Authentication
    - Firestore

#### Testing Dependencies

- **JUnit**
    - JUnit
    - AndroidX Test JUnit

- **Android Testing**
    - UI Automator
    - Test Rules
    - Test Runner

- **Espresso**
    - Espresso Core (v3.5.1)
    - Espresso Contrib
    - Espresso Intents
