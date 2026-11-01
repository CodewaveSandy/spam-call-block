package com.codewaves.callblock

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class RuleStore(context: Context) {
    private val prefs = context.getSharedPreferences("blocking_rules_v1", Context.MODE_PRIVATE)
    var enabled: Boolean
        get() = prefs.getBoolean("enabled", false)
        set(value) { prefs.edit().putBoolean("enabled", value).apply() }

    fun rules(): List<BlockingRule> = try {
        val array = JSONArray(prefs.getString("rules", "[]"))
        (0 until array.length()).mapNotNull { index ->
            try {
                val item = array.getJSONObject(index)
                BlockingRule(item.getString("id"), item.getString("name"),
                    MatchType.valueOf(item.getString("type")), item.getString("value"),
                    item.optBoolean("enabled", true))
            } catch (_: Exception) { null }
        }
    } catch (_: Exception) { emptyList() }

    private fun save(rules: List<BlockingRule>) {
        val array = JSONArray()
        rules.forEach { rule -> array.put(JSONObject().apply {
            put("id", rule.id); put("name", rule.name); put("type", rule.type.name)
            put("value", rule.value); put("enabled", rule.enabled)
        }) }
        prefs.edit().putString("rules", array.toString()).apply()
    }
    fun put(rule: BlockingRule) {
        val current = rules().toMutableList()
        val index = current.indexOfFirst { it.id == rule.id }
        if (index < 0) current.add(rule) else current[index] = rule
        save(current)
    }
    fun delete(id: String) = save(rules().filterNot { it.id == id })
}
