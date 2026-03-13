# How to Get Your google-services.json File

## Quick Steps:

### 1. Go to Firebase Console
Visit: https://console.firebase.google.com/

### 2. Create or Select Project
- Click "Add project" to create a new one, OR
- Select an existing project

### 3. Add Android App
1. Click the **⚙️ Settings** icon (Project Settings)
2. Scroll down to "Your apps" section
3. Click the **Android app icon** 
4. Enter package name: `com.example.bariaxorjak`
5. App nickname (optional): "Bari axorjak"
6. Click "Register app"

### 4. Download google-services.json
1. On the next screen, click **"Download google-services.json"**
2. Save the file

### 5. Place File in Project
Move the downloaded file to:
```
C:\Users\admin\Desktop\Bariaxorjak\app\google-services.json
```

## Alternative Method - If You Already Have a Firebase Project:

1. Go to Firebase Console: https://console.firebase.google.com/
2. Select your project
3. Click ⚙️ Settings → Project settings
4. Scroll to "Your apps" section
5. If you see your Android app, click on it
6. If not, add a new Android app with package name: `com.example.bariaxorjak`
7. Download the `google-services.json` file

## After Placing the File:

1. In Android Studio, click "Sync Now" (top right)
2. Or run: `.\gradlew.bat build`
3. Build should succeed!

## IMPORTANT Notes:

⚠️ **DO NOT share this file publicly** - It contains sensitive Firebase configuration

⚠️ **Each Firebase project has its own unique file** - You need YOUR project's file

⚠️ **File location MUST be exact**: `app/google-services.json` (not in src/, not in debug/)
