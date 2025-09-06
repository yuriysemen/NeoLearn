package com.neolearn.course

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView


class CourseListener(
    private var context: Activity,
    private var webView: WebView,
    private var onComplete: () -> Boolean
) {

    @JavascriptInterface
    fun showPageFromStart() {
        Log.i(this.javaClass.name, "showPageFromStart()")
        context.runOnUiThread {
            webView.evaluateJavascript(
                "window.scrollTo(0, 0);",
                null
            )
        }
    }

    @JavascriptInterface
    fun completeLesson() {
        Log.i(this.javaClass.name, "showPageFromStart()")
        Handler(Looper.getMainLooper()).post {
            onComplete()
        }
    }
}