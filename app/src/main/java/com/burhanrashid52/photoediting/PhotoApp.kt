package com.burhanrashid52.photoediting

import android.app.Application
import android.content.Context

/**
 * Created by Burhanuddin Rashid on 1/23/2018.
 */
class PhotoApp : Application() {
    /**
     * Called when the application is starting, before any activity, service, or receiver objects (excluding content providers) have been created.
     * Initializes the static instance of PhotoApp.
     */
    override fun onCreate() {
        super.onCreate()
        photoApp = this
    }

    companion object {
        // Static instance of PhotoApp, initialized during application start.
        var photoApp: PhotoApp? = null
            private set
        // Simple name of the class, used for logging.
        private val TAG = PhotoApp::class.java.simpleName
    }
}