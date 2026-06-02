package com.mavdev.calchistory

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class HistoryItem(
    val id: Long,
    val rawExpr: String,
    val formattedExpr: String,
    val result: String
)

class HistoryManager(context: Context) {
    private val prefs = context.getSharedPreferences("calc_prefs", Context.MODE_PRIVATE)

    var ans: String
        get() = prefs.getString("calc_ans", "") ?: ""
        set(value) = prefs.edit().putString("calc_ans", value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean("calc_dark_mode", true)
        set(value) = prefs.edit().putBoolean("calc_dark_mode", value).apply()

    fun getHistory(): List<HistoryItem> {
        val historyStr = prefs.getString("calc_history", "[]") ?: "[]"
        val list = mutableListOf<HistoryItem>()
        try {
            val jsonArray = JSONArray(historyStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    HistoryItem(
                        id = obj.getLong("id"),
                        rawExpr = obj.getString("rawExpr"),
                        formattedExpr = obj.getString("formattedExpr"),
                        result = obj.getString("result")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveHistory(history: List<HistoryItem>) {
        val jsonArray = JSONArray()
        for (item in history) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("rawExpr", item.rawExpr)
                put("formattedExpr", item.formattedExpr)
                put("result", item.result)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("calc_history", jsonArray.toString()).apply()
    }

    fun clearHistory() {
        prefs.edit().remove("calc_history").remove("calc_ans").apply()
    }
}
