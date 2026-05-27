// Android Implementation - Place in androidApp/src/main/kotlin/analytics/
// Path: androidApp/src/main/kotlin/analytics/FirebaseAnalytics.android.kt

package com.example.digitaldiary.analytics

import android.os.Bundle
import com.google.firebase.analytics.Firebase
import com.google.firebase.analytics.analytics
import android.os.Build

actual class AnalyticsLogger {
    private val firebase = Firebase.analytics

    actual fun logEvent(name: String, params: Map<String, Any>) {
        val bundle = Bundle().apply {
            // Add global context
            putString(AnalyticsEvents.Params.PLATFORM, "android")
            putString(AnalyticsEvents.Params.APP_VERSION, getAppVersion())

            // Add custom parameters
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        firebase.logEvent(name, bundle)
    }

    actual fun logEvent(name: String, params: Pair<String, Any>) {
        logEvent(name, mapOf(params))
    }

    actual fun setUserProperty(name: String, value: String) {
        firebase.setUserProperty(name, value)
    }

    actual fun setUserId(userId: String) {
        firebase.setUserId(userId)
    }

    private fun getAppVersion(): String {
        return try {
            val pInfo = android.app.ActivityThread.currentApplication().packageManager
                .getPackageInfo(android.app.ActivityThread.currentApplication().packageName, 0)
            pInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
}

actual val currentPlatform: String = "android"
