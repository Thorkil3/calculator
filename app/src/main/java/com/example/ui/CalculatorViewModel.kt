package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HistoryEntry
import com.example.data.HistoryRepository
import com.example.util.ExpressionEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HistoryRepository
    val historyList: StateFlow<List<HistoryEntry>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HistoryRepository(database.historyDao())
        historyList = repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    val expression = MutableStateFlow("")
    val displayResult = MutableStateFlow("0")

    val isScientific = MutableStateFlow(false)
    val isDegreeMode = MutableStateFlow(true)
    val vibrationEnabled = MutableStateFlow(true)
    
    // Tracks if the last action was an evaluation (equals press)
    private var justEvaluated = false

    fun appendCharacter(char: String) {
        if (justEvaluated) {
            if ("0123456789.eπ(".contains(char) || char.length > 2) {
                expression.value = ""
                displayResult.value = ""
            }
            justEvaluated = false
        }

        val currentExpr = expression.value
        
        // Handle parenthesis or function append
        if (char.endsWith("(")) {
            expression.value = currentExpr + char
            updateDisplayPreview()
            return
        }

        // Regular append
        if (currentExpr == "0" && char != ".") {
            expression.value = char
        } else {
            expression.value = currentExpr + char
        }
        updateDisplayPreview()
    }

    fun appendOperator(op: String) {
        var currentExpr = expression.value
        if (justEvaluated) {
            // Chain with previous result
            currentExpr = displayResult.value
            justEvaluated = false
        }

        if (currentExpr.isEmpty()) {
            if (op == "-") {
                expression.value = "-"
            }
            return
        }

        val lastChar = currentExpr.last().toString()
        val operators = listOf("+", "-", "×", "÷", "^")
        
        if (operators.contains(lastChar)) {
            // Replace last operator
            expression.value = currentExpr.dropLast(1) + op
        } else {
            expression.value = currentExpr + op
        }
        updateDisplayPreview()
    }

    private fun updateDisplayPreview() {
        val expr = expression.value
        if (expr.isEmpty()) {
            displayResult.value = "0"
            return
        }
        // Attempt to calculate a live preview for well-formatted expressions
        try {
            // Check if there is anything to calculate (has operators or functions)
            val cleanExpr = expr.replace("−", "-").replace("×", "*").replace("÷", "/")
            val hasOperatorsOrFuncs = cleanExpr.any { "+-*/^(".contains(it) } || 
                    listOf("sin", "cos", "tan", "log", "ln", "sqrt").any { cleanExpr.contains(it) }
                    
            if (hasOperatorsOrFuncs) {
                // Try evaluating. If it ends with operator, trim it for preview
                var evalExpr = expr
                val operators = listOf("+", "-", "×", "÷", "^")
                while (evalExpr.isNotEmpty() && (operators.contains(evalExpr.last().toString()) || evalExpr.last() == '(')) {
                    evalExpr = evalExpr.dropLast(1)
                }
                
                // Add missing closing parentheses for a cleaner live preview
                val openCount = evalExpr.count { it == '(' }
                val closeCount = evalExpr.count { it == ')' }
                if (openCount > closeCount) {
                    evalExpr += ")".repeat(openCount - closeCount)
                }

                if (evalExpr.isNotEmpty()) {
                    val preview = ExpressionEvaluator.evaluate(evalExpr, isDegreeMode.value)
                    if (preview.isFinite()) {
                        displayResult.value = formatResult(preview)
                    }
                }
            } else {
                displayResult.value = expr
            }
        } catch (e: Exception) {
            // If preview fails, keep last numeric segment or fallback gracefully
            val lastNumberSegment = expr.split("+", "-", "×", "÷", "^", "(").lastOrNull() ?: ""
            if (lastNumberSegment.isNotEmpty()) {
                displayResult.value = lastNumberSegment
            }
        }
    }

    fun clear() {
        expression.value = ""
        displayResult.value = "0"
        justEvaluated = false
    }

    fun backspace() {
        if (justEvaluated) {
            clear()
            return
        }
        val currentExpr = expression.value
        if (currentExpr.isNotEmpty()) {
            // Check if it's a scientific function backspace, like "sin("
            val funcs = listOf("sin(", "cos(", "tan(", "log(", "ln(", "sqrt(")
            var removed = false
            for (f in funcs) {
                if (currentExpr.endsWith(f)) {
                    expression.value = currentExpr.dropLast(f.length)
                    removed = true
                    break
                }
            }
            if (!removed) {
                expression.value = currentExpr.dropLast(1)
            }
            updateDisplayPreview()
        }
    }

    fun handlePercent() {
        val currentExpr = expression.value
        if (currentExpr.isEmpty()) return

        if (justEvaluated) {
            val resVal = displayResult.value.toDoubleOrNull()
            if (resVal != null) {
                val value = resVal / 100.0
                expression.value = formatResult(value)
                displayResult.value = expression.value
            }
            return
        }

        // Divide the last operand by 100
        val parts = currentExpr.split(Regex("(?<=[+\\-×÷^])|(?=[+\\-×÷^])"))
        if (parts.isNotEmpty()) {
            val lastPart = parts.last()
            val numValue = lastPart.toDoubleOrNull()
            if (numValue != null) {
                val value = numValue / 100.0
                expression.value = currentExpr.dropLast(lastPart.length) + formatResult(value)
                updateDisplayPreview()
            }
        }
    }

    fun handleSignToggle() {
        if (justEvaluated) {
            val resVal = displayResult.value.toDoubleOrNull()
            if (resVal != null) {
                val value = -resVal
                expression.value = formatResult(value)
                displayResult.value = expression.value
            }
            return
        }

        val currentExpr = expression.value
        if (currentExpr.isEmpty()) {
            expression.value = "-"
            return
        }

        // Toggle sign of the last separate numeric operand
        val parts = currentExpr.split(Regex("(?<=[+\\-×÷^(])|(?=[+\\-×÷^(])"))
        if (parts.isNotEmpty()) {
            val lastOperand = parts.last()
            val numValue = lastOperand.toDoubleOrNull()
            if (numValue != null) {
                val negatedVal = -numValue
                expression.value = currentExpr.dropLast(lastOperand.length) + formatResult(negatedVal)
            } else if (lastOperand == "-") {
                expression.value = currentExpr.dropLast(1)
            } else {
                expression.value = currentExpr + "-"
            }
            updateDisplayPreview()
        }
    }

    fun evaluate() {
        val currentExpr = expression.value
        if (currentExpr.isEmpty()) return

        try {
            // Trim trailing operators
            var evalExpr = currentExpr
            val operators = listOf("+", "-", "×", "÷", "^")
            while (evalExpr.isNotEmpty() && operators.contains(evalExpr.last().toString())) {
                evalExpr = evalExpr.dropLast(1)
            }

            // Close parentheses if open
            val openCount = evalExpr.count { it == '(' }
            val closeCount = evalExpr.count { it == ')' }
            if (openCount > closeCount) {
                evalExpr += ")".repeat(openCount - closeCount)
            }

            val result = ExpressionEvaluator.evaluate(evalExpr, isDegreeMode.value)
            
            if (result.isNaN() || result.isInfinite()) {
                displayResult.value = "Error"
                return
            }

            val formatted = formatResult(result)
            
            // Format equation for history
            val equationExpr = if (evalExpr != currentExpr) evalExpr else currentExpr
            
            // Insert in DB History
            viewModelScope.launch {
                repository.insert(HistoryEntry(expression = equationExpr, result = formatted))
            }

            // Set state
            expression.value = "$equationExpr ="
            displayResult.value = formatted
            justEvaluated = true
            
        } catch (e: Exception) {
            displayResult.value = "Error"
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun useHistoryItem(entry: HistoryEntry) {
        expression.value = entry.expression
        displayResult.value = entry.result
        justEvaluated = false
    }

    private fun formatResult(value: Double): String {
        if (value == 0.0) return "0"
        
        // Remove trailing decimal points
        if (value % 1 == 0.0 && value < 1e9 && value > -1e9) {
            return value.toLong().toString()
        }

        // Scientific format for very big/small numbers to keep beautiful precision fit
        val df = if (abs(value) >= 1e11 || (abs(value) < 1e-4 && value != 0.0)) {
            DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
        } else {
            // Standard format with up to 10 decimals
            val symbols = DecimalFormatSymbols(Locale.US)
            DecimalFormat("0.#########", symbols)
        }
        return df.format(value)
    }
}
