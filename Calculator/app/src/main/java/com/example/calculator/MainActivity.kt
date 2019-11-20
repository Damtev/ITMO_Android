package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import org.mariuszgromada.math.mxparser.Expression
import java.text.DecimalFormat
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_RESULT = "com.example.calculator.result"
    }

    private var result = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        input.showSoftInputOnFocus = false
        setCommonListeners()
        setSpecialListeners()
        result = savedInstanceState?.getString(KEY_RESULT) ?: ""
        output.text = result
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_RESULT, result)
        super.onSaveInstanceState(outState)
    }

    private fun setCommonListeners() {
        button_0.setOnClickListener { addExpression(getString(R.string._0)) }
        button_1.setOnClickListener { addExpression(getString(R.string._1)) }
        button_2.setOnClickListener { addExpression(getString(R.string._2)) }
        button_3.setOnClickListener { addExpression(getString(R.string._3)) }
        button_4.setOnClickListener { addExpression(getString(R.string._4)) }
        button_5.setOnClickListener { addExpression(getString(R.string._5)) }
        button_6.setOnClickListener { addExpression(getString(R.string._6)) }
        button_7.setOnClickListener { addExpression(getString(R.string._7)) }
        button_8.setOnClickListener { addExpression(getString(R.string._8)) }
        button_9.setOnClickListener { addExpression(getString(R.string._9)) }
        button_dot.setOnClickListener { addExpression(getString(R.string.dot)) }

        button_left_bracket.setOnClickListener { addExpression(getString(R.string.left_bracket)) }
        button_right_bracket.setOnClickListener { addExpression(getString(R.string.right_bracket)) }

        button_add.setOnClickListener { addExpression(getString(R.string.add)) }
        button_mult.setOnClickListener { addExpression(getString(R.string.mult)) }
        button_div.setOnClickListener { addExpression(getString(R.string.div)) }
        button_mod.setOnClickListener { addExpression(getString(R.string.mod)) }
        button_pow.setOnClickListener { addExpression(getString(R.string.pow)) }
        button_sub.setOnClickListener { addExpression(getString(R.string.sub)) }
        button_sqrt.setOnClickListener { addExpression(getString(R.string.sqrt)) }
        button_sin.setOnClickListener { addExpression(getString(R.string.sin)) }
        button_cos.setOnClickListener { addExpression(getString(R.string.cos)) }
    }


    private fun setSpecialListeners() {
        button_clear.setOnClickListener {
            input.text.clear()
            calculate()
        }
        button_backspace.setOnClickListener {
            removeSelected()
        }
        button_calc.setOnClickListener {
            calculate()
            input.setText(result)
            input.selectAll()
        }
    }

    private fun removeSelected() {
        with(input) {
            var selectionStart = input.selectionStart
            if (selectionStart == selectionEnd && selectionStart != 0) {
                --selectionStart
            }
            text = input.editableText.delete(selectionStart, selectionEnd)
            setSelection(selectionStart)
        }

        calculate()
    }


    private fun addExpression(expr: String) {
        if (input.selectionStart != input.selectionEnd) {
            removeSelected()
        }
        var text = expr
        var isUnary = false
        if (text == "sqrt" || text == "sin" || text == "cos") {
            text += "()"
            isUnary = true
        }
        val selectionEnd = input.selectionEnd
        val editableText = input.editableText
        input.text = editableText.insert(selectionEnd, text)
        input.setSelection(selectionEnd + text.length + (if (isUnary) -1 else 0))
        calculate()
    }

    private fun calculate() {
        if (input.text.isEmpty()) {
            output.text = ""
            return
        }
        val calculated = Expression(input.text.toString()).calculate()
        if (!calculated.isNaN()) {
            var format = calculated.toString()
            if (abs(calculated) > 1e10) {
                format = DecimalFormat("0.00000000E0").format(calculated)
            }
            result = format.replace(",", ".")
            if (result.endsWith(".0")) {
                result = result.substringBefore(".")
            }
            output.text = result
        }
    }
}
