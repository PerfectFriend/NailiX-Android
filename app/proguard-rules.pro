# Project specific ProGuard & R8 rules for NailiX (Release Candidate 1)

# Room Database rules
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Moshi, Room and App Data Models
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep class com.example.data.** { *; }
-keep class com.example.ui.viewmodel.** { *; }
-keep class com.example.notification.** { *; }

# Firebase Cloud Messaging
-keep class com.google.firebase.messaging.** { *; }
-dontwarn com.google.firebase.messaging.**

# Coil Image Loader
-keep class coil.** { *; }
-dontwarn coil.**

# Cryptographic routines (SHA-256 / SimpleX / UUID)
-keepclassmembers class * {
    java.security.MessageDigest *;
}

# Android Compose runtime
-keep class androidx.compose.runtime.** { *; }
