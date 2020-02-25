package com.lfuture.mygmail.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.EditText
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat

object Utils {

    const val REQUEST_ACCOUNT_PICKER = 1000
    const val REQUEST_AUTHORIZATION = 1001
    const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003

     private val isMarshmallow: Boolean
        get() = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1

    fun checkPermission(context: Context, permission: String): Boolean {
        return if (isMarshmallow) {
            val result = ContextCompat.checkSelfPermission(context, permission)
            result == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

}
