# Build Instructions for Puzzle Timer

## Prerequisites
- Java Development Kit (JDK) 17 or higher
- Android SDK (can be installed via Android Studio)
- Android Studio (recommended) or command-line tools

## Building the Project

### Using Android Studio
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to this project directory
4. Wait for Gradle sync to complete
5. Click "Build" > "Make Project" or press Ctrl+F9

### Using Command Line

#### On Windows:
```bash
gradlew.bat assembleDebug
```

#### On Linux/Mac:
```bash
./gradlew assembleDebug
```

## Expected Build Output
- APK location: `app/build/outputs/apk/debug/app-debug.apk`
- The build should complete without errors if all dependencies are properly downloaded

## Troubleshooting

### Gradle Sync Issues
If Gradle sync fails:
1. Check internet connection (required for downloading dependencies)
2. Ensure Android SDK is installed
3. Run: `gradlew --refresh-dependencies`

### Java Version Issues
Ensure you have JDK 17 or higher installed:
```bash
java -version
```

### SDK Missing
If Android SDK is not found:
1. Install Android Studio
2. Set ANDROID_HOME environment variable to your SDK location
3. Example: `ANDROID_HOME=C:\Users\<username>\AppData\Local\Android\Sdk`

## Next Steps After Successful Build
1. Run the app on an emulator or physical device
2. Implement database models in `data/model/`
3. Create DAOs in `data/dao/`
4. Set up the Room database in `data/database/`
5. Implement repositories in `repository/`
6. Build UI screens in `ui/screens/`
