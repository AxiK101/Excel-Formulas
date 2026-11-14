package com.example.data.ai

import com.example.data.model.ChartDataPoint
import com.example.data.model.ChartKind
import com.example.data.model.ExcelDialect
import com.example.data.model.ExcelPlatform
import com.example.data.model.FormulaChartSpec
import com.example.data.model.FormulaPart
import com.example.data.model.GeneratedFormulaResult
import com.example.data.model.RelatedFormulaSuggestion
import com.example.data.model.SampleRow
import com.example.data.validation.ExcelFormulaValidator
import com.example.data.validation.FormulaValidationResult
import com.example.data.validation.InvalidFormulaSyntaxException
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

class FormulaAiRepository {

    private val formulaSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Short 3-6 word title describing what the formula achieves."),
            "category" to Schema.enumeration(
                values = listOf(
                    "Data Analysis",
                    "Finance",
                    "String Manipulation",
                    "Lookup",
                    "Logical",
                    "Date & Time",
                    "Dynamic Array"
                ),
                description = "Primary spreadsheet formula category."
            ),
            "englishFormula" to Schema.string(
                description = "Complete, copy-ready English Excel formula starting with '=' using English function names, comma ',' argument separators, and dot '.' decimals."
            ),
            "greekFormula" to Schema.string(
                description = "Complete, copy-ready Greek Excel (Ελληνικό Excel) formula starting with '=' using official localized Greek Excel function names (e.g., ΑΘΡΟΙΣΜΑ, ΑΝ, ΚΑΘΕΤΗ.ΑΝΑΖΗΤΗΣΗ, ΜΕΣΟΣ.ΟΡΟΣ, ΜΕΤΡΗΣΗ.ΑΝ, ΑΝ.ΣΦΑΛΜΑ, ΕΥΡΕΤΗΡΙΟ, ΤΑΙΡΙΑΣΜΑ, ΣΗΜΕΡΑ, ΑΡΙΣΤΕΡΑ, ΦΙΛΤΡΟ), semicolon ';' argument separators, comma ',' decimals, and Greek booleans ΑΛΗΘΗΣ/ΨΕΥΔΗΣ."
            ),
            "purpose" to Schema.string(
                description = "1 concise sentence in simple, everyday words stating the formula's purpose (what it accomplishes)."
            ),
            "explanation" to Schema.string(
                description = "2-3 concise sentences in simple, beginner-friendly terms explaining how the formula functions step by step without technical jargon. Match the language of the user's request (Greek if prompt is in Greek or Greek Excel is active, otherwise English)."
            ),
            "usageExample" to Schema.string(
                description = "A simple, illustrative example describing how the formula works in a realistic spreadsheet scenario (referencing specific columns/cells and what result it produces). Match the user's language."
            ),
            "greekNotes" to Schema.string(
                description = "Helpful note for Greek Excel users (e.g., semicolon ';' separator reminder, localized function mapping, or date format code note like εεεε-μμ-ηη)."
            ),
            "parts" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "token" to Schema.string(description = "Function name or key argument token."),
                        "role" to Schema.string(description = "Short role label like 'Function', 'Range', 'Criteria', 'Fallback'."),
                        "description" to Schema.string(description = "Brief explanation of what this token does in simple terms.")
                    )
                ),
                description = "Breakdown of 2 to 5 key parts of the formula."
            ),
            "suggestions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "title" to Schema.string(description = "Short name of the related or alternative approach (e.g. 'Classic INDEX + MATCH', 'Sum Multiple Criteria with SUMIFS')."),
                        "reason" to Schema.string(description = "1-sentence explanation of what similar or additional functionality this alternative formula provides."),
                        "englishFormula" to Schema.string(description = "Copy-ready English Excel formula starting with '='."),
                        "greekFormula" to Schema.string(description = "Copy-ready Greek Excel formula starting with '=' and ';' separators.")
                    )
                ),
                description = "1 to 2 related or alternative Excel formulas that achieve similar results, work in older Excel versions, or add useful extra functionality."
            ),
            "sampleHeaders" to Schema.array(
                items = Schema.string(),
                description = "3 to 4 column headers matching the formula's columns, e.g. ['A: Item', 'B: Qty', 'C: Price']."
            ),
            "sampleRows" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "rowNumber" to Schema.integer(description = "Spreadsheet row number, e.g. 2, 3, 4."),
                        "cells" to Schema.array(
                            items = Schema.string(),
                            description = "Cell values corresponding to sampleHeaders."
                        )
                    )
                ),
                description = "2 to 3 realistic sample data rows."
            ),
            "sampleResultCell" to Schema.string(description = "Target output cell reference, e.g. 'D2'."),
            "sampleResultValue" to Schema.string(description = "Exact computed output value for the sample data."),
            "chartSpec" to Schema.obj(
                properties = mapOf(
                    "shouldVisualize" to Schema.boolean(
                        description = "True when the user's request involves numeric, statistical, financial, aggregation, comparison, or trend data that benefits from a chart; false for purely text/string formatting requests."
                    ),
                    "chartType" to Schema.enumeration(
                        values = listOf("BAR", "LINE"),
                        description = "Use BAR for category comparisons, conditional sums/counts, or rankings; use LINE for time-series or trend progressions."
                    ),
                    "title" to Schema.string(description = "Short chart title describing the sample data visualization."),
                    "yAxisLabel" to Schema.string(description = "Short unit or metric label for the values, e.g. 'Sales (€)', 'Count', 'Hours'."),
                    "points" to Schema.array(
                        items = Schema.obj(
                            properties = mapOf(
                                "label" to Schema.string(description = "Short X-axis label (1-12 chars), e.g. 'Athens', 'Jan', 'Row 2'."),
                                "value" to Schema.double(description = "Numeric value for the sample data point."),
                                "isHighlighted" to Schema.boolean(description = "True if this data point matches the formula criteria or represents the highlighted result.")
                            )
                        ),
                        description = "3 to 6 sample numeric data points consistent with sampleRows when shouldVisualize is true (empty array when false)."
                    )
                )
            )
        ),
        optionalProperties = listOf("greekNotes", "chartSpec")
    )

    private val model by lazy {
        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = formulaSchema
            temperature = 0.2f
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config,
            systemInstruction = content {
                text(
                    """
                    You are FormulaGrid, an expert Microsoft Excel and Google Sheets formula engineer fluent in both English Excel and Greek Excel (Ελληνικό Excel).
                    Your job is to convert any user description (typed in English, Greek, or mixed Greeklish) into accurate, copy-paste-ready spreadsheet formulas.

                    REQUIREMENTS FOR EVERY RESPONSE:
                    1. Provide BOTH a valid English Excel formula (comma ',' separator, dot '.' decimal) AND a valid Greek Excel formula (semicolon ';' separator, comma ',' decimal).
                    2. Provide a brief, simple-terms 'purpose' (1 sentence on what it solves) and 'explanation' (2-3 simple sentences on how the formula functions step by step—avoid technical jargon so anyone can understand it).
                    3. Provide a clear, illustrative spreadsheet scenario ('usageExample') plus a matching 2-3 row sample spreadsheet preview ('sampleHeaders', 'sampleRows', 'sampleResultCell', 'sampleResultValue').
                    4. Suggest 1 to 2 related or alternative formulas ('suggestions') that achieve similar results (e.g., classic compatibility, dynamic array alternative) or add helpful extra functionality (e.g., handling errors or multiple criteria).
                    5. Whenever the user's request involves numeric, statistical, financial, aggregation, comparison, or date/trend data, populate 'chartSpec' with shouldVisualize=true and 3-6 numeric data points aligned with your sample data (marking matched/target items with isHighlighted=true). For purely non-numeric text manipulation (like trimming text or extracting email domains), set shouldVisualize=false and points=[].

                    CRITICAL GREEK EXCEL RULES:
                    1. Greek Excel uses semicolon (;) as the argument separator, NEVER comma (,).
                    2. Greek Excel uses comma (,) as the decimal separator (e.g. 1,24 instead of 1.24).
                    3. Use official Microsoft Greek Excel localized function names:
                       SUM=ΑΘΡΟΙΣΜΑ, SUMIF=ΑΘΡΟΙΣΜΑ.ΑΝ, SUMIFS=ΑΘΡΟΙΣΜΑ.ΑΝ.ΣΥΝΟΛΑ,
                       AVERAGE=ΜΕΣΟΣ.ΟΡΟΣ, AVERAGEIF=ΜΕΣΟΣ.ΟΡΟΣ.ΑΝ, AVERAGEIFS=ΜΕΣΟΣ.ΟΡΟΣ.ΑΝ.ΣΥΝΟΛΑ,
                       COUNT=ΠΛΗΘΟΣ, COUNTA=ΠΛΗΘΟΣ.Α, COUNTBLANK=ΠΛΗΘΟΣ.ΚΕΝΩΝ, COUNTIF=ΜΕΤΡΗΣΗ.ΑΝ, COUNTIFS=ΜΕΤΡΗΣΗ.ΑΝ.ΣΥΝΟΛΑ,
                       IF=ΑΝ, IFS=ΑΝ.ΣΥΝΟΛΑ, IFERROR=ΑΝ.ΣΦΑΛΜΑ, IFNA=ΑΝ.ΔΥ, AND=ΚΑΙ, OR=Η, NOT=ΟΧΙ,
                       TRUE=ΑΛΗΘΗΣ, FALSE=ΨΕΥΔΗΣ,
                       VLOOKUP=ΚΑΘΕΤΗ.ΑΝΑΖΗΤΗΣΗ, HLOOKUP=ΟΡΙΖΟΝΤΙΑ.ΑΝΑΖΗΤΗΣΗ, INDEX=ΕΥΡΕΤΗΡΙΟ, MATCH=ΤΑΙΡΙΑΣΜΑ,
                       FILTER=ΦΙΛΤΡΟ, SORT=ΤΑΞΙΝΟΜΗΣΗ, UNIQUE=ΜΟΝΑΔΙΚΑ, SEQUENCE=ΑΚΟΛΟΥΘΙΑ,
                       LEFT=ΑΡΙΣΤΕΡΑ, RIGHT=ΔΕΞΙΑ, MID=ΜΕΣΑ, LEN=ΜΗΚΟΣ, TRIM=ΠΕΡΙΚΟΠΗ, UPPER=ΚΕΦΑΛΑΙΑ, LOWER=ΠΕΖΑ, PROPER=ΠΡΩΤΟ.ΚΕΦΑΛΑΙΟ,
                       FIND=ΕΥΡΕΣΗ, SEARCH=ΑΝΑΖΗΤΗΣΗ, SUBSTITUTE=ΑΝΤΙΚΑΤΑΣΤΑΣΗ, REPLACE=ΑΛΛΑΓΗ, TEXT=ΚΕΙΜΕΝΟ, CONCAT=ΣΥΝΕΝΩΣΗ,
                       DATE=ΗΜΕΡΟΜΗΝΙΑ, TODAY=ΣΗΜΕΡΑ, NOW=ΤΩΡΑ, YEAR=ΕΤΟΣ, MONTH=ΜΗΝΑΣ, DAY=ΗΜΕΡΑ, EOMONTH=ΤΕΛΟΣ.ΜΗΝΑ, NETWORKDAYS=ΕΡΓΑΣΙΜΕΣ.ΗΜΕΡΕΣ,
                       ROUND=ΣΤΡΟΓΓΥΛΟΠΟΙΗΣΗ, ROUNDUP=ΣΤΡΟΓΓΥΛΟΠΟΙΗΣΗ.ΠΑΝΩ, ROUNDDOWN=ΣΤΡΟΓΓΥΛΟΠΟΙΗΣΗ.ΚΑΤΩ,
                       MAX=ΜΕΓΙΣΤΟ, MIN=ΕΛΑΧΙΣΤΟ, LARGE=ΜΕΓΑΛΟ, SMALL=ΜΙΚΡΟ, RANK=ΚΑΤΑΤΑΞΗ,
                       PMT=ΚΑΤΑΒΟΛΗ, NPV=ΚΑΘΑΡΗ.ΠΑΡΟΥΣΑ.ΑΞΙΑ, IRR=ΕΣΩΤ.ΒΑΘΜΟΣ.ΑΠΟΔΟΣΗΣ.
                       Note: Newer Excel 365 functions like XLOOKUP, TEXTSPLIT, TEXTJOIN, LET, LAMBDA keep their English names in Greek Excel 365, but STILL require semicolon (;) separators!
                    4. Never invent fake Excel functions. Always start formulas with '='.
                    """.trimIndent()
                )
            }
        )
    }

    suspend fun generateFormula(
        userPrompt: String,
        sheetContext: String,
        preferredDialect: ExcelDialect,
        platform: ExcelPlatform
    ): GeneratedFormulaResult {
        val fullPrompt = buildString {
            appendLine("Target Platform: ${platform.label} (${platform.subtitle})")
            appendLine("Primary User Dialect: ${preferredDialect.displayName}")
            if (sheetContext.isNotBlank()) {
                appendLine("Sheet / Column Layout Context: $sheetContext")
            }
            appendLine("User Request: $userPrompt")
        }

        val response = withTimeout(60_000L) {
            model.generateContent(fullPrompt)
        }
        val rawJson = response.text ?: throw IllegalStateException("AI returned an empty response.")
        val parsed = parseFormulaJson(
            rawJson = rawJson,
            userPrompt = userPrompt,
            sheetContext = sheetContext,
            platform = platform
        )
        return validateOrRepair(parsed, userPrompt, sheetContext, platform)
    }

    suspend fun refineFormula(
        previous: GeneratedFormulaResult,
        refinementInstruction: String,
        preferredDialect: ExcelDialect
    ): GeneratedFormulaResult {
        val updatedPrompt = "${previous.userPrompt} • $refinementInstruction"
        val fullPrompt = buildString {
            appendLine("Target Platform: ${previous.platform.label}")
            appendLine("Primary User Dialect: ${preferredDialect.displayName}")
            if (previous.sheetContext.isNotBlank()) {
                appendLine("Sheet / Column Layout Context: ${previous.sheetContext}")
            }
            appendLine("Original Request: ${previous.userPrompt}")
            appendLine("Current English Formula: ${previous.englishFormula}")
            appendLine("Current Greek Formula: ${previous.greekFormula}")
            appendLine("Refinement Request: $refinementInstruction")
            appendLine("Return the updated complete formula result in both English and Greek Excel syntax, including simple explanation, usage example, 1-2 related/alternative formulas, and chartSpec if data-related.")
        }

        val response = withTimeout(60_000L) {
            model.generateContent(fullPrompt)
        }
        val rawJson = response.text ?: throw IllegalStateException("AI returned an empty response.")
        val parsed = parseFormulaJson(
            rawJson = rawJson,
            userPrompt = updatedPrompt,
            sheetContext = previous.sheetContext,
            platform = previous.platform
        )
        return validateOrRepair(parsed, updatedPrompt, previous.sheetContext, previous.platform)
    }

    private suspend fun validateOrRepair(
        initial: GeneratedFormulaResult,
        userPrompt: String,
        sheetContext: String,
        platform: ExcelPlatform
    ): GeneratedFormulaResult {
        val initialCheck = ExcelFormulaValidator.validateResult(initial)
        if (initialCheck is FormulaValidationResult.Valid) {
            return initial
        }

        val invalidResult = initialCheck as FormulaValidationResult.Invalid
        val repairPrompt = buildString {
            appendLine("The previously generated Excel formulas failed syntax validation.")
            appendLine("Validation errors: ${invalidResult.summary()}")
            appendLine("Offending English formula: ${initial.englishFormula}")
            appendLine("Offending Greek formula: ${initial.greekFormula}")
            appendLine("Target Platform: ${platform.label}")
            if (sheetContext.isNotBlank()) {
                appendLine("Sheet / Column Layout Context: $sheetContext")
            }
            appendLine("User Request: $userPrompt")
            appendLine("Fix ALL syntax errors (balanced parentheses/quotes, valid starting '=', ',' for English arguments and '.' for English decimals, ';' for Greek arguments and ',' for Greek decimals) and return the complete corrected JSON response.")
        }

        val repairResponse = withTimeout(60_000L) {
            model.generateContent(repairPrompt)
        }
        val repairedJson = repairResponse.text
            ?: throw InvalidFormulaSyntaxException("Generated formula failed syntax verification: ${invalidResult.summary()}")
        val repaired = parseFormulaJson(
            rawJson = repairedJson,
            userPrompt = userPrompt,
            sheetContext = sheetContext,
            platform = platform
        )

        return when (val repairedCheck = ExcelFormulaValidator.validateResult(repaired)) {
            FormulaValidationResult.Valid -> repaired
            is FormulaValidationResult.Invalid -> {
                throw InvalidFormulaSyntaxException(
                    "Generated formula failed syntax validation: ${repairedCheck.summary()}"
                )
            }
        }
    }

    private fun parseFormulaJson(
        rawJson: String,
        userPrompt: String,
        sheetContext: String,
        platform: ExcelPlatform
    ): GeneratedFormulaResult {
        val json = JSONObject(rawJson)
        val partsArray = json.optJSONArray("parts")
        val parts = if (partsArray != null) {
            (0 until partsArray.length()).map { idx ->
                val obj = partsArray.getJSONObject(idx)
                FormulaPart(
                    token = obj.optString("token"),
                    role = obj.optString("role"),
                    description = obj.optString("description")
                )
            }
        } else {
            emptyList()
        }

        val suggestionsArray = json.optJSONArray("suggestions")
        val rawSuggestions = if (suggestionsArray != null) {
            (0 until suggestionsArray.length()).map { idx ->
                val obj = suggestionsArray.getJSONObject(idx)
                RelatedFormulaSuggestion(
                    title = obj.optString("title"),
                    reason = obj.optString("reason"),
                    englishFormula = ensureEqualsPrefix(obj.optString("englishFormula")),
                    greekFormula = ensureEqualsPrefix(obj.optString("greekFormula"))
                )
            }
        } else {
            emptyList()
        }
        val suggestions = ExcelFormulaValidator.filterValidSuggestions(rawSuggestions)

        val headersArray = json.optJSONArray("sampleHeaders")
        val headers = if (headersArray != null) {
            (0 until headersArray.length()).map { headersArray.getString(it) }
        } else {
            emptyList()
        }

        val rowsArray = json.optJSONArray("sampleRows")
        val rows = if (rowsArray != null) {
            (0 until rowsArray.length()).map { idx ->
                val rowObj = rowsArray.getJSONObject(idx)
                val cellsArray = rowObj.optJSONArray("cells")
                val cells = if (cellsArray != null) {
                    (0 until cellsArray.length()).map { cellsArray.getString(it) }
                } else {
                    emptyList()
                }
                SampleRow(
                    rowNumber = rowObj.optInt("rowNumber", idx + 2),
                    cells = cells
                )
            }
        } else {
            emptyList()
        }

        val chartObj = json.optJSONObject("chartSpec")
        val chartSpec = if (chartObj != null) {
            val shouldVisualize = chartObj.optBoolean("shouldVisualize", false)
            val ptsArray = chartObj.optJSONArray("points")
            val pts = if (ptsArray != null) {
                (0 until ptsArray.length()).map { idx ->
                    val pt = ptsArray.getJSONObject(idx)
                    ChartDataPoint(
                        label = pt.optString("label"),
                        value = pt.optDouble("value", 0.0),
                        isHighlighted = pt.optBoolean("isHighlighted", false)
                    )
                }
            } else {
                emptyList()
            }
            val kind = ChartKind.entries.firstOrNull {
                it.name == chartObj.optString("chartType")
            } ?: ChartKind.BAR
            FormulaChartSpec(
                shouldVisualize = shouldVisualize && pts.size >= 2,
                chartType = kind,
                title = chartObj.optString("title", "Sample Data Overview"),
                yAxisLabel = chartObj.optString("yAxisLabel", ""),
                points = pts
            )
        } else {
            null
        }

        return GeneratedFormulaResult(
            userPrompt = userPrompt,
            sheetContext = sheetContext,
            platform = platform,
            category = json.optString("category", "Data Analysis"),
            title = json.optString("title", "Custom Formula"),
            englishFormula = ensureEqualsPrefix(json.optString("englishFormula")),
            greekFormula = ensureEqualsPrefix(json.optString("greekFormula")),
            purpose = json.optString("purpose", ""),
            explanation = json.optString("explanation"),
            usageExample = json.optString("usageExample", ""),
            greekNotes = json.optString("greekNotes", ""),
            parts = parts,
            suggestions = suggestions,
            sampleHeaders = headers,
            sampleRows = rows,
            sampleResultCell = json.optString("sampleResultCell", "Result"),
            sampleResultValue = json.optString("sampleResultValue", ""),
            chartSpec = chartSpec
        )
    }

    private fun ensureEqualsPrefix(formula: String): String {
        val cleaned = ExcelFormulaValidator.sanitizeFormula(formula)
        return if (cleaned.isEmpty() || cleaned.startsWith("=")) cleaned else "=$cleaned"
    }
}
