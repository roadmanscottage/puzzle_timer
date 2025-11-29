# Common Android Issues & Solutions

This document captures real-world Android development issues encountered during projects and their solutions. Agents should reference this to avoid repeating these mistakes.

---

## 🔧 Build & Dependency Issues

### Issue 1: Unresolved Reference to Extended Material Icons

**Symptom:**
```
Unresolved reference: Extension
Unresolved reference: Upload
Unresolved reference: Download
```

**Cause:**
Extended Material icons like `Icons.Default.Extension`, `Icons.Default.Upload`, `Icons.Default.Download` are NOT in the core Material Icons library.

**Solution:**
Add the extended icons library to `app/build.gradle.kts`:
```kotlin
dependencies {
    // Material Icons Extended - Required for Icons.Default.Extension, Upload, Download, etc.
    implementation("androidx.compose.material:material-icons-extended")
}
```

**When to Add:**
- Immediately when using ANY icon beyond the basic set (Star, Home, Menu, Search, etc.)
- Common extended icons: Extension, Upload, Download, FileUpload, FileDownload, Camera, etc.

**Prevention:**
Check [Material Icons list](https://fonts.google.com/icons) - if icon is not in "Core" category, add extended library.

---

### Issue 2: Unresolved Material 3 Color Roles

**Symptom:**
```
Unresolved reference: surfaceContainer
Unresolved reference: surfaceContainerLow
Unresolved reference: surfaceContainerHigh
```

**Cause:**
Compose BOM version may provide older Material3 library that doesn't include newer color roles from Material Design 3.1+.

**Solution:**
Explicitly specify Material3 version in `app/build.gradle.kts`:
```kotlin
dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.01.00")
    implementation(composeBom)

    // Material3 - Explicit version for newer color roles support
    implementation("androidx.compose.material3:material3:1.2.1")  // Override BOM version
}
```

**When to Add:**
- When using `surfaceContainer`, `surfaceContainerHigh`, `surfaceContainerLow`, `surfaceContainerLowest`, `surfaceContainerHighest`
- When using `onSurfaceVariant`, `outlineVariant`
- Basically, use Material3 1.2.0+ for full M3 spec

**Prevention:**
Always use explicit Material3 version ≥1.2.0 for modern projects.

---

### Issue 3: Missing Dependencies Not Detected Early

**Symptom:**
Build fails midway through project with missing dependency errors.

**Cause:**
Dependencies added to build.gradle but Gradle never synced before coding.

**Solution:**
After adding ANY dependency to build.gradle.kts:
```bash
./gradlew --refresh-dependencies
# OR in Android Studio: File → Sync Project with Gradle Files
```

**Prevention:**
- Add ALL known dependencies upfront during project setup
- Run Gradle sync immediately after adding dependencies
- Test import statements before proceeding with implementation

---

## 💻 Code Issues

### Issue 4: Conflicting Overloads - Duplicate Function Names

**Symptom:**
```
Conflicting overloads: public fun formatTime(millis: Long): String defined in com.example.app.ui.screens
```

**Cause:**
Multiple files in the same package define helper functions with identical signatures.

**Example:**
```kotlin
// TimerScreen.kt
fun formatTime(millis: Long): String { ... }

// PuzzleDetailsScreen.kt
fun formatTime(millis: Long): String { ... }  // CONFLICT!
```

**Solution A - Make Functions Private:**
```kotlin
// PuzzleDetailsScreen.kt
private fun formatTime(millis: Long): String { ... }  // No conflict
```

**Solution B - Rename:**
```kotlin
// PuzzleDetailsScreen.kt
private fun formatTimeInternal(millis: Long): String { ... }
```

**Solution C - Extract to Util File:**
```kotlin
// utils/TimeFormatter.kt
object TimeFormatter {
    fun formatTime(millis: Long): String { ... }
}

// Usage:
TimeFormatter.formatTime(elapsedMillis)
```

**Prevention:**
- Make helper functions `private` by default
- If shared across multiple files, extract to utils package
- Use descriptive names that indicate scope (formatTimerDisplay vs formatSessionTime)

---

### Issue 5: String.format Without Locale

**Symptom:**
```
Warning: Implicitly using the default locale is a common source of bugs
```

**Cause:**
Using `String.format()` without specifying a Locale:
```kotlin
String.format("%02d:%02d:%02d", hours, minutes, seconds)  // ❌ No Locale
```

**Solution:**
Always specify Locale:
```kotlin
String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)  // ✅
```

**Impact:**
Different locales may format numbers differently (e.g., decimal separators, digit grouping).

**Prevention:**
Add to lint checks, always use `Locale.getDefault()` or `Locale.US` for consistent formatting.

---

## 🎨 UI/UX Issues

### Issue 6: Non-Functional Navigation Drawer/Menu

**Symptom:**
Hamburger menu icon renders but clicking it does nothing.

**Cause:**
Icon is displayed but no drawer state or navigation logic implemented.

**Bad Code:**
```kotlin
TopAppBar(
    navigationIcon = {
        IconButton(onClick = { /* TODO */ }) {  // ❌ Empty click handler
            Icon(Icons.Default.Menu, "Menu")
        }
    }
)
```

**Solution:**
Implement full drawer with state:
```kotlin
val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
val scope = rememberCoroutineScope()

ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
        ModalDrawerSheet {
            // Drawer content
            NavigationDrawerItem(
                label = { Text("Option 1") },
                selected = false,
                onClick = {
                    scope.launch { drawerState.close() }
                    // Navigate
                }
            )
        }
    }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, "Menu")
                    }
                }
            )
        }
    ) { /* Content */ }
}
```

**Prevention:**
When adding ANY interactive element (button, menu, FAB), implement full functionality immediately. Don't leave `TODO` or empty click handlers.

---

### Issue 7: Icons/Buttons Too Close Together - Accidental Clicks

**Symptom:**
User reports accidentally clicking reset when trying to click pause.

**Cause:**
Touch targets too close together without adequate spacing.

**Bad Layout:**
```kotlin
Column {
    Text("01:23:45")
    IconButton(onClick = { reset() }) { ... }  // ❌ Right below timer
    Button(onClick = { pause() }) { ... }      // ❌ Right below reset
}
```

**Solution A - Add Spacing:**
```kotlin
Column {
    Text("01:23:45")
    Spacer(modifier = Modifier.height(16.dp))  // ✅ Add space
    IconButton(onClick = { reset() }) { ... }
    Spacer(modifier = Modifier.height(8.dp))   // ✅ Add space
    Button(onClick = { pause() }) { ... }
}
```

**Solution B - Move Dangerous Action:**
```kotlin
TopAppBar(
    title = { Text("Timer") },
    actions = {
        IconButton(onClick = { showResetDialog = true }) {  // ✅ In app bar
            Icon(Icons.Default.Replay, "Reset")
        }
    }
)
```

**Solution C - Add Confirmation:**
```kotlin
var showResetDialog by remember { mutableStateOf(false) }

IconButton(onClick = { showResetDialog = true }) { ... }

if (showResetDialog) {
    AlertDialog(
        title = { Text("Reset Timer?") },
        text = { Text("This will reset the timer to 00:00:00. Are you sure?") },
        onDismissRequest = { showResetDialog = false },
        confirmButton = {
            TextButton(onClick = {
                reset()
                showResetDialog = false
            }) { Text("Reset") }
        },
        dismissButton = {
            TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
        }
    )
}
```

**Prevention:**
- Minimum touch target: 48dp × 48dp
- Minimum spacing between interactive elements: 8dp
- Destructive actions (delete, reset) should require confirmation
- Place dangerous actions away from frequently-used buttons

---

## 🗂️ Architecture Issues

### Issue 8: ViewModel Not Properly Injected

**Symptom:**
```
java.lang.IllegalArgumentException: CreationExtras must have an application
```

**Cause:**
ViewModels requiring Application context but using default ViewModel factory.

**Solution - Manual DI Container:**
```kotlin
// di/AppContainer.kt
class AppContainer(private val context: Context) {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "app_db").build()
    }
    val repository: AppRepository by lazy {
        AppRepository(database.dao())
    }
}

// Application.kt
class MyApp : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}

// ViewModelFactory.kt
class ViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            TimerViewModel::class.java -> TimerViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }

    companion object {
        fun create(context: Context): ViewModelFactory {
            val app = context.applicationContext as MyApp
            return ViewModelFactory(app.container.repository)
        }
    }
}

// Screen.kt
@Composable
fun MyScreen() {
    val context = LocalContext.current
    val viewModel: TimerViewModel = viewModel(
        factory = ViewModelFactory.create(context)
    )
}
```

**Prevention:**
- Set up DI (Hilt or manual) during initial project setup
- Create ViewModelFactory before implementing screens
- Never use `ViewModel()` constructor directly in Composables

---

## 📱 AndroidManifest Issues

### Issue 9: Activity Not Registered

**Symptom:**
```
android.content.ActivityNotFoundException: Unable to find explicit activity class
```

**Cause:**
Created Activity but forgot to register in AndroidManifest.xml.

**Solution:**
```xml
<manifest>
    <application>
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- New activity must be registered -->
        <activity android:name=".DetailsActivity" />
    </application>
</manifest>
```

**Prevention:**
When creating ANY Activity class, immediately register in AndroidManifest.xml.

---

### Issue 10: Missing Permissions

**Symptom:**
```
java.lang.SecurityException: Permission denied (missing INTERNET permission?)
```

**Cause:**
Using network/storage/camera without declaring permission.

**Solution:**
```xml
<manifest>
    <!-- Add BEFORE <application> tag -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />

    <application>
        ...
    </application>
</manifest>
```

**For Dangerous Permissions (API 23+):**
Also request at runtime:
```kotlin
val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    if (granted) { /* Use camera */ }
}

Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
    Text("Take Photo")
}
```

**Prevention:**
- Review feature requirements at planning stage
- Add all needed permissions during setup
- Use checklist: Network? → INTERNET, Storage? → READ/WRITE_EXTERNAL_STORAGE, etc.

---

## 🧪 Testing Issues

### Issue 11: Tests Pass Locally But Fail in CI

**Cause:**
Using hardcoded paths, timestamps, or random data without proper mocking.

**Solution:**
- Use dependency injection for all external dependencies
- Mock time/date sources (use `Clock` interface, not `System.currentTimeMillis()`)
- Use test fixtures with consistent data

---

## 📦 Release Issues

### Issue 12: APK Size Too Large

**Cause:**
Including unused resources, duplicate dependencies, unoptimized images.

**Solution:**
```kotlin
// build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true  // Enable ProGuard/R8
            isShrinkResources = true  // Remove unused resources
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

---

## ✅ Prevention Checklist

Before marking any implementation as "complete", verify:

### Build Configuration
- [ ] All required dependencies added to build.gradle.kts
- [ ] Explicit Material3 version (≥1.2.0) if using modern color roles
- [ ] Extended icons library added if using non-core icons
- [ ] Gradle sync run and successful
- [ ] No dependency conflicts

### Code Quality
- [ ] No duplicate function names in same package (use `private` or rename)
- [ ] `String.format()` uses `Locale.getDefault()`
- [ ] No empty `onClick = { }` or `TODO` in interactive elements
- [ ] Dangerous actions have confirmation dialogs
- [ ] Touch targets ≥48dp, spacing ≥8dp between buttons

### AndroidManifest
- [ ] All Activities registered
- [ ] All permissions declared
- [ ] Application class registered if custom

### Architecture
- [ ] ViewModelFactory implemented and used
- [ ] Repository pattern for data access
- [ ] No direct database access from UI

### Testing
- [ ] `./gradlew assembleDebug` succeeds
- [ ] App installs and launches without crash
- [ ] All interactive elements functional (no placeholders)
- [ ] No logcat errors

---

**Use this document as a reference when implementing Android projects. Many of these issues are preventable with proper initial setup and attention to detail.** 🛡️
