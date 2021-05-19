package com.alicebiometrics.aliceonboardingsampleapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import com.alicebiometrics.onboarding.AliceUrl


class ReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        val webview = findViewById<WebView>(R.id.wv_report)
        val webSettings = webview.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls =false
        webSettings.setSupportZoom(true)
        webSettings.defaultTextEncodingName = "utf-8"
        val userId = intent.getStringExtra("userId")
        webview.loadUrl(AliceUrl.dashboard +"#/users/$userId")
    }
}
