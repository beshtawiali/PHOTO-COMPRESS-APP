# Project specific ProGuard rules

# Keep line numbers and annotations for debugging
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Appodeal SDK & Consent Rules
-keep class com.appodeal.** { *; }
-dontwarn com.appodeal.**
-keep class com.explorestack.** { *; }
-dontwarn com.explorestack.**

# Mediated Networks & Adapters
-keep class io.bidmachine.** { *; }
-dontwarn io.bidmachine.**
-keep class com.applovin.** { *; }
-dontwarn com.applovin.**
-keep class org.bidon.** { *; }
-dontwarn org.bidon.**

# Room Database & Entities
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.tayf.photocompressor.data.model.** { *; }
-keep class com.tayf.photocompressor.data.local.** { *; }

