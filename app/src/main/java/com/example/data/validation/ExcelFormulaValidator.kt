package com.example.data.validation

import com.example.data.model.ExcelDialect
import com.example.data.model.GeneratedFormulaResult
import com.example.data.model.RelatedFormulaSuggestion

sealed interface FormulaValidationResult {
    data object Valid : FormulaValidationResult
    data class Invalid(val reasons: List<String>) : FormulaValidationResult {
        fun summary(): String = reasons.distinct().joinToString(" ")
    }
}

class InvalidFormulaSyntaxException(message: String) : IllegalStateException(message)

object ExcelFormulaValidator {

    private val binaryOperators = setOf('*', '/', '^', '&', '=', '<', '>')
    private val allOperators = setOf('+', '-', '*', '/', '^', '&', '=', '<', '>')
    private val separators = setOf(',', ';')

    fun sanitizeFormula(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
                .trimStart()
                .let { body ->
                    val firstNewline = body.indexOf('\n')
                    if (firstNewline in 1..15 && body.substring(0, firstNewline).all { it.isLetterOrDigit() }) {
                        body.substring(firstNewline + 1)
                    } else {
                        body
                    }
                }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.removeSuffix("```")
            }
            cleaned = cleaned.trim()
        }
        if (cleaned.length >= 2 && cleaned.startsWith("`") && cleaned.endsWith("`")) {
            cleaned = cleaned.drop(1).dropLast(1).trim()
        }
        return cleaned
    }

    fun validateFormula(formula: String, dialect: ExcelDialect): FormulaValidationResult {
        val reasons = mutableListOf<String>()
        val trimmed = formula.trim()

        if (trimmed.isEmpty()) {
            return FormulaValidationResult.Invalid(listOf("Formula cannot be empty."))
        }
        if (trimmed.contains("```")) {
            reasons += "Formula must not contain markdown code fences."
        }
        if (trimmed.contains('\n') || trimmed.contains('\r')) {
            reasons += "Formula must be a single-line expression without line breaks."
        }
        if (!trimmed.startsWith("=")) {
            reasons += "Formula must start with '='."
            return FormulaValidationResult.Invalid(reasons)
        }

        val body = trimmed.drop(1).trim()
        if (body.isEmpty()) {
            reasons += "Formula has no expression after '='."
            return FormulaValidationResult.Invalid(reasons)
        }

        var inDoubleQuote = false
        var inSingleQuote = false
        var parenDepth = 0
        var braceDepth = 0
        var bracketDepth = 0
        var lastSignificantChar = '='
        var lastSignificantIndex = 0
        var sawWhitespaceSinceLastToken = false

        var i = 1
        while (i < trimmed.length) {
            val c = trimmed[i]

            if (inDoubleQuote) {
                if (c == '"') {
                    if (i + 1 < trimmed.length && trimmed[i + 1] == '"') {
                        i += 2
                        continue
                    } else {
                        inDoubleQuote = false
                        lastSignificantChar = '"'
                        lastSignificantIndex = i
                        sawWhitespaceSinceLastToken = false
                    }
                }
                i++
                continue
            }

            if (inSingleQuote) {
                if (c == '\'') {
                    if (i + 1 < trimmed.length && trimmed[i + 1] == '\'') {
                        i += 2
                        continue
                    } else {
                        inSingleQuote = false
                        lastSignificantChar = '\''
                        lastSignificantIndex = i
                        sawWhitespaceSinceLastToken = false
                    }
                }
                i++
                continue
            }

            if (c.isWhitespace()) {
                sawWhitespaceSinceLastToken = true
                i++
                continue
            }

            // Detect plain space-separated prose like "=Sum column A"
            if (sawWhitespaceSinceLastToken &&
                c.isLetter() &&
                lastSignificantChar.isLetter() &&
                lastSignificantIndex > 0
            ) {
                reasons += "Invalid syntax: unexpected space between identifiers."
            }

            when (c) {
                '"' -> {
                    inDoubleQuote = true
                    sawWhitespaceSinceLastToken = false
                    i++
                    continue
                }

                '\'' -> {
                    inSingleQuote = true
                    sawWhitespaceSinceLastToken = false
                    i++
                    continue
                }

                '(' -> {
                    parenDepth++
                }

                ')' -> {
                    if (parenDepth == 0) {
                        reasons += "Unexpected closing parenthesis ')'."
                    } else {
                        parenDepth--
                    }
                    when {
                        lastSignificantChar == '(' -> {
                            val beforeOpenParen = findPreviousNonWhitespaceChar(trimmed, lastSignificantIndex)
                            if (beforeOpenParen == null || !isFunctionIdentifierChar(beforeOpenParen)) {
                                reasons += "Empty parentheses '()' are only valid for zero-argument functions."
                            }
                        }
                        lastSignificantChar in separators -> {
                            reasons += "Trailing argument separator '$lastSignificantChar' before ')'."
                        }
                        lastSignificantChar in allOperators -> {
                            reasons += "Incomplete operator '$lastSignificantChar' before ')'."
                        }
                    }
                }

                '{' -> {
                    braceDepth++
                }

                '}' -> {
                    if (braceDepth == 0) {
                        reasons += "Unexpected closing brace '}'."
                    } else {
                        braceDepth--
                    }
                    if (lastSignificantChar == '{') {
                        reasons += "Empty array constant '{}' is not valid."
                    } else if (lastSignificantChar in separators || lastSignificantChar in allOperators) {
                        reasons += "Incomplete expression before '}'."
                    }
                }

                '[' -> {
                    bracketDepth++
                }

                ']' -> {
                    if (bracketDepth == 0) {
                        reasons += "Unexpected closing bracket ']'."
                    } else {
                        bracketDepth--
                    }
                }

                ',', ';' -> {
                    if (lastSignificantChar == '(' || lastSignificantChar == '{') {
                        reasons += "Unexpected separator '$c' at start of argument list."
                    } else if (lastSignificantChar in allOperators) {
                        reasons += "Incomplete operator '$lastSignificantChar' before '$c'."
                    }

                    if (braceDepth == 0 && bracketDepth == 0) {
                        if (dialect == ExcelDialect.ENGLISH && c == ';') {
                            reasons += "English Excel formulas must use ',' instead of ';' to separate arguments."
                        } else if (dialect == ExcelDialect.GREEK && c == ',') {
                            val prevRaw = trimmed.getOrNull(i - 1)
                            val nextRaw = trimmed.getOrNull(i + 1)
                            val isDecimalComma = prevRaw?.isDigit() == true && nextRaw?.isDigit() == true
                            if (!isDecimalComma) {
                                reasons += "Greek Excel formulas must use ';' instead of ',' to separate arguments."
                            }
                        }
                    }
                }

                '.' -> {
                    if (dialect == ExcelDialect.GREEK && braceDepth == 0 && bracketDepth == 0) {
                        val prevRaw = trimmed.getOrNull(i - 1)
                        val nextRaw = trimmed.getOrNull(i + 1)
                        if (prevRaw?.isDigit() == true && nextRaw?.isDigit() == true) {
                            reasons += "Greek Excel uses ',' for decimal numbers (e.g. 1,24), not '.'."
                        }
                    }
                }

                in binaryOperators -> {
                    val isRelationalPair = (lastSignificantChar == '<' && (c == '=' || c == '>')) ||
                        (lastSignificantChar == '>' && c == '=')
                    if (!isRelationalPair) {
                        if (lastSignificantChar in setOf('=', '(', '{', ',', ';') ||
                            lastSignificantChar in allOperators
                        ) {
                            reasons += "Unexpected operator '$c' after '$lastSignificantChar'."
                        }
                    }
                }

                '+' , '-' -> {
                    // Unary +/- is valid after '=', '(', '{', ',', ';', and binary operators (e.g. =-A1, =A1*-1)
                }
            }

            lastSignificantChar = c
            lastSignificantIndex = i
            sawWhitespaceSinceLastToken = false
            i++
        }

        if (inDoubleQuote) {
            reasons += "Unclosed string literal (missing closing '\"')."
        }
        if (inSingleQuote) {
            reasons += "Unclosed sheet quote (missing closing ''')."
        }
        if (parenDepth > 0) {
            reasons += "Unbalanced parentheses: missing $parenDepth closing ')'."
        }
        if (braceDepth > 0) {
            reasons += "Unbalanced braces: missing $braceDepth closing '}'."
        }
        if (bracketDepth > 0) {
            reasons += "Unbalanced brackets: missing $bracketDepth closing ']'."
        }
        if (lastSignificantChar in allOperators || lastSignificantChar in separators || lastSignificantChar == '(') {
            reasons += "Formula ends unexpectedly with '$lastSignificantChar'."
        }

        return if (reasons.isEmpty()) {
            FormulaValidationResult.Valid
        } else {
            FormulaValidationResult.Invalid(reasons.distinct())
        }
    }

    fun validateResult(result: GeneratedFormulaResult): FormulaValidationResult {
        val errors = mutableListOf<String>()

        when (val en = validateFormula(result.englishFormula, ExcelDialect.ENGLISH)) {
            FormulaValidationResult.Valid -> Unit
            is FormulaValidationResult.Invalid -> {
                errors += "English formula syntax error: ${en.summary()}"
            }
        }

        when (val el = validateFormula(result.greekFormula, ExcelDialect.GREEK)) {
            FormulaValidationResult.Valid -> Unit
            is FormulaValidationResult.Invalid -> {
                errors += "Greek formula syntax error: ${el.summary()}"
            }
        }

        return if (errors.isEmpty()) {
            FormulaValidationResult.Valid
        } else {
            FormulaValidationResult.Invalid(errors)
        }
    }

    fun filterValidSuggestions(
        suggestions: List<RelatedFormulaSuggestion>
    ): List<RelatedFormulaSuggestion> {
        return suggestions.filter { suggestion ->
            validateFormula(suggestion.englishFormula, ExcelDialect.ENGLISH) is FormulaValidationResult.Valid &&
                validateFormula(suggestion.greekFormula, ExcelDialect.GREEK) is FormulaValidationResult.Valid
        }
    }

    private fun findPreviousNonWhitespaceChar(text: String, beforeIndex: Int): Char? {
        var idx = beforeIndex - 1
        while (idx >= 0) {
            if (!text[idx].isWhitespace()) return text[idx]
            idx--
        }
        return null
    }

    private fun isFunctionIdentifierChar(c: Char): Boolean {
        return c.isLetterOrDigit() || c == '_' || c == '.'
    }
}
