package com.district37.toastmasters.notifications

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

internal const val NOTIFICATION_PERMISSION_UPDATED_ACTION =
    "com.district37.toastmasters.notifications.PERMISSION_UPDATED"

private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 2001

class NotificationPermissionRequestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            publishPermissionUpdateAndFinish()
            return
        }

        val isGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            publishPermissionUpdateAndFinish()
            return
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            publishPermissionUpdateAndFinish()
            return
        }
        finish()
    }

    private fun publishPermissionUpdateAndFinish() {
        sendBroadcast(
            Intent(NOTIFICATION_PERMISSION_UPDATED_ACTION).setPackage(packageName)
        )
        finish()
    }
}
