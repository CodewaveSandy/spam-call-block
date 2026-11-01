package com.codewaves.callblock

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var enableButton: Button
    private lateinit var contactsButton: Button
    private val roles: RoleManager get() = getSystemService(RoleManager::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val padding = (24 * resources.displayMetrics.density).toInt()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding * 2, padding, padding * 2)
        }
        fun addText(resource: Int, size: Float): TextView = TextView(this).apply {
            setText(resource)
            textSize = size
            setPadding(0, 0, 0, padding)
            content.addView(this)
        }
        addText(R.string.heading, 30f)
        addText(R.string.rule_description, 20f)
        status = addText(R.string.checking, 16f)
        addText(R.string.setup_description, 16f)
        enableButton = Button(this).apply {
            setText(R.string.enable_screening)
            setOnClickListener {
                if (roles.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                    startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING), 1)
                }
            }
            content.addView(this)
        }
        contactsButton = Button(this).apply {
            setText(R.string.allow_contacts)
            setOnClickListener { requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 2) }
            content.addView(this)
        }
        content.addView(Button(this).apply {
            setText(R.string.app_settings)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")))
            }
        })
        content.addView(Button(this).apply {
            setText(R.string.default_apps)
            setOnClickListener { startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)) }
        })
        addText(R.string.testing_description, 16f)
        setContentView(ScrollView(this).apply { addView(content) })
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refreshStatus()
    }

    private fun refreshStatus() {
        val available = roles.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)
        val enabled = available && roles.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        val contacts = checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        status.setText(when {
            !available -> R.string.screening_unavailable
            !enabled -> R.string.screening_disabled
            !contacts -> R.string.screening_partial
            else -> R.string.screening_ready
        })
        enableButton.isEnabled = available && !enabled
        contactsButton.isEnabled = !contacts
    }
}
