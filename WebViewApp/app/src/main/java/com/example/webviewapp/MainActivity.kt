package com.example.webviewapp

import android.content.res.Configuration
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)

        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                actualizarUI()
            }
        }

        if (savedInstanceState == null) {
            webView.loadUrl("file:///android_asset/index.html")
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    private fun actualizarUI() {
        val mensaje = getString(R.string.mensaje)

        val colorTexto = String.format(
            "#%06X",
            0xFFFFFF and getColor(R.color.textColor)
        )

        val colorFondo = String.format(
            "#%06X",
            0xFFFFFF and getColor(R.color.bgColor)   // 👈 TU VARIABLE
        )

        val script = "javascript:actualizarUI('$mensaje','$colorTexto','$colorFondo')"

        webView.evaluateJavascript(script, null)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actualizarUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }
}