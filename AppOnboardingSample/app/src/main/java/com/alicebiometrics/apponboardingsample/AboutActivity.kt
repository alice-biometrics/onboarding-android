package com.alicebiometrics.apponboardingsample

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alicebiometrics.onboarding.api.Onboarding
import android.content.Intent
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.View
import androidx.appcompat.widget.Toolbar


class AboutActivity : AppCompatActivity() {

    private lateinit var frameworkVersionTextView: TextView
    private lateinit var appVersionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        frameworkVersionTextView = findViewById(R.id.tv_framework_version)
        appVersionTextView = findViewById(R.id.tv_app_version)
        frameworkVersionTextView.text = Onboarding.version
        appVersionTextView.text = BuildConfig.VERSION_NAME
        setSupportActionBar(findViewById(R.id.about_toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        val toolbar: Toolbar = findViewById(R.id.about_toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

}
