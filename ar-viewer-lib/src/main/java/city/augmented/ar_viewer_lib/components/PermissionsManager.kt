package city.augmented.ar_viewer_lib.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

class PermissionsManager(
    private val rationaleMessage: String = "This app needs camera ang location permissions.",
    private val permissions: Array<String> = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA
    )
) {
    fun requestPermissions(activityCompat: Activity) {
        if (shouldShowRationale(activityCompat)) {
            AlertDialog.Builder(activityCompat)
                .setMessage("This app needs camera permission.")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    ActivityCompat.requestPermissions(
                        activityCompat,
                        permissions,
                        REQUEST_PERMISSIONS_INTERVAL
                    )
                }
                .setNegativeButton(
                    android.R.string.cancel
                ) { _, _ -> activityCompat.finish() }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                activityCompat,
                permissions,
                REQUEST_PERMISSIONS_INTERVAL
            )
        }
    }

    private fun shouldShowRationale(activityCompat: Activity) = permissions.all { permission ->
        ActivityCompat.shouldShowRequestPermissionRationale(activityCompat, permission)
    }

    fun isAllPermissionsGranted(context: Context) = permissions.all { permission ->
        ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val REQUEST_PERMISSIONS_INTERVAL = 1000
    }
}