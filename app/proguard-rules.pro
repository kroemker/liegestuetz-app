# Kotlin
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Firebase Firestore - keep data class fields for deserialization
-keepattributes Signature
-keepattributes *Annotation*
-keepnames class com.google.firebase.** { *; }
-keepnames class com.google.android.gms.** { *; }

# Keep domain model data classes (Firestore needs field names)
-keepclassmembers class com.liegestuetz.data.remote.dto.** {
    <fields>;
    <init>(...);
}

# Retrofit
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.liegestuetz.**$$serializer { *; }

# Hilt
-keepclasseswithmembers class * {
    @dagger.hilt.* *;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
