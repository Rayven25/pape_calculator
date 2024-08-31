package com.example.papa_calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var inputDisplay: TextView
    private lateinit var resultDisplay: TextView

    private var currentExpression: String = ""
    private var lastResult: String = ""
    private var isResultDisplayed: Boolean = false
    private val maxDigits = 10 // Maximum number of digits to display in the result

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputDisplay = findViewById(R.id.inputDisplay)
        resultDisplay = findViewById(R.id.resultDisplay)

        setupButtons()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.buttonClear).setOnClickListener {
            clear()
            updateDisplay()
        }
        findViewById<Button>(R.id.buttonNegate).setOnClickListener {
            toggleSign()
            updateDisplayAndCalculate()
        }
        findViewById<Button>(R.id.buttonPercent).setOnClickListener {
            applyPercentage()
            updateDisplayAndCalculate()
        }
        findViewById<Button>(R.id.buttonDivide).setOnClickListener {
            setOperator("÷")
            updateDisplayAndCalculate()
        }
        findViewById<Button>(R.id.buttonMultiply).setOnClickListener {
            setOperator("×")
            updateDisplayAndCalculate()
        }
        findViewById<Button>(R.id.buttonSubtract).setOnClickListener {
            setOperator("-")
            updateDisplayAndCalculate()
        }
        findViewById<Button>(R.id.buttonAdd).setOnClickListener {
            setOperator("+")
            updateDisplayAndCalculate()
        }
        findViewById<Button>(R.id.buttonEquals).setOnClickListener {
            if (currentExpression.isNotEmpty() && !currentExpression.endsWith(" ")) {
                if (isResultDisplayed) {
                    // If result is already displayed, do not calculate again.
                    // The next number input will start a new calculation.
                    inputDisplay.text = ""
                } else {
                    // Calculate and show result if not displayed yet
                    val result = calculate()
                    if (result.isNotEmpty() && result != "Error") {
                        showResult(result)
                        currentExpression = result // Update currentExpression to the result for further calculations
                        isResultDisplayed = true
                    } else {
                        resultDisplay.text = ""
                    }
                }
            } else {
                // Do not display anything if the expression ends with an operator or is empty
                resultDisplay.text = ""
            }
        }

        // Number buttons
        val numberButtons = listOf(
            R.id.button0 to "0", R.id.button1 to "1", R.id.button2 to "2",
            R.id.button3 to "3", R.id.button4 to "4", R.id.button5 to "5",
            R.id.button6 to "6", R.id.button7 to "7", R.id.button8 to "8",
            R.id.button9 to "9", R.id.buttonDecimal to "."
        )
        for ((buttonId, number) in numberButtons) {
            findViewById<Button>(buttonId).setOnClickListener {
                if (isResultDisplayed) {
                    currentExpression = ""
                    isResultDisplayed = false
                }
                appendNumber(number)
                updateDisplayAndCalculate()
            }
        }

        findViewById<Button>(R.id.buttonDelete).setOnClickListener {
            if (isResultDisplayed) {
                // Clear expression if result is displayed
                currentExpression = ""
                resultDisplay.text = ""
                isResultDisplayed = false
            } else {
                deleteLast()
                updateDisplayAndCalculate()
            }
        }
    }

    private fun updateDisplay() {
        inputDisplay.text = currentExpression
        if (!isResultDisplayed) {
            resultDisplay.text = "" // Clear the result display only if not showing result
        }
    }

    private fun updateDisplayAndCalculate() {
        inputDisplay.text = currentExpression
        if (!isResultDisplayed) {
            resultDisplay.text = calculateTemporaryResult()
        }
    }

    private fun showResult(result: String) {
        if (result.isNotEmpty() && result != "Error") {
            inputDisplay.text = "" // Clear the input display
            resultDisplay.text = result // Show the result
        } else {
            resultDisplay.text = "" // Ensure the result display is empty if no valid result
        }
    }

    private fun clear() {
        currentExpression = ""
        lastResult = ""
        isResultDisplayed = false
    }

    private fun toggleSign() {
        if (currentExpression.isEmpty()) return

        val tokens = currentExpression.split(" ").toMutableList()

        if (tokens.isNotEmpty()) {
            val lastToken = tokens.last()

            if (lastToken.isNumber()) {
                tokens[tokens.size - 1] = if (lastToken.startsWith("-")) {
                    lastToken.substring(1)
                } else {
                    "-$lastToken"
                }
                currentExpression = tokens.joinToString(" ")
            }
        }
    }

    private fun applyPercentage() {
        if (currentExpression.isNotEmpty()) {
            val tokens = currentExpression.split(" ").toMutableList()
            val lastToken = tokens.last()

            if (lastToken.isNumber()) {
                val value = lastToken.toDoubleOrNull() ?: return
                tokens[tokens.size - 1] = (value / 100).toString()
                currentExpression = tokens.joinToString(" ")
            }
        }
    }

    private fun setOperator(op: String) {
        if (currentExpression.isEmpty()) {
            // If expression is empty, set the operator to the last result
            if (lastResult.isNotEmpty()) {
                currentExpression = "$lastResult $op "
            }
        } else {
            // Remove trailing operator if exists
            if (currentExpression.endsWith(" ")) {
                currentExpression = currentExpression.dropLast(3)
            }
            // Add operator if the last character is not an operator
            if (!currentExpression.endsWith(" ") && !currentExpression.isEmpty()) {
                currentExpression += " $op "
            }
        }
        isResultDisplayed = false
    }

    private fun appendNumber(number: String) {
        if (isResultDisplayed) {
            currentExpression = ""
            isResultDisplayed = false
        }
        if (currentExpression == "0" && number != ".") {
            currentExpression = number
        } else {
            currentExpression += number
        }
    }

    private fun deleteLast() {
        if (currentExpression.isNotEmpty()) {
            val lastChar = currentExpression.last()
            if (lastChar == ' ' && currentExpression.length > 2) {
                currentExpression = currentExpression.dropLast(3)
            } else if (lastChar == ' ') {
                currentExpression = currentExpression.dropLast(1)
            } else {
                currentExpression = currentExpression.dropLast(1)
            }
        }
    }

    private fun calculate(): String {
        if (!currentExpression.containsAnyOperator() && currentExpression.isNotEmpty()) {
            return "" // Return empty if there's no operator
        }

        return try {
            val expressionToEvaluate = processPercentages(currentExpression)
            val result = evaluateExpression(expressionToEvaluate)
            lastResult = formatResult(result)
            lastResult // Return the result without modifying currentExpression
        } catch (e: ArithmeticException) {
            resultDisplay.text = "Cannot be divided by 0" // Display error in the result box
            ""
        } catch (e: Exception) {
            "Error: Invalid operation"
        }
    }

    private fun calculateTemporaryResult(): String {
        if (currentExpression.isEmpty() || currentExpression.endsWith(" ") || !currentExpression.containsAnyOperator()) {
            return "" // Return empty if no valid operation
        }

        return try {
            val expressionToEvaluate = processPercentages(currentExpression)
            val result = evaluateExpression(expressionToEvaluate)
            formatResult(result)
        } catch (e: ArithmeticException) {
            "Cannot be divided by 0"
        } catch (e: Exception) {
            ""
        }
    }

    private fun evaluateExpression(expression: String): Double {
        val tokens = expression.split(" ").filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return 0.0

        var result = tokens[0].toDoubleOrNull() ?: 0.0
        var index = 1

        while (index < tokens.size) {
            val operator = tokens[index]
            val nextOperand = tokens.getOrNull(index + 1)?.toDoubleOrNull() ?: 0.0

            result = when (operator) {
                "+" -> result + nextOperand
                "-" -> result - nextOperand
                "×" -> result * nextOperand
                "÷" -> {
                    if (nextOperand == 0.0) {
                        throw ArithmeticException("Cannot be divided by 0")
                    } else {
                        result / nextOperand
                    }
                }
                else -> throw IllegalArgumentException("Invalid operator")
            }
            index += 2
        }

        return result
    }

    private fun formatResult(result: Double): String {
        val resultString: String = if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            result.toString()
        }

        return if (resultString.length > maxDigits) {
            // If the result exceeds the maximum digits, truncate or round it
            val roundedResult = "%.${maxDigits - resultString.indexOf('.') - 1}f".format(result)
            if (roundedResult.length > maxDigits) {
                // If rounding doesn't fit, switch to scientific notation
                "%.${maxDigits - 2}e".format(result)
            } else {
                roundedResult
            }
        } else {
            resultString
        }
    }

    private fun String.containsAnyOperator(): Boolean {
        return this.contains("×") || this.contains("÷") || this.contains("+") || this.contains("-")
    }

    private fun String.isNumber(): Boolean {
        return this.toDoubleOrNull() != null
    }

    private fun processPercentages(expression: String): String {
        val tokens = expression.split(" ").toMutableList()

        for (i in tokens.indices) {
            val token = tokens[i]
            if (token.endsWith("%")) {
                val numberPart = token.dropLast(1).toDoubleOrNull()
                if (numberPart != null) {
                    tokens[i] = (numberPart / 100).toString()
                }
            }
        }

        return tokens.joinToString(" ")
    }
}
