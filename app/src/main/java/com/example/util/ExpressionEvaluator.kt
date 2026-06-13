package com.example.util

import kotlin.math.*

object ExpressionEvaluator {
    fun evaluate(expression: String, isDegreeMode: Boolean = false): Double {
        if (expression.isBlank()) return 0.0
        
        // Normalize symbols for the parser
        var expr = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", Math.PI.toString())
            .replace("e", Math.E.toString())
        
        class Parser {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expr.length) expr[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expr.length) throw RuntimeException("Unexpected character: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // multiplication
                    else if (eat('/'.code)) {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor // division
                    }
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { // numbers
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    val numStr = expr.substring(startPos, pos)
                    x = numStr.toDoubleOrNull() ?: 0.0
                } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = expr.substring(startPos, pos)
                    x = parseFactor()
                    x = when (func) {
                        "sin" -> {
                            val angle = if (isDegreeMode) Math.toRadians(x) else x
                            sin(angle)
                        }
                        "cos" -> {
                            val angle = if (isDegreeMode) Math.toRadians(x) else x
                            cos(angle)
                        }
                        "tan" -> {
                            val angle = if (isDegreeMode) Math.toRadians(x) else x
                            tan(angle)
                        }
                        "log" -> log10(x)
                        "ln" -> ln(x)
                        "sqrt" -> {
                            if (x < 0.0) throw ArithmeticException("Square root of negative number")
                            sqrt(x)
                        }
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected character: " + ch.toChar())
                }

                if (eat('^'.code)) x = x.pow(parseFactor()) // exponentiation

                return x
            }
        }
        return Parser().parse()
    }
}
