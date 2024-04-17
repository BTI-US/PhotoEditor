package com.burhanrashid52.photoediting.base

import android.R
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

/**
 * Created by Burhanuddin Rashid on 1/17/2018.
 */
open class BaseActivity : AppCompatActivity() {
    // Holds the ProgressDialog instance for showing loading progress in this activity.
    private var mProgressDialog: ProgressDialog? = null

    // Holds the permission string that is requested by the activity.
    private var mPermission: String? = null

    /**
     * A launcher for the permission request result.
     * This launcher is registered for the result of a permission request, and it calls the isPermissionGranted method
     * with the result of the permission request and the requested permission.
     */
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isPermissionGranted(it, mPermission)
    }

    /**
     * Requests a permission.
     * This method checks if the permission is already granted. If not, it launches a permission request.
     *
     * @param permission The permission to request.
     * @return True if the permission is already granted, false otherwise.
     */
    fun requestPermission(permission: String): Boolean {
        val isGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            mPermission = permission
            permissionLauncher.launch(permission)
        }
        return isGranted
    }

    /**
     * This method is called when a permission request result is received.
     * It is intended to be overridden by subclasses to handle the result of a permission request.
     *
     * @param isGranted True if the permission was granted, false otherwise.
     * @param permission The permission that was requested. This may be null if no permission was requested.
     */
    open fun isPermissionGranted(isGranted: Boolean, permission: String?) {}

    /**
     * Makes the activity full screen.
     * This method removes the title bar and sets the activity to full screen mode.
     */
    fun makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    /**
     * Shows a loading dialog with the given message.
     * This method creates a new ProgressDialog, sets its message and style, makes it non-cancelable, and shows it.
     *
     * @param message The message to be shown in the loading dialog.
     */
    protected fun showLoading(message: String) {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog?.run {
            setMessage(message)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(false)
            show()
        }
    }

    /**
     * Hides the loading dialog.
     * This method dismisses the ProgressDialog if it is currently shown.
     */
    protected fun hideLoading() {
        mProgressDialog?.dismiss()
    }

    /**
     * Shows a Snackbar with the given message.
     * This method first tries to find a view with the id 'content' to be used as the anchor for the Snackbar.
     * If such a view is found, it shows a Snackbar with the given message.
     * If no such view is found, it falls back to showing a Toast with the given message.
     *
     * @param message The message to be shown in the Snackbar or Toast.
     */
    protected fun showSnackbar(message: String) {
        val view = findViewById<View>(R.id.content)
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}