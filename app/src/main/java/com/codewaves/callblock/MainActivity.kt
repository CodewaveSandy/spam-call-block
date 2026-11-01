package com.codewaves.callblock

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.role.RoleManager
import android.content.Intent
import android.content.res.ColorStateList
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private val ink = Color.rgb(30, 34, 46)
    private val muted = Color.rgb(112, 116, 130)
    private val accent = Color.rgb(100, 78, 215)
    private val canvas = Color.rgb(247, 247, 251)
    private val lavender = Color.rgb(237, 232, 255)
    private val green = Color.rgb(39, 116, 89)
    private lateinit var store: RuleStore
    private lateinit var page: LinearLayout
    private val roles: RoleManager get() = getSystemService(RoleManager::class.java)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun hasRole() = roles.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
        roles.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    private fun hasContacts() = checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = RuleStore(this)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        window.statusBarColor = canvas
        window.navigationBarColor = canvas
        render()
    }

    override fun onResume() { super.onResume(); if (::store.isInitialized) render() }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        render()
    }

    private fun shape(color: Int, radius: Int = 22): GradientDrawable = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
    }
    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun row() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    private fun text(value: String, size: Float = 16f, color: Int = ink, bold: Boolean = false) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color)
        typeface = Typeface.create(if (bold) "sans-serif-medium" else "sans-serif", Typeface.NORMAL)
        setLineSpacing(dp(3).toFloat(), 1f)
    }
    private fun LinearLayout.gap(height: Int) { addView(View(this@MainActivity), LinearLayout.LayoutParams(1, dp(height))) }
    private fun LinearLayout.full(view: View, top: Int = 0) {
        addView(view, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(top) })
    }
    private fun card(color: Int = Color.WHITE) = column().apply {
        background = shape(color); setPadding(dp(22), dp(22), dp(22), dp(22))
    }
    private fun button(label: String, primary: Boolean = false, action: () -> Unit) = Button(this).apply {
        text = label; isAllCaps = false; textSize = 15f
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        setTextColor(if (primary) Color.WHITE else accent)
        background = shape(if (primary) accent else lavender, 16)
        minHeight = dp(52); minimumHeight = dp(52)
        setPadding(dp(18), dp(12), dp(18), dp(12))
        setOnClickListener { action() }
    }
    private fun toggle(checked: Boolean, label: String, onChange: (Boolean) -> Unit) = Switch(this).apply {
        isChecked = checked; contentDescription = label; minHeight = dp(48); minWidth = dp(52)
        buttonTintList = ColorStateList.valueOf(accent)
        thumbTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(accent, muted))
        trackTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(Color.rgb(198, 187, 246), Color.rgb(220, 221, 229)))
        setOnCheckedChangeListener { _, value -> onChange(value) }
    }

    private fun render() {
        val rules = store.rules()
        val activeCount = rules.count { it.enabled }
        val enabled = store.enabled
        val ready = hasRole()
        val outer = column().apply { setBackgroundColor(canvas) }
        outer.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets
        }
        val scroll = ScrollView(this).apply { isFillViewport = true; clipToPadding = false }
        page = column().apply { setPadding(dp(24), dp(22), dp(24), dp(32)) }
        scroll.addView(page)
        outer.full(scroll)
        setContentView(outer)
        outer.requestApplyInsets()

        val header = row()
        header.addView(text("quietly", 27f, ink, true), LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(button("Settings") { settings() })
        page.full(header)
        page.gap(28)
        page.full(text("Your time.\nYour rules.", 38f, ink, true))
        page.full(text("A little less noise. A lot more focus.", 16f, muted), 8)
        page.gap(24)

        val protection = card(if (enabled && ready) lavender else Color.WHITE)
        val protectionRow = row()
        val protectionCopy = column()
        protectionCopy.full(text(when { !enabled -> "Protection paused"; !ready -> "Finish setup"; activeCount == 0 -> "Ready when you are"; else -> "Your focus is protected" }, 20f, ink, true))
        protectionCopy.full(text(when {
            !enabled -> "All calls pass through normally."
            !ready -> "Choose quietly as your screening app."
            activeCount == 0 -> "Add a rule to start blocking."
            else -> "$activeCount active ${if (activeCount == 1) "rule" else "rules"} · silent blocking"
        }, 14f, muted), 6)
        protectionRow.addView(protectionCopy, LinearLayout.LayoutParams(0, -2, 1f))
        protectionRow.addView(toggle(enabled, "Enable call blocking") {
            store.enabled = it
            if (it && !hasRole()) requestScreening()
            render()
        })
        protection.full(protectionRow)
        if (enabled && !ready) protection.full(button("Complete setup", true) { requestScreening() }, 18)
        page.full(protection)

        if (enabled && ready && !hasContacts()) {
            val contactCard = card(Color.rgb(255, 246, 226))
            contactCard.full(text("Include saved contacts", 16f, ink, true))
            contactCard.full(text("Android skips saved callers without Contacts access. Your contacts stay on your phone.", 14f, muted), 6)
            contactCard.full(button("Allow Contacts") { requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 2) }, 14)
            page.full(contactCard, 12)
        }
        page.gap(30)
        val rulesHeading = row()
        rulesHeading.addView(text("Your rules", 23f, ink, true), LinearLayout.LayoutParams(0, -2, 1f))
        rulesHeading.addView(text("${rules.size} saved", 14f, muted))
        page.full(rulesHeading)
        page.full(text("A call is blocked when any active rule matches.", 14f, muted), 6)
        page.full(button("+  Create a rule", true) { editRule(null) }, 18)
        if (rules.isEmpty()) {
            val empty = card()
            empty.full(text("Make room for what matters.", 20f, ink, true))
            empty.full(text("Block a number, a prefix, or a pattern. You decide which calls get through.", 15f, muted), 10)
            empty.gap(16)
            empty.full(text("STARTS WITH    +91140\nENDS WITH        1234\nPATTERN             8697??????", 13f, accent))
            page.full(empty, 14)
        } else rules.forEach { rule ->
            val item = card()
            val top = row()
            val copy = column()
            copy.full(text(rule.name, 18f, if (rule.enabled) ink else muted, true))
            copy.full(text(rule.type.label.uppercase(), 11f, accent, true), 5)
            top.addView(copy, LinearLayout.LayoutParams(0, -2, 1f))
            top.addView(toggle(rule.enabled, "Enable ${rule.name}") { store.put(rule.copy(enabled = it)); render() })
            item.full(top)
            item.full(text(rule.value, 25f, if (rule.enabled) ink else muted, true).apply {
                typeface = Typeface.create("monospace", Typeface.NORMAL)
            }, 14)
            item.full(text(if (!rule.enabled) "Paused · kept for later" else if (!enabled) "Waiting · protection is paused" else if (!ready) "Waiting · complete setup" else "Active · reject silently", 13f, if (rule.enabled && enabled && ready) green else muted), 8)
            val actions = row()
            actions.addView(button("Edit") { editRule(rule) }, LinearLayout.LayoutParams(0, -2, 1f))
            actions.addView(button("Delete") { confirmDelete(rule) }, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(10) })
            item.full(actions, 18)
            page.full(item, 14)
        }
        page.full(button("Try a number") { testNumber() }, 20)
        page.full(text("On-device matching. No account. No uploads.\nBlocked calls may still appear in call history.", 12f, muted).apply { gravity = Gravity.CENTER }, 22)
    }

    private fun requestScreening() {
        if (roles.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING), 1)
        } else AlertDialog.Builder(this).setTitle("Screening unavailable")
            .setMessage("This device does not offer Android call screening.").setPositiveButton("OK", null).show()
    }

    private fun input(hintValue: String, initial: String = "") = EditText(this).apply {
        hint = hintValue; setText(initial); textSize = 16f; setTextColor(ink); setHintTextColor(muted)
        backgroundTintList = ColorStateList.valueOf(accent)
        setSingleLine(true); setPadding(dp(8), dp(12), dp(8), dp(12))
    }
    private fun watch(edit: EditText, action: () -> Unit) {
        edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { action() }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun editRule(existing: BlockingRule?) {
        val body = column().apply { setPadding(dp(24), dp(8), dp(24), dp(12)) }
        body.full(text("Give unwanted calls a clear boundary.", 14f, muted))
        val name = input("Rule name (optional)", existing?.name ?: "")
        body.full(name, 14)
        body.full(text("MATCH TYPE", 11f, accent, true), 20)
        val types = MatchType.values()
        val type = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, types.map { it.label })
            setSelection(existing?.type?.ordinal ?: 0)
            minimumHeight = dp(52)
        }
        body.full(type)
        val detail = text("", 13f, muted)
        body.full(detail, 4)
        val number = input("e.g. +918697", existing?.value ?: "")
        number.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        body.full(number, 12)
        body.full(text("Use + and a country code for international matching. Without +, Indian +91 calls match their 10-digit number. Spaces and dashes are ignored.", 13f, muted), 8)
        val feedback = text("", 13f, accent)
        body.full(feedback, 14)
        fun preview() {
            val selected = types[type.selectedItemPosition.coerceAtLeast(0)]
            detail.text = selected.description
            number.hint = when (selected) {
                MatchType.PREFIX -> "e.g. +918697 or 8697"
                MatchType.SUFFIX -> "e.g. 1234"
                MatchType.EXACT -> "e.g. +918697123456"
                MatchType.CONTAINS -> "e.g. 8697"
                MatchType.WILDCARD -> "e.g. 8697?????? or +91140*"
            }
            feedback.text = if (number.text.isEmpty()) "" else RuleEngine.error(selected, number.text.toString())
                ?: "${selected.label}: ${RuleEngine.clean(number.text.toString())}"
        }
        type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { preview() }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        watch(number) { preview() }
        val dialog = AlertDialog.Builder(this).setTitle(if (existing == null) "Create a rule" else "Edit rule")
            .setView(ScrollView(this).apply { addView(body) })
            .setNegativeButton("Cancel", null).setPositiveButton("Save rule", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(accent)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val selected = types[type.selectedItemPosition]
                val value = RuleEngine.clean(number.text.toString())
                val error = RuleEngine.error(selected, value)
                if (error != null) { number.error = error; return@setOnClickListener }
                val label = name.text.toString().trim().take(60).ifEmpty { "${selected.label} $value" }
                val updated = if (existing != null) existing.copy(name = label, type = selected, value = value)
                    else BlockingRule(name = label, type = selected, value = value)
                store.put(updated)
                dialog.dismiss(); render()
            }
        }
        dialog.show()
    }

    private fun confirmDelete(rule: BlockingRule) {
        AlertDialog.Builder(this).setTitle("Delete this rule?")
            .setMessage("“${rule.name}” will be removed. You can pause it with its switch instead.")
            .setNegativeButton("Keep rule", null).setPositiveButton("Delete") { _, _ -> store.delete(rule.id); render() }.show()
    }

    private fun testNumber() {
        val body = column().apply { setPadding(dp(24), dp(8), dp(24), dp(16)) }
        body.full(text("Preview your saved rules without making a call.", 14f, muted))
        val number = input("Incoming number, e.g. +918697123456").apply { inputType = InputType.TYPE_CLASS_PHONE }
        body.full(number, 14)
        val result = text("Enter a number to check.", 16f, ink, true)
        body.full(result, 18)
        watch(number) {
            val value = RuleEngine.clean(number.text.toString())
            val digits = value.removePrefix("+")
            val matches = store.rules().filter { RuleEngine.matches(value, it) }
            result.text = when {
                digits.isEmpty() -> "Enter a number to check."
                digits.any { it !in '0'..'9' } || digits.length !in 7..15 -> "Enter a complete phone number (7–15 digits)."
                !store.enabled -> "Allowed · protection is paused."
                !hasRole() -> "Allowed · complete call-screening setup first."
                matches.isEmpty() -> "Allowed · no active rule matches."
                else -> "Would block · ${matches.joinToString { it.name }}${if (!hasContacts()) "\nSaved callers require Contacts access." else ""}"
            }
        }
        AlertDialog.Builder(this).setTitle("Try a number").setView(body).setPositiveButton("Done", null).show()
    }

    private fun settings() {
        val body = column().apply { setPadding(dp(24), dp(8), dp(24), dp(12)) }
        body.full(text("CALL SCREENING", 11f, accent, true))
        body.full(text(if (hasRole()) "Selected as the screening app" else "Not selected as the screening app", 16f, ink, true), 8)
        body.full(button("Choose screening app") { requestScreening() }, 14)
        body.full(text("CONTACTS", 11f, accent, true), 24)
        body.full(text(if (hasContacts()) "Saved callers are included" else "Saved callers are excluded", 16f, ink, true), 8)
        body.full(text("Contacts access lets Android send saved callers for screening. The app never reads or uploads your address book.", 13f, muted), 8)
        body.full(button("Manage permissions") {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
        }, 14)
        body.full(button("Default apps") { startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)) }, 10)
        body.full(text("quietly · version 0.3.0\nRules stay on this device. Regular phone calls only.", 12f, muted), 24)
        AlertDialog.Builder(this).setTitle("Your setup").setView(ScrollView(this).apply { addView(body) }).setPositiveButton("Done", null).show()
    }
}
