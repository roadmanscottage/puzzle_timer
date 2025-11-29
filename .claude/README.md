# Android Multi-Agent Orchestration System

This folder contains a specialized AI orchestration system designed specifically for Android development with Kotlin and Jetpack Compose.

## 📁 Structure

```
android-app-agents-wizard/
└── .claude/
    ├── README.md              # This file - system overview & usage
    ├── COMMON-ISSUES.md       # Real-world Android pitfalls & solutions ⚠️
    ├── CLAUDE.md              # Android Orchestrator instructions
    └── agents/
        ├── android-coder.md    # Kotlin/Compose implementation specialist
        ├── android-tester.md   # Build & emulator testing specialist
        ├── ui-reviewer.md      # Material Design compliance reviewer
        └── stuck.md            # Human escalation for Android issues
```

## ⚠️ IMPORTANT: Common Issues Document

**`COMMON-ISSUES.md`** contains real-world Android problems encountered during actual projects and their proven solutions. ALL agents reference this document to avoid repeating mistakes.

**Key Issues Covered:**

- Missing extended Material icons (Extension, Upload, Download)
- Material3 version requirements for color roles
- Conflicting function names in same package
- String.format without Locale warnings
- Non-functional navigation drawers/menus
- Interactive elements too close together
- Empty click handlers (TODO placeholders)
- Missing confirmation dialogs for dangerous actions
- AndroidManifest permission/Activity registration

**Agents MUST read this before implementing features to prevent these documented problems.**

## 🎯 How It Works

### The Orchestrator (You - Claude Code)
- Maintains 200k context window with full project state
- Creates comprehensive Android todo lists with TodoWrite
- Delegates ONE task at a time to specialized agents
- Each agent works in its own fresh context window
- Tracks progress and coordinates the entire build

### The Agent Workflow

```
USER: "Build an Android note-taking app"
    ↓
ORCHESTRATOR: Creates detailed todo list
    ↓
ORCHESTRATOR → android-coder: "Set up Room database"
    ↓
android-coder: Implements Room entities, DAOs, database
    ↓
ORCHESTRATOR: Marks todo complete, moves to next
    ↓
[Repeat until all todos complete]
```

## 🤖 The Agents

### android-coder
**Role**: Android implementation specialist
- Writes Kotlin & Jetpack Compose code
- Configures Gradle dependencies
- Updates AndroidManifest
- Follows Material Design 3
- Uses MVVM architecture
- Implements Room, Navigation, etc.

**Invokes stuck agent when**:
- Gradle sync fails
- Compilation errors
- Dependency conflicts
- AndroidManifest issues
- Any uncertainty

### android-tester
**Role**: Build & emulator testing specialist (OPTIONAL - NOT USED BY DEFAULT)
- Runs Gradle builds (`./gradlew assembleDebug`)
- Launches emulators
- Installs and tests APKs
- Captures logcat output
- Takes screenshots
- Verifies functionality

**When to invoke**: Only if explicitly requested by user

**Invokes stuck agent when**:
- Build fails
- Tests fail
- App crashes
- Unexpected behavior
- Performance issues

### ui-reviewer
**Role**: Material Design compliance reviewer
- Verifies UI matches design specs
- Checks Material Design 3 compliance
- Tests light and dark themes
- Validates accessibility (touch targets, contrast)
- Tests responsive design
- Captures screenshot evidence

**Invokes stuck agent when**:
- Colors don't match spec
- Layout issues
- Material Design violations
- Accessibility problems
- Design spec conflicts

### stuck
**Role**: Human escalation point (MANDATORY)
- **ONLY** agent with AskUserQuestion access
- ALL other agents MUST invoke this when problems occur
- Presents clear options to human
- Returns human's decision to calling agent
- **NO FALLBACKS ALLOWED**

## 🚨 Critical Rules

### For the Orchestrator (You):
1. ✅ Create Android-specific todo lists immediately
2. ✅ Delegate ONE todo at a time to android-coder
3. ✅ Review UI features with ui-reviewer
4. ✅ Never implement code yourself
5. ✅ Maintain the big picture in your 200k context

### For All Agents:
1. ❌ NO fallbacks - invoke stuck agent immediately
2. ❌ NO assumptions - ask human via stuck agent
3. ❌ NO workarounds - escalate problems
4. ❌ NO skipping errors - all errors must be resolved
5. ✅ One task, complete it, report back
6. ✅ Invoke stuck agent on ANY problem

## 📋 Example Android Project Flow

### User Request:
"Build a weather app with current conditions and 5-day forecast"

### Orchestrator Creates Todos:
1. [ ] Set up Android project with Gradle (API 26+)
2. [ ] Add Retrofit & Moshi dependencies for API
3. [ ] Create Weather data models
4. [ ] Implement WeatherRepository with API calls
5. [ ] Create CurrentWeatherScreen with Compose
6. [ ] Create ForecastScreen with Compose
7. [ ] Implement Navigation between screens
8. [ ] Apply Material3 theme with weather icons
9. [ ] Add location permission handling
10. [ ] Test complete app flow

### Execution:
```
ORCHESTRATOR → android-coder(todo #1)
    → android-coder creates project structure
    → Reports back with files created

ORCHESTRATOR → Marks todo #1 complete

ORCHESTRATOR → android-coder(todo #2)
    → android-coder adds Retrofit dependencies
    → Gradle sync issue → INVOKES stuck agent
    → stuck agent asks human about version conflict
    → Human chooses solution
    → android-coder completes with human's guidance

ORCHESTRATOR → Marks todo #2 complete

[Continue for all todos...]

ORCHESTRATOR → ui-reviewer(review weather screens)
    → ui-reviewer checks Material Design compliance
    → Takes screenshots of light/dark themes
    → Verifies accessibility
    → Reports approval

ORCHESTRATOR → Reports to user: Project complete!
```

## ✅ Success Indicators

- Todo list created immediately upon request
- One todo completed before moving to next
- UI features reviewed by ui-reviewer
- All problems escalated to stuck agent (human)
- No blind fallbacks or workarounds
- UI matches Material Design 3 guidelines

## 🎯 Key Android Principles

1. **Gradle First**: Always configure dependencies before implementing features
2. **Manifest Matters**: Don't forget permissions and Activity registration
3. **Material Design 3**: Use proper components, colors, typography
4. **Test on Emulator**: Always verify on actual device/emulator
5. **Dark Theme**: Test both light and dark themes
6. **Accessibility**: Touch targets ≥48dp, contrast ≥4.5:1
7. **State Management**: Use StateFlow/Flow for reactive UI
8. **Human in Loop**: stuck agent ensures human oversight on all issues

## 🚀 Getting Started

When you (the orchestrator) receive an Android project request:

1. **IMMEDIATELY** use TodoWrite to create Android todo list
2. **IMMEDIATELY** invoke android-coder with first todo
3. Wait for android-coder to complete
4. Mark todo complete and move to next
5. Repeat until all todos done
6. Report final results to user

## 💡 Tips for Success

- **Be specific in todos**: "Add Room database with Note entity" not "Add database"
- **One feature at a time**: Don't combine "Create UI and add navigation" into one todo
- **Review UI**: User-facing screens should be reviewed by ui-reviewer
- **Trust the agents**: They have specialized expertise in their domain
- **Use stuck agent**: When any agent hits a problem, stuck agent gets human guidance

---

**This orchestration system ensures high-quality Android apps through specialized agents, comprehensive testing, and human oversight on all issues.** 🤖📱✨
