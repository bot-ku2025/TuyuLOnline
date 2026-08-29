package com.example.tuyulonline

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.Locale
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var headerRow: LinearLayout
    private lateinit var addButton: Button
    private lateinit var spoofButton: Button
    private lateinit var contentPanel: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var appGrid: LinearLayout
    private lateinit var appPackageManager: PackageManager
    private lateinit var biometricExecutor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var securePrefs: EncryptedSharedPreferences

    private val appList = mutableListOf<AppEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toggleFullscreen()
        setupSecurePreferences()
        appPackageManager = packageManager

        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(24, 24, 24, 16)
            setBackgroundColor(Color.BLACK)
        }

        val title = TextView(this).apply {
            text = "TuyuL Online"
            textSize = 18f
            setTextColor(Color.parseColor("#00FF00"))
            typeface = android.graphics.Typeface.MONOSPACE
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 16, 0)
        }

        addButton = Button(this).apply {
            text = "[+]"
            setTextColor(Color.parseColor("#00FF00"))
            setBackgroundColor(Color.BLACK)
            setTextSize(18f)
            typeface = android.graphics.Typeface.MONOSPACE
            setOnClickListener { loadInstalledApps() }
        }

        spoofButton = Button(this).apply {
            text = "[SPOOF]"
            setTextColor(Color.parseColor("#00FF00"))
            setBackgroundColor(Color.BLACK)
            setTextSize(15f)
            typeface = android.graphics.Typeface.MONOSPACE
            setOnClickListener { openSpoofPanel() }
        }

        headerRow.addView(title)
        headerRow.addView(addButton)
        headerRow.addView(spoofButton)

        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                2
            )
            setBackgroundColor(Color.parseColor("#00FF00"))
        }

        contentPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        statusText = TextView(this).apply {
            text = "STATUS: READY\nSECURE FOLDER LOCKED\nWAITING FOR BIOMETRIC AUTH"
            setTextColor(Color.parseColor("#00FF00"))
            textSize = 15f
            typeface = android.graphics.Typeface.MONOSPACE
            setLineSpacing(4f, 1.2f)
            setPadding(0, 0, 0, 20)
        }

        appGrid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
        }

        contentPanel.addView(statusText)
        contentPanel.addView(appGrid)

        mainLayout.addView(headerRow)
        mainLayout.addView(divider)
        mainLayout.addView(contentPanel)

        setContentView(mainLayout)

        biometricExecutor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, biometricExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    statusText.text = "STATUS: AUTHORIZED\nSECURE WORKSPACE ACTIVE\nTuyuL Online ready."
                    refreshAppGrid()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    statusText.text = "STATUS: ACCESS DENIED\nBIOMETRIC LOCKED\n${errString}"
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    statusText.text = "STATUS: ATTEMPT FAILED\nRETRY REQUIRED"
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate to TuyuL Online")
            .setSubtitle("Use your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        if (supportsBiometricAuth()) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            statusText.text = "STATUS: BIOMETRIC UNSUPPORTED\nFALLBACK MODE ACTIVE"
            refreshAppGrid()
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isFinishing && !isChangingConfigurations) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun toggleFullscreen() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }

    private fun supportsBiometricAuth(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun setupSecurePreferences() {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        securePrefs = EncryptedSharedPreferences.create(
            this,
            "tuyul_secure_store",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    private fun loadInstalledApps() {
        val installed = appPackageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        appList.clear()

        for (app in installed) {
            if ((app.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                val label = appPackageManager.getApplicationLabel(app).toString()
                val packageName = app.packageName
                appList.add(AppEntry(label, packageName))
            }
        }

        appList.sortBy { it.label.lowercase(Locale.getDefault()) }
        refreshAppGrid()
    }

    private fun refreshAppGrid() {
        appGrid.removeAllViews()
        if (appList.isEmpty()) {
            val empty = TextView(this).apply {
                text = "NO APPLICATIONS IN SANDBOX\nCLICK [+] TO SCAN DEVICE"
                setTextColor(Color.parseColor("#00FF00"))
                typeface = android.graphics.Typeface.MONOSPACE
                textSize = 14f
            }
            appGrid.addView(empty)
            return
        }

        for (entry in appList) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 12, 0, 12)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val appName = TextView(this).apply {
                text = entry.label
                setTextColor(Color.parseColor("#00FF00"))
                textSize = 15f
                typeface = android.graphics.Typeface.MONOSPACE
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val packageName = TextView(this).apply {
                text = entry.packageName
                setTextColor(Color.parseColor("#00FF00"))
                textSize = 12f
                typeface = android.graphics.Typeface.MONOSPACE
                alpha = 0.8f
            }

            val addToSandbox = Button(this).apply {
                text = "[ADD]"
                setTextColor(Color.parseColor("#00FF00"))
                setBackgroundColor(Color.BLACK)
                typeface = android.graphics.Typeface.MONOSPACE
                setOnClickListener {
                    addAppToSandbox(entry)
                }
            }

            row.addView(appName)
            row.addView(packageName)
            row.addView(addToSandbox)
            appGrid.addView(row)
        }
    }

    private fun addAppToSandbox(entry: AppEntry) {
        val listKey = "sandbox_apps"
        val existing = securePrefs.getString(listKey, "") ?: ""
        val entries = existing.split(";\n").filter { it.isNotBlank() }.toMutableSet()
        entries.add("${entry.label}|${entry.packageName}")
        securePrefs.edit().putString(listKey, entries.joinToString(";\n")).apply()
        statusText.text = "STATUS: APP ADDED\n${entry.label}\n${entry.packageName}"
        refreshAppGrid()
    }

    private fun openSpoofPanel() {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(20, 20, 20, 20)
        }

        val title = TextView(this).apply {
            text = "[SPOOF CONFIG]"
            setTextColor(Color.parseColor("#00FF00"))
            textSize = 18f
            typeface = android.graphics.Typeface.MONOSPACE
        }

        val spoofInfo = TextView(this).apply {
            text = "LOCAL DEVICE IDENTITY ONLY\nISOLATED TO TuyuL Online CONTAINER"
            setTextColor(Color.parseColor("#00FF00"))
            typeface = android.graphics.Typeface.MONOSPACE
            textSize = 13f
            setPadding(0, 12, 0, 18)
        }

        val androidId = Button(this).apply {
            text = "Spoof AndroidID"
            setOnClickListener { setSpoofValue("android_id", Settings.Secure.ANDROID_ID) }
        }

        val model = Button(this).apply {
            text = "Spoof Device Model"
            setOnClickListener { setSpoofValue("device_model", Build.MODEL) }
        }

        val brand = Button(this).apply {
            text = "Spoof Brand"
            setOnClickListener { setSpoofValue("device_brand", Build.BRAND) }
        }

        val serial = Button(this).apply {
            text = "Spoof IMEI / Serial"
            setOnClickListener { setSpoofValue("device_serial", Build.SERIAL) }
        }

        panel.addView(title)
        panel.addView(spoofInfo)
        panel.addView(androidId)
        panel.addView(model)
        panel.addView(brand)
        panel.addView(serial)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(panel)
            .setPositiveButton("Close", null)
            .create()
        dialog.show()
    }

    private fun setSpoofValue(key: String, value: String) {
        val safeKey = "spoof_$key"
        securePrefs.edit().putString(safeKey, value).apply()
        statusText.text = "STATUS: SPOOF REGISTERED\nKEY: $safeKey\nVALUE: $value"
    }

    data class AppEntry(
        val label: String,
        val packageName: String
    )
}

class TuyuLApplication : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

class SandboxActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = TextView(this).apply {
            text = "SANDBOX CONTAINER ACTIVE"
            textSize = 20f
            setTextColor(Color.parseColor("#00FF00"))
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
        }
        setContentView(text)
    }
}
