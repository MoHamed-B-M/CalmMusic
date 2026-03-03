# ProGuard rules for CalmPlayer

# Keep Media3 classes for reflection
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep Compose runtime
-keep class androidx.compose.** { *; }

# Keep Coil image loader
-keep class coil.** { *; }

# Keep our data classes
-keep class com.music.calmplayer.data.Song { *; }
-keep class com.music.calmplayer.data.Album { *; }
-keep class com.music.calmplayer.data.Artist { *; }
-keep class com.music.calmplayer.data.ThemeConfig { *; }

# Keep the service
-keep class com.music.calmplayer.service.PlaybackService { *; }

# Kotlin coroutines
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# General Android rules
-keepattributes *Annotation*
-keepattributes Signature
