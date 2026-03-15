# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A system that automatically injects lifecycle logging into Android Activities via a Kotlin compiler plugin, and visualizes them as a real-time timeline in an Android Studio plugin.

## Module Structure

```
android-lifecycle-profiler/
├── annotations/            # @LogLifecycle annotation definition
├── compiler-plugin/        # Kotlin IR transformation core (log injection into lifecycle methods)
├── gradle-plugin/          # KotlinCompilerPluginSupportPlugin (composite build)
├── sample-android/         # Sample Android app for verification
└── lifecycle-studio-plugin/ # Android Studio plugin (timeline UI)
```

`gradle-plugin` is managed as a composite build (`includeBuild("gradle-plugin")`) and is an independent Gradle project from the other modules.

## Build Commands

### Initial Setup (Required)

Since `gradle-plugin` references artifacts from `compiler-plugin`, local publishing must be done first:

```bash
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
./gradlew :annotations:publishToMavenLocal
./gradlew :compiler-plugin:publishToMavenLocal
```

### Common Tasks

```bash
# Build sample-android
./gradlew :sample-android:assembleDebug

# Launch Android Studio plugin (IDE opens in a separate window)
./gradlew :lifecycle-studio-plugin:runIde

# Run tests
./gradlew :lifecycle-studio-plugin:test

# Verify plugin compatibility
./gradlew :lifecycle-studio-plugin:verifyPlugin
```

## Architecture

### Data Flow

1. **Compile time**: IR transformation inserts `android.util.Log.d()` calls at the start of lifecycle methods (`onCreate`, etc.) in classes annotated with `@LogLifecycle`
2. **Runtime**: Log format is `"ClassName|methodName|${System.currentTimeMillis()}"`, tag is `LC_VIZ`
3. **IDE**: `LogcatMonitor` subscribes to logs and accumulates them as `LifecycleSpan` in `LifecycleStore` (`StateFlow`)
4. **UI**: `TimelineChart` (Compose Canvas) renders spans in real time

### Methods Transformed by compiler-plugin

`onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`

### Key Classes in lifecycle-studio-plugin

- `LogcatMonitor`: Subscribes to logcat via `AndroidLogcatService`
- `LifecycleStore`: Project service annotated with `@Service(Service.Level.PROJECT)`, converts `LifecycleEvent` → `LifecycleSpan` via a state machine
- `TimelineChart`: Renders spans from the last 10 seconds on Canvas
- `LifecycleToolWindow`: Tool window factory

## Dependency Notes

- `compiler-plugin` depends on `compileOnly("com.google.android:android:4.1.1.4")` for resolving `android.util.Log` symbols
- `lifecycle-studio-plugin` requires `bundledPlugin("org.jetbrains.android")` (for `AndroidLogcatService`)
- Android Studio version is `androidStudio("2025.2.2.1")`
- Gradle Configuration Cache and Build Cache are enabled
