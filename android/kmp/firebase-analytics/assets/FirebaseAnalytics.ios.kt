// iOS Implementation - Place in iosApp/src/iosMain/kotlin/analytics/
// Path: composeApp/src/iosMain/kotlin/analytics/FirebaseAnalytics.ios.kt

package com.example.digitaldiary.analytics

import platform.FirebaseAnalytics.FIRAnalytics
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import kotlinx.cinterop.memScoped
import platform.objc.NSObject

actual class AnalyticsLogger {
    actual fun logEvent(name: String, params: Map<String, Any>) {
        val mutableParams = mutableMapOf<String, Any>()
        
        // Add global context
        mutableParams["platform"] = "ios"
        mutableParams["app_version"] = getAppVersion()
        
        // Add custom parameters
        mutableParams.putAll(params)
        
        // Convert to Firebase-compatible dictionary
        val dict = mutableParams.mapValues { (_, v) ->
            when (v) {
                is String -> v as NSString
                is Int -> NSNumber((v as Int).toDouble())
                is Long -> NSNumber((v as Long).toDouble())
                is Double -> NSNumber(v as Double)
                is Boolean -> NSNumber((v as Boolean))
                else -> v.toString() as NSString
            }
        }
        
        FIRAnalytics.logEventWithName(name, parameters = dict)
    }

    actual fun logEvent(name: String, params: Pair<String, Any>) {
        logEvent(name, mapOf(params))
    }

    actual fun setUserProperty(name: String, value: String) {
        FIRAnalytics.setUserPropertyString(value, forName = name)
    }

    actual fun setUserId(userId: String) {
        FIRAnalytics.setUserID(userId)
    }

    private fun getAppVersion(): String {
        // Bundle version can be fetched from main bundle
        return "1.0" // TODO: Get from Info.plist
    }
}

actual val currentPlatform: String = "ios"
