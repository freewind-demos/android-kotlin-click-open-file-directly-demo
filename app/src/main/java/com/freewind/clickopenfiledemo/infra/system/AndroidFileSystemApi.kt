package com.freewind.clickopenfiledemo.infra.system

import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.freewind.clickopenfiledemo.domain.model.SelectedFile
import java.util.Locale

class AndroidFileSystemApi(
    private val appContext: Context,
) {
    private val contentResolver: ContentResolver
        get() = appContext.contentResolver

    fun createPickFileIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
    }

    fun readPickedFile(resultIntent: Intent?): SelectedFile? {
        val uri = resultIntent?.data ?: return null
        persistReadPermission(resultIntent, uri)
        return SelectedFile(
            uri = uri,
            displayName = queryDisplayName(uri),
            mimeType = resolveMimeType(uri),
        )
    }

    fun openWithDefaultApp(file: SelectedFile) {
        val resolvedMimeType = file.mimeType ?: "*/*"
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(file.uri, resolvedMimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            appContext.startActivity(openIntent)
        } catch (error: ActivityNotFoundException) {
            throw error
        }
    }

    private fun persistReadPermission(resultIntent: Intent, uri: Uri) {
        val grantedFlags = resultIntent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (grantedFlags == 0) {
            return
        }

        runCatching {
            contentResolver.takePersistableUriPermission(uri, grantedFlags)
        }
    }

    private fun queryDisplayName(uri: Uri): String {
        val fallbackName = uri.lastPathSegment ?: uri.toString()
        val cursor: Cursor = contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        ) ?: return fallbackName

        cursor.use {
            if (!it.moveToFirst()) {
                return fallbackName
            }

            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex < 0) {
                return fallbackName
            }
            return it.getString(nameIndex) ?: fallbackName
        }
    }

    private fun resolveMimeType(uri: Uri): String? {
        val directMimeType = contentResolver.getType(uri)
        if (directMimeType != null) {
            return directMimeType
        }

        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            .ifBlank {
                queryDisplayName(uri).substringAfterLast('.', "").lowercase(Locale.US)
            }
        if (extension.isBlank()) {
            return null
        }

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase(Locale.US))
    }
}
