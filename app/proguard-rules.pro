# Project specific ProGuard rules

# Keep line numbers and annotations for debugging
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Appodeal SDK & Consent Rules
-keep class com.appodeal.** { *; }
-dontwarn com.appodeal.**
-keep class com.explorestack.** { *; }
-dontwarn com.explorestack.**

# Mediated Networks & Adapters Actually Included
-keep class io.bidmachine.** { *; }
-dontwarn io.bidmachine.**
-keep class com.applovin.** { *; }
-dontwarn com.applovin.**
-keep class org.bidon.** { *; }
-dontwarn org.bidon.**
-keep class com.amazon.device.ads.** { *; }
-dontwarn com.amazon.device.ads.**
-keep class sg.bigo.ads.** { *; }
-dontwarn sg.bigo.ads.**
-keep class com.bytedance.sdk.openadsdk.** { *; }
-dontwarn com.bytedance.sdk.openadsdk.**
-keep class com.chartboost.sdk.** { *; }
-dontwarn com.chartboost.sdk.**
-keep class com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**
-keep class com.fyber.** { *; }
-dontwarn com.fyber.**
-keep class com.digitalturbine.** { *; }
-dontwarn com.digitalturbine.**
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
-keep class com.ironsource.** { *; }
-dontwarn com.ironsource.**
-keep class com.mbridge.msdk.** { *; }
-dontwarn com.mbridge.msdk.**
-keep class com.mobilefuse.sdk.** { *; }
-dontwarn com.mobilefuse.sdk.**
-keep class com.moloco.sdk.** { *; }
-dontwarn com.moloco.sdk.**
-keep class com.my.target.** { *; }
-dontwarn com.my.target.**
-keep class com.ogury.** { *; }
-dontwarn com.ogury.**
-keep class com.pubmatic.sdk.** { *; }
-dontwarn com.pubmatic.sdk.**
-keep class com.smaato.sdk.** { *; }
-dontwarn com.smaato.sdk.**
-keep class com.startapp.** { *; }
-dontwarn com.startapp.**
-keep class com.taurusx.ads.** { *; }
-dontwarn com.taurusx.ads.**
-keep class com.unity3d.services.** { *; }
-keep class com.unity3d.ads.** { *; }
-dontwarn com.unity3d.services.**
-dontwarn com.unity3d.ads.**
-keep class net.pubnative.** { *; }
-dontwarn net.pubnative.**
-keep class com.verve.** { *; }
-dontwarn com.verve.**
-keep class com.vungle.** { *; }
-dontwarn com.vungle.**
-keep class com.yandex.mobile.ads.** { *; }
-dontwarn com.yandex.mobile.ads.**
-keep class io.sentry.** { *; }
-dontwarn io.sentry.**

# Room Database & Entities
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.tayf.photocompressor.data.model.** { *; }
-keep class com.tayf.photocompressor.data.local.** { *; }

