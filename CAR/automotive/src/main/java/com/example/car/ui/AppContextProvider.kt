package com.example.car.ui



import android.content.Context

/**
 * Provides application context globally without memory leaks.
 */
object AppContextProvider {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun get(): Context {
        check(::appContext.isInitialized) { "AppContextProvider is not initialized!" }
        return appContext
    }
}
