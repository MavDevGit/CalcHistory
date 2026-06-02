package com.mavdev.calchistory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val historyManager = HistoryManager(application)
    
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _preview = MutableStateFlow("")
    val preview: StateFlow<String> = _preview.asStateFlow()

    private val _history = MutableStateFlow(historyManager.getHistory())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private val _isDarkMode = MutableStateFlow(historyManager.isDarkMode) // Load from prefs
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val df = DecimalFormat("0.########", DecimalFormatSymbols(Locale.US))

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
        historyManager.isDarkMode = _isDarkMode.value // Save to prefs
    }

    fun onKeyPress(key: String) {
        when (key) {
            "AC" -> clear()
            "DEL" -> delete()
            "=" -> calculate()
            else -> {
                if (_expression.value.isEmpty() && (key == "+" || key == "*" || key == "/" || key == "%")) {
                    val ans = historyManager.ans
                    if (ans.isNotEmpty()) {
                        _expression.value = ans + key
                    }
                } else if (_expression.value.isEmpty() && key == "-") {
                    _expression.value = "-"
                } else {
                    val operators = listOf("+", "-", "*", "/", "%")
                    val lastChar = _expression.value.takeLast(1)
                    if (operators.contains(key) && operators.contains(lastChar)) {
                        _expression.value = _expression.value.dropLast(1) + key
                    } else {
                        _expression.value += key
                    }
                }
            }
        }
        updatePreview()
    }

    private fun clear() {
        _expression.value = ""
    }

    private fun delete() {
        if (_expression.value.isNotEmpty()) {
            _expression.value = _expression.value.dropLast(1)
        }
    }

    private fun updatePreview() {
        val expr = _expression.value
        if (expr.isEmpty()) {
            _preview.value = ""
            return
        }
        
        // Don't evaluate if ends with operator or decimal
        val lastChar = expr.takeLast(1)
        if (listOf("+", "-", "*", "/", ".", "(").contains(lastChar)) {
            _preview.value = ""
            return
        }

        try {
            val result = MathParser(expr).parse()
            if (result.isFinite() && !result.isNaN()) {
                _preview.value = "= " + df.format(result)
            } else {
                _preview.value = ""
            }
        } catch (e: Exception) {
            _preview.value = ""
        }
    }

    private fun calculate() {
        val expr = _expression.value
        if (expr.trim().isEmpty() || expr == "-") return

        try {
            val result = MathParser(expr).parse()
            if (!result.isFinite() || result.isNaN()) throw Exception("Math Error")

            val formattedResult = df.format(result)
            
            val newItem = HistoryItem(
                id = System.currentTimeMillis(),
                rawExpr = expr,
                formattedExpr = formatDisplay(expr),
                result = formattedResult
            )
            
            val newList = _history.value.toMutableList()
            newList.add(newItem)
            _history.value = newList
            historyManager.saveHistory(newList)
            historyManager.ans = formattedResult
            
            _expression.value = ""
            updatePreview()
        } catch (e: Exception) {
            _preview.value = "Error"
        }
    }

    fun clearHistory() {
        historyManager.clearHistory()
        _history.value = emptyList()
        _expression.value = ""
        _preview.value = ""
    }

    fun reuseFormula(rawExpr: String) {
        _expression.value = rawExpr
        updatePreview()
    }

    fun reuseResult(res: String) {
        _expression.value += res
        updatePreview()
    }

    fun sumHistory() {
        val currentHistory = _history.value
        if (currentHistory.isEmpty()) return
        
        var total = 0.0
        for (item in currentHistory) {
            try {
                total += item.result.toDouble()
            } catch (e: Exception) {
                // Ignore
            }
        }
        reuseResult(df.format(total))
    }

    fun deleteHistoryItem(id: Long) {
        val newList = _history.value.filter { it.id != id }
        _history.value = newList
        historyManager.saveHistory(newList)
    }

    fun formatDisplay(expr: String): String {
        return expr
            .replace("*", " × ")
            .replace("/", " ÷ ")
            .replace("+", " + ")
            .replace("-", " - ")
            .replace("  ", " ") // Cleanup any potential double spaces
            .trim()
            .replace(Regex("^-\\s+"), "-") // Fix leading minus sign spacing
    }
}
