package com.nunchuk.android.auth.util

import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.text.util.Linkify
import android.widget.TextView
import com.nunchuk.android.widget.NCEditTextView
import java.util.regex.Pattern

internal fun String?.orUnknownError() = this ?: "Unknown Error"

fun TextView.setUnderlineText(text: String) {
    SpannableString(text).apply {
        setSpan(UnderlineSpan(), 0, length, 0)
        setText(this)
    }
}

fun TextView.linkify(textToLink: String, url: String) {
    val pattern = Pattern.compile(textToLink)
    Linkify.addLinks(this, pattern, url, { _, _, _ -> true }, { _, _ -> "" })
}

fun NCEditTextView.getTextTrimmed(): String {
    return getEditText().trim()
}