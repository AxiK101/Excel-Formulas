package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.FormulaDatabase
import com.example.data.local.FormulaHistoryRepository
import com.example.data.model.ChartDataPoint
import com.example.data.model.ChartKind
import com.example.data.model.ExcelPlatform
import com.example.data.model.FormulaChartSpec
import com.example.data.model.FormulaPart
import com.example.data.model.GeneratedFormulaResult
import com.example.data.model.RelatedFormulaSuggestion
import com.example.data.model.SampleRow
import com.example.data.validation.InvalidFormulaSyntaxException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: FormulaDatabase
    private lateinit var repository: FormulaHistoryRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FormulaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FormulaHistoryRepository(database.formulaDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FormulaGrid", appName)
    }

    @Test
    fun `room history repository saves retrieves favorites and deletes formulas`() = runTest {
        val sample = GeneratedFormulaResult(
            userPrompt = "Sum column C where column A is Athens",
            sheetContext = "A: City, C: Sales",
            platform = ExcelPlatform.EXCEL_365,
            category = "Data Analysis",
            title = "Conditional Sum by City",
            englishFormula = "=SUMIF(A2:A50, \"Athens\", C2:C50)",
            greekFormula = "=ΑΘΡΟΙΣΜΑ.ΑΝ(A2:A50; \"Αθήνα\"; C2:C50)",
            purpose = "Adds up sales only for rows matching Athens.",
            explanation = "SUMIF checks each cell in A2:A50 for Athens and totals the matching values in C2:C50.",
            usageExample = "If A2 is Athens and C2 is 120, that 120 is included in the total.",
            greekNotes = "Use semicolon ';' in Greek Excel.",
            parts = listOf(FormulaPart("SUMIF", "Function", "Adds numbers matching one condition.")),
            suggestions = listOf(
                RelatedFormulaSuggestion(
                    title = "Multiple Conditions (SUMIFS)",
                    reason = "Add extra filters like date range.",
                    englishFormula = "=SUMIFS(C2:C50, A2:A50, \"Athens\")",
                    greekFormula = "=ΑΘΡΟΙΣΜΑ.ΑΝ.ΣΥΝΟΛΑ(C2:C50; A2:A50; \"Αθήνα\")"
                )
            ),
            sampleHeaders = listOf("A: City", "C: Sales"),
            sampleRows = listOf(SampleRow(2, listOf("Athens", "120"))),
            sampleResultCell = "D2",
            sampleResultValue = "120",
            chartSpec = FormulaChartSpec(
                shouldVisualize = true,
                chartType = ChartKind.BAR,
                title = "Sales by City",
                yAxisLabel = "EUR",
                points = listOf(
                    ChartDataPoint("Athens", 120.0, true),
                    ChartDataPoint("Patras", 80.0, false)
                )
            )
        )

        val id = repository.saveFormula(sample)
        assertTrue(id > 0L)

        val savedList = repository.allHistory.first()
        assertEquals(1, savedList.size)
        assertEquals("Data Analysis", savedList.first().category)
        assertEquals("=ΑΘΡΟΙΣΜΑ.ΑΝ(A2:A50; \"Αθήνα\"; C2:C50)", savedList.first().greekFormula)
        assertEquals(1, savedList.first().suggestions.size)
        assertEquals(2, savedList.first().chartSpec?.points?.size)

        val dataAnalysisItems = repository.observeByCategory("Data Analysis").first()
        assertEquals(1, dataAnalysisItems.size)
        val financeItems = repository.observeByCategory("Finance").first()
        assertTrue(financeItems.isEmpty())

        repository.setFavorite(id, true)
        val favorited = repository.allHistory.first()
        assertTrue(favorited.first().isFavorite)

        repository.deleteById(id)
        val afterDelete = repository.allHistory.first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun `room history repository rejects invalid formula syntax`() = runTest {
        val invalid = GeneratedFormulaResult(
            userPrompt = "Broken formula",
            category = "Data Analysis",
            title = "Broken Sum",
            englishFormula = "=SUM(A1:A10",
            greekFormula = "=ΑΘΡΟΙΣΜΑ(A1:A10)",
            explanation = "Missing closing parenthesis."
        )

        var threwInvalidException = false
        try {
            repository.saveFormula(invalid)
        } catch (_: InvalidFormulaSyntaxException) {
            threwInvalidException = true
        }

        assertTrue(threwInvalidException)
        assertTrue(repository.allHistory.first().isEmpty())
    }
}
