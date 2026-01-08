package com.lukekadigitalservices.lectionnairecatholique.ui

import android.content.Context
import android.content.Intent
import android.text.Html

fun shareLecture(context: Context, title: String, contentHtml: String) {
    val plainText = Html.fromHtml(contentHtml, Html.FROM_HTML_MODE_COMPACT).toString()
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_TEXT, "$title\n\n$plainText")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}