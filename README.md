# 🛍️ Product Detail App

Welcome to the **Product Detail App**, a modern Android application built using **Jetpack Compose**. This app features a visually appealing **splash screen** with animated falling blue lines, followed by a **detailed product screen** displaying product information, pricing, features, and additional images. It uses **Retrofit** to fetch product data from a dummy API and offers a smooth, responsive experience.

---

## ✨ Features

- 🔵 **Dynamic Splash Screen**: Captivating splash screen with falling blue lines in various shades, fading out after 2 seconds.
- 🛍️ **Product Detail Screen**: Displays product name, original and discounted prices, runtime, features, and description.
- 🌐 **API Integration**: Fetches real-time product data using Retrofit from [`https://dummyjson.com/`](https://dummyjson.com/).
- 🎞️ **Smooth Transitions**: Fade-out animation for the splash screen, with a seamless white status bar and black icons.
- 📱 **Responsive Design**: Scrollable layout to ensure all content and buttons are accessible on any screen size.
- ⏱️ **Countdown Timer**: Red banner showing a live countdown for limited-time offers.

---

## 📷 Screenshots

### Logo

<img src="./PDP.png" alt="Profile App Logo" width="100"/>

> The Profile App logo, showcasing a minimalist design with a focus on user profiles.

### Splash Screen and Profile Screen

<img src="./pdp2.png" alt="Splash Screen" width="300"/>  
<img src="./pdp4.png" alt="Splash Screen 2" width="300"/>  
<img src="./pdp5.png" alt="Profile Screen" width="300"/>
<img src="./pdp1.png" alt="Splash Screen 2" width="300"/>  
<img src="./pdp3.png" alt="Profile Screen" width="300"/>

> The splash screen with animated falling lines (left) transitions to the profile screen (right), displaying user data and rewards.

---

## 🎥 Demo Video

> 📹 A quick demo of the Profile App, highlighting the splash screen animation and profile screen navigation.

[▶️ Watch Demo Video on Google Drive](https://drive.google.com/file/d/1h5DuBT1wLWgZDartQjNoEf8bkhenx7G0/view?usp=sharing)

---

## 📦 Download

Download the latest APK here:

[Download APK](./apk.apk)

---

## 🧑‍💻 Usage

- Displays after splash screen with a 1-second fade-in.
- Shows user data including credit score, cashback, bank balance, and rewards.
- Scroll to view "YOUR REWARDS & BENEFITS" and "TRANSACTIONS & SUPPORT".

---

## 🗂 Project Structure

| File                  | Description                                                                   |
|-----------------------|-------------------------------------------------------------------------------|
| `MainActivity.kt`     | Entry point of the app, handles navigation and splash/profile transitions.   |
| `SplashScreen.kt`     | Composable for the splash screen with animated lines and glowing text.       |
| `ProfileScreen.kt`    | Composable for displaying user data in a scrollable layout.                  |
| `ProfileViewModel.kt` | ViewModel to manage data fetching from Firebase.                             |
| `UserData.kt`         | Data class representing user information fetched from Firebase.              |

---
