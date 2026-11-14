package com.example

import com.example.data.local.ExcelDictionaryData
import com.example.data.model.ExcelDialect
import com.example.data.model.GeneratedFormulaResult
import com.example.data.model.RelatedFormulaSuggestion
import com.example.data.validation.ExcelFormulaValidator
import com.example.data.validation.FormulaValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun allBuiltInDictionaryFormulasAreValid() {
        ExcelDictionaryData.entries.forEach { entry ->
            val enResult = ExcelFormulaValidator.validateFormula(entry.englishSyntax, ExcelDialect.ENGLISH)
            assertTrue(
                "Expected valid English syntax for ${entry.englishName}: ${entry.englishSyntax} -> $enResult",
                enResult is FormulaValidationResult.Valid
            )
            val elResult = ExcelFormulaValidator.validateFormula(entry.greekSyntax, ExcelDialect.GREEK)
            assertTrue(
                "Expected valid Greek syntax for ${entry.greekName}: ${entry.greekSyntax} -> $elResult",
                elResult is FormulaValidationResult.Valid
            )
        }
    }

    @Test
    fun validComplexEnglishAndGreekFormulasPass() {
        val validEnglish = listOf(
            "=TODAY()",
            "=SUMIFS(D2:D100, A2:A100, \"Sales; Q1\", B2:B100, \">100\")",
            "=IF(A2<>\"\", B2*10%, 0)",
            "=XLOOKUP(E2, A2:A100, C2:C100, , 0)",
            "=PMT(5%/12, 60, -10000)",
            "=SUM('Q1; Sales'!A2:A50)",
            "=LAMBDA(x, x*2)(5)",
            "=SUM({1,2;3,4})"
        )
        validEnglish.forEach { formula ->
            val res = ExcelFormulaValidator.validateFormula(formula, ExcelDialect.ENGLISH)
            assertTrue("Expected valid EN formula: $formula but got $res", res is FormulaValidationResult.Valid)
        }

        val validGreek = listOf(
            "=ΣΗΜΕΡΑ()",
            "=ΣΤΡΟΓΓΥΛΟΠΟΙΗΣΗ(A2*1,24; 2)",
            "=ΑΝ(ΚΑΙ(A2>0; B2=\"Πληρωμένο, ΟΚ\"); \"Ναι\"; \"Όχι\")",
            "=TEXTJOIN(\", \"; ΑΛΗΘΗΣ; A2:C2)",
            "=ΑΘΡΟΙΣΜΑ('Φύλλο 1, 2026'!A2:A50)",
            "=ΕΥΡΕΤΗΡΙΟ(C2:C100; ΤΑΙΡΙΑΣΜΑ(E2; A2:A100; 0))",
            "=ΚΑΤΑΒΟΛΗ(5%/12; 60; -10000)"
        )
        validGreek.forEach { formula ->
            val res = ExcelFormulaValidator.validateFormula(formula, ExcelDialect.GREEK)
            assertTrue("Expected valid EL formula: $formula but got $res", res is FormulaValidationResult.Valid)
        }
    }

    @Test
    fun invalidSyntaxIsRejected() {
        val invalidEnglish = listOf(
            "",
            "SUM(A1:A10)",
            "=",
            "=SUM(A1:A10",
            "=SUM(A1:A10))",
            "=IF(A1>0, \"Open, 1)",
            "=SUM(A1,)",
            "=SUM(,A1)",
            "=A1+",
            "=SUM(A1+)",
            "=()",
            "=SUM(A1; B1)",
            "```excel\n=SUM(A1:A10)\n```",
            "=Sum column A"
        )
        invalidEnglish.forEach { formula ->
            val res = ExcelFormulaValidator.validateFormula(formula, ExcelDialect.ENGLISH)
            assertTrue("Expected invalid EN formula: $formula", res is FormulaValidationResult.Invalid)
        }

        val invalidGreek = listOf(
            "=ΑΘΡΟΙΣΜΑ(A1, B1)",
            "=ΣΤΡΟΓΓΥΛΟΠΟΙΗΣΗ(A2*1.24; 2)",
            "=ΑΝ(A1>0; \"Ναι\";)",
            "=ΚΑΘΕΤΗ.ΑΝΑΖΗΤΗΣΗ(E2; A2:C100; 3; ΨΕΥΔΗΣ"
        )
        invalidGreek.forEach { formula ->
            val res = ExcelFormulaValidator.validateFormula(formula, ExcelDialect.GREEK)
            assertTrue("Expected invalid EL formula: $formula", res is FormulaValidationResult.Invalid)
        }
    }

    @Test
    fun validateResultAndFilterSuggestionsWork() {
        val validResult = GeneratedFormulaResult(
            userPrompt = "Add 24% VAT",
            category = "Finance",
            title = "Add VAT",
            englishFormula = "=ROUND(A2*1.24, 2)",
            greekFormula = "=ΣΤΡΟΓΓΥΛΟΠΟΙΗΣΗ(A2*1,24; 2)",
            explanation = "Multiplies by 1.24 and rounds to 2 decimals."
        )
        assertEquals(FormulaValidationResult.Valid, ExcelFormulaValidator.validateResult(validResult))

        val brokenGreekResult = validResult.copy(greekFormula = "=ΣΤΡΟΓΓΥΛΟΠΟΙΗΣΗ(A2*1,24, 2)")
        assertTrue(ExcelFormulaValidator.validateResult(brokenGreekResult) is FormulaValidationResult.Invalid)

        val suggestions = listOf(
            RelatedFormulaSuggestion(
                title = "Valid alt",
                reason = "Works",
                englishFormula = "=A2*1.24",
                greekFormula = "=A2*1,24"
            ),
            RelatedFormulaSuggestion(
                title = "Broken alt",
                reason = "Missing paren",
                englishFormula = "=ROUND(A2*1.24, 2",
                greekFormula = "=ΣΤΡΟΓΓΥΛΟΠΟΙΗΣΗ(A2*1,24; 2)"
            )
        )
        val filtered = ExcelFormulaValidator.filterValidSuggestions(suggestions)
        assertEquals(1, filtered.size)
        assertEquals("Valid alt", filtered.first().title)
    }

    @Test
    fun sanitizeFormulaStripsMarkdownFences() {
        val raw = "```excel\n=SUM(A1:A10)\n```"
        assertEquals("=SUM(A1:A10)", ExcelFormulaValidator.sanitizeFormula(raw))
    }
}
