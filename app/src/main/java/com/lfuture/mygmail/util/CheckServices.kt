package com.lfuture.mygmail.util

import android.app.Activity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class CheckServices(activity: Activity) {
    private var activity: Activity? = null

    init {
        this.activity = activity
    }

    // Method for Checking Google Play Service is Available
    fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    // Method to Show Info, If Google Play Service is Not Available.
    fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    // Method for Google Play Services Error Info
    private fun showGooglePlayServicesAvailabilityErrorDialog(
        connectionStatusCode: Int

    ) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
            activity,
            connectionStatusCode,
            Permission.REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog.show()
    }
}