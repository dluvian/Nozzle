package com.dluvian.nozzle.data.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString

fun copyAndToast(text: String, toast: String, context: Context, clip: ClipboardManager) {
    clip.setText(AnnotatedString(text))
    Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
}
