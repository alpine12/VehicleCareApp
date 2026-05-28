# Keamanan Dasar
-keepattributes Signature, *Annotation*, EnclosingMethod

# Hilt & Dagger
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep class * extends androidx.lifecycle.ViewModel
-keep class * implements dagger.hilt.internal.GeneratedComponent
-keep class * implements dagger.hilt.internal.UnsafeCasts
-keep class * implements dagger.hilt.codegen.OriginatingElement

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao
-keep class * extends androidx.room.RoomDatabase$Callback
-dontwarn androidx.room.paging.**

# Coil (Image Loading)
-keep class io.coilkt.** { *; }
-dontwarn io.coilkt.**

# Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext$HandlerPost {
    volatile <fields>;
}

# Mencegah penghapusan resource yang digunakan secara dinamis
-keepclassmembers class **.R$* {
    public static <fields>;
}
