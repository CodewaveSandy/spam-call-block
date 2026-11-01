package com.codewaves.callblock

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val padding = (24 * resources.displayMetrics.density).toInt()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(padding, padding, padding, padding)
        }

        fun addText(value: String, size: Float) {
            content.addView(TextView(this).apply {
                text = value
                textSize = size
                setPadding(0, 0, 0, padding)
            })
        }

        addText(getString(R.string.heading), 30f)
        addText(getString(R.string.build_success), 20f)
        addText(getString(R.string.version, "0.1.0"), 16f)
        addText(getString(R.string.scope), 16f)

        content.addView(Button(this).apply {
            text = getString(R.string.test_button)
            setOnClickListener {
                Toast.makeText(this@MainActivity, R.string.test_success, Toast.LENGTH_SHORT).show()
            }
        })
        setContentView(content, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
    }
}
