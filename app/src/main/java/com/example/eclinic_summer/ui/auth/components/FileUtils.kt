package com.example.eclinic_summer.ui.auth.components

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

/**
 * Extension function to get the file name from a Uri.
 *
 * @receiver ContentResolver used to query the Uri.
 * @param uri The Uri of the file.
 * @return The display name of the file.
 */
fun ContentResolver.getFileNameFromUri(uri: Uri): String {
    var name = ""
    val cursor = query(uri, null, null, null, null)
    cursor?.use {
        it.moveToFirst()
        name = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
    }
    return name
}
