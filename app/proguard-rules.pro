# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Waister\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# Keep Retrofit/Gson models (reflection-based serialization)
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.renobile.carrinho.network.** { *; }

# Enum used via ProductSortOrder.valueOf(...) in ViewModels
-keep enum com.renobile.carrinho.util.ProductSortOrder { *; }

# Keep line numbers for readable Crashlytics stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
