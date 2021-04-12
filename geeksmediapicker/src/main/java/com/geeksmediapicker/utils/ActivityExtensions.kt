package com.geeksmediapicker.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.geeksmediapicker.R
import kotlinx.android.synthetic.main.my_custom_toast.view.*

internal fun Activity.showPermissionSettingDialog() {
    val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
    alertDialogBuilder.setTitle("Permission needed")
    alertDialogBuilder.setMessage("Location permission needed to access location")
    alertDialogBuilder.setPositiveButton("Open Setting") { _, _ ->
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        })
        finish()
    }
    alertDialogBuilder.setNegativeButton("Cancel") { dialogInterface, i ->
        dialogInterface.dismiss()
        finish()
    }
    val dialog: AlertDialog = alertDialogBuilder.create()
    dialog.show()
}