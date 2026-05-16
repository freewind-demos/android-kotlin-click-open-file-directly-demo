package com.freewind.clickopenfiledemo.domain.model

import android.net.Uri

data class SelectedFile(
    val uri: Uri,
    val displayName: String,
    val mimeType: String?,
)
