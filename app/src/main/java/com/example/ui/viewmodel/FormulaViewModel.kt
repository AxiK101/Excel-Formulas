package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.FormulaAiRepository
import com.example.data.local.ExcelDictionaryData
import com.example.data.local.ExcelFunctionEntry
import com.example.data.local.FormulaDatabase
import com.example.data.local.FormulaHistoryRepository
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.AppThemeMode
import com.example.data.model.ExcelDialect
import com.example.data.model.ExcelPlatform
import com.example.data.model.GeneratedFormulaResult
import com.example.data.model.QuickPromptSuggestion
import com.example.data.validation.ExcelFormulaValidator
import com.example.data.validation.FormulaValidationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "FormulaViewModel"

sealed interface FormulaUiState {
    data object Idle : FormulaUiState
    data object Loading : FormulaUiState
    data class Success(val result: GeneratedFormulaResult) : FormulaUiState
    data class Error(val message: String) : FormulaUiState
}

enum class LibraryFilter(val label: String) {
    ALL("All"),
    FAVORITES("Favorites")
}

class FormulaViewModel(application: Application) : AndroidViewModel(application) {

    private val aiRepository = FormulaAiRepository()
    private val historyRepository = FormulaHistoryRepository(
        FormulaDatabase.getInstance(application).formulaDao()
    )
    private val preferencesRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow<FormulaUiState>(FormulaUiState.Idle)
    val uiState: StateFlow<FormulaUiState> = _uiState.asStateFlow()

    private val _promptText = MutableStateFlow("")
    val promptText: StateFlow<String> = _promptText.asStateFlow()

    private val _sheetContextText = MutableStateFlow("")
    val sheetContextText: StateFlow<String> = _sheetContextText.asStateFlow()

    private val _preferredDialect = MutableStateFlow(ExcelDialect.ENGLISH)
    val preferredDialect: StateFlow<ExcelDialect> = _preferredDialect.asStateFlow()

    private val _activeResultDialect = MutableStateFlow(ExcelDialect.ENGLISH)
    val activeResultDialect: StateFlow<ExcelDialect> = _activeResultDialect.asStateFlow()

    private val _preferredPlatform = MutableStateFlow(ExcelPlatform.EXCEL_365)
    val preferredPlatform: StateFlow<ExcelPlatform> = _preferredPlatform.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _libraryFilter = MutableStateFlow(LibraryFilter.ALL)
    val libraryFilter: StateFlow<LibraryFilter> = _libraryFilter.asStateFlow()

    private val _selectedHistoryCategory = MutableStateFlow("All Categories")
    val selectedHistoryCategory: StateFlow<String> = _selectedHistoryCategory.asStateFlow()

    private val _librarySearchQuery = MutableStateFlow("")
    val librarySearchQuery: StateFlow<String> = _librarySearchQuery.asStateFlow()

    private val _selectedHistoryFunction = MutableStateFlow<String?>(null)
    val selectedHistoryFunction: StateFlow<String?> = _selectedHistoryFunction.asStateFlow()

    val historyCategories: List<String> = listOf(
        "All Categories",
        "Data Analysis",
        "Finance",
        "String Manipulation",
        "Lookup",
        "Logical",
        "Date & Time",
        "Dynamic Array"
    )

    private val _dictionaryQuery = MutableStateFlow("")
    val dictionaryQuery: StateFlow<String> = _dictionaryQuery.asStateFlow()

    private val _dictionaryCategory = MutableStateFlow("All")
    val dictionaryCategory: StateFlow<String> = _dictionaryCategory.asStateFlow()

    val quickSuggestions: List<QuickPromptSuggestion> = listOf(
        QuickPromptSuggestion(
            label = "Conditional Sum",
            prompt = "Sum column C only where column A is 'Athens' and column B is greater than 100"
        ),
        QuickPromptSuggestion(
            label = "Άθροισμα με συνθήκη",
            prompt = "Άθροισε τη στήλη C όπου η στήλη A είναι 'Αθήνα' και η ημερομηνία στη B είναι το 2026",
            isGreek = true
        ),
        QuickPromptSuggestion(
            label = "Lookup + Fallback",
            prompt = "Look up product code in E2 inside A2:C100 and return price in column C, showing 'Not Found' if missing"
        ),
        QuickPromptSuggestion(
            label = "Μέρες καθυστέρησης",
            prompt = "Υπολόγισε πόσες εργάσιμες ημέρες έχουν περάσει από την ημερομηνία στη στήλη A μέχρι σήμερα",
            isGreek = true
        ),
        QuickPromptSuggestion(
            label = "Extract Email Domain",
            prompt = "Extract the domain name after @ from an email address in A2"
        ),
        QuickPromptSuggestion(
            label = "Υπολογισμός ΦΠΑ 24%",
            prompt = "Πρόσθεσε ΦΠΑ 24% στην τιμή του κελιού A2 και στρογγυλοποίησε στα 2 δεκαδικά ψηφία",
            isGreek = true
        )
    )

    val allHistory: StateFlow<List<GeneratedFormulaResult>> = historyRepository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val functionNameRegex = Regex("""([A-ZΑ-Ω][A-ZΑ-Ω0-9_.]+)\s*\(""")

    val availableHistoryFunctions: StateFlow<List<String>> = allHistory
        .map { items ->
            items.flatMap { item ->
                val enFuncs = functionNameRegex.findAll(item.englishFormula.uppercase())
                    .map { it.groupValues[1] }
                val elFuncs = functionNameRegex.findAll(item.greekFormula.uppercase())
                    .map { it.groupValues[1] }
                (enFuncs + elFuncs).toList()
            }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .map { it.key }
                .take(12)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val categoryScopedHistory = _selectedHistoryCategory.flatMapLatest { category ->
        if (category == "All Categories") {
            historyRepository.allHistory
        } else {
            historyRepository.observeByCategory(category)
        }
    }

    val filteredHistory: StateFlow<List<GeneratedFormulaResult>> = combine(
        categoryScopedHistory,
        _libraryFilter,
        _librarySearchQuery,
        _selectedHistoryFunction
    ) { items, filter, query, selectedFn ->
        val byFilter = when (filter) {
            LibraryFilter.ALL -> items
            LibraryFilter.FAVORITES -> items.filter { it.isFavorite }
        }
        val byFunction = if (selectedFn.isNullOrBlank()) {
            byFilter
        } else {
            val targetFn = "${selectedFn.uppercase()}("
            byFilter.filter { item ->
                item.englishFormula.uppercase().contains(targetFn) ||
                    item.greekFormula.uppercase().contains(targetFn) ||
                    item.parts.any { it.token.equals(selectedFn, ignoreCase = true) }
            }
        }
        if (query.isBlank()) {
            byFunction
        } else {
            val tokens = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
            byFunction.filter { item ->
                val searchableCorpus = buildString {
                    append(item.title).append(' ')
                    append(item.userPrompt).append(' ')
                    append(item.sheetContext).append(' ')
                    append(item.category).append(' ')
                    append(item.purpose).append(' ')
                    append(item.explanation).append(' ')
                    append(item.englishFormula).append(' ')
                    append(item.greekFormula).append(' ')
                    item.parts.forEach { append(it.token).append(' ').append(it.role).append(' ') }
                    item.suggestions.forEach {
                        append(it.title).append(' ')
                        append(it.englishFormula).append(' ')
                        append(it.greekFormula).append(' ')
                    }
                }.lowercase()
                tokens.all { token -> searchableCorpus.contains(token) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filteredDictionary: StateFlow<List<ExcelFunctionEntry>> = combine(
        _dictionaryQuery,
        _dictionaryCategory
    ) { query, category ->
        val byCategory = if (category == "All") {
            ExcelDictionaryData.entries
        } else {
            ExcelDictionaryData.entries.filter { it.category == category }
        }
        if (query.isBlank()) {
            byCategory
        } else {
            val q = query.trim().lowercase()
            byCategory.filter { entry ->
                entry.englishName.lowercase().contains(q) ||
                    entry.greekName.lowercase().contains(q) ||
                    entry.descriptionEn.lowercase().contains(q) ||
                    entry.descriptionEl.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExcelDictionaryData.entries)

    init {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _preferredDialect.value = prefs.preferredDialect
                _activeResultDialect.value = prefs.preferredDialect
                _preferredPlatform.value = prefs.preferredPlatform
                _themeMode.value = prefs.themeMode
            }
        }
    }

    fun updatePromptText(value: String) {
        _promptText.value = value
    }

    fun updateSheetContextText(value: String) {
        _sheetContextText.value = value
    }

    fun setPreferredDialect(dialect: ExcelDialect) {
        _preferredDialect.value = dialect
        _activeResultDialect.value = dialect
        viewModelScope.launch {
            preferencesRepository.setDialect(dialect)
        }
    }

    fun setActiveResultDialect(dialect: ExcelDialect) {
        _activeResultDialect.value = dialect
    }

    fun setPreferredPlatform(platform: ExcelPlatform) {
        _preferredPlatform.value = platform
        viewModelScope.launch {
            preferencesRepository.setPlatform(platform)
        }
    }

    fun toggleLightDarkTheme(currentlyDark: Boolean) {
        val nextMode = if (currentlyDark) AppThemeMode.LIGHT else AppThemeMode.DARK
        _themeMode.value = nextMode
        viewModelScope.launch {
            preferencesRepository.setThemeMode(nextMode)
        }
    }

    fun setLibraryFilter(filter: LibraryFilter) {
        _libraryFilter.value = filter
    }

    fun setHistoryCategoryFilter(category: String) {
        _selectedHistoryCategory.value = category
    }

    fun updateLibrarySearchQuery(query: String) {
        _librarySearchQuery.value = query
    }

    fun toggleHistoryFunctionFilter(functionName: String) {
        _selectedHistoryFunction.value = if (_selectedHistoryFunction.value == functionName) {
            null
        } else {
            functionName
        }
    }

    fun clearHistorySearchAndFilters() {
        _librarySearchQuery.value = ""
        _selectedHistoryFunction.value = null
        _selectedHistoryCategory.value = "All Categories"
        _libraryFilter.value = LibraryFilter.ALL
    }

    fun updateDictionaryQuery(query: String) {
        _dictionaryQuery.value = query
    }

    fun setDictionaryCategory(category: String) {
        _dictionaryCategory.value = category
    }

    fun applyQuickSuggestion(suggestion: QuickPromptSuggestion) {
        _promptText.value = suggestion.prompt
        if (suggestion.isGreek) {
            setPreferredDialect(ExcelDialect.GREEK)
        }
        generateFormula()
    }

    fun prefillPrompt(prompt: String, switchToGreek: Boolean = false) {
        _promptText.value = prompt
        if (switchToGreek) {
            setPreferredDialect(ExcelDialect.GREEK)
        }
    }

    fun openSavedResult(result: GeneratedFormulaResult) {
        _promptText.value = result.userPrompt
        _sheetContextText.value = result.sheetContext
        _uiState.value = FormulaUiState.Success(result)
    }

    fun generateFormula() {
        val prompt = _promptText.value.trim()
        if (prompt.isEmpty()) {
            _uiState.value = FormulaUiState.Error("Please describe what you want the Excel formula to do.")
            return
        }

        // Auto-detect Greek characters in user's prompt so Greek requests default to Greek Excel output
        val containsGreek = prompt.any { it in '\u0370'..'\u03FF' || it in '\u1F00'..'\u1FFF' }
        val effectiveDialect = if (containsGreek) ExcelDialect.GREEK else _preferredDialect.value
        _activeResultDialect.value = effectiveDialect

        viewModelScope.launch {
            _uiState.value = FormulaUiState.Loading
            try {
                val generated = aiRepository.generateFormula(
                    userPrompt = prompt,
                    sheetContext = _sheetContextText.value.trim(),
                    preferredDialect = effectiveDialect,
                    platform = _preferredPlatform.value
                )
                when (val validation = ExcelFormulaValidator.validateResult(generated)) {
                    is FormulaValidationResult.Invalid -> {
                        Log.e(TAG, "Formula validation failed before save/display: ${validation.summary()}")
                        _uiState.value = FormulaUiState.Error(
                            "Formula syntax check failed: ${validation.summary()}"
                        )
                    }
                    FormulaValidationResult.Valid -> {
                        val insertedId = historyRepository.saveFormula(generated)
                        _uiState.value = FormulaUiState.Success(generated.copy(id = insertedId))
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Formula generation timed out", e)
                _uiState.value = FormulaUiState.Error(
                    "Formula generation timed out after 60 seconds. Please try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate Excel formula", e)
                _uiState.value = FormulaUiState.Error(
                    e.localizedMessage ?: "Failed to generate formula. Please check your connection and try again."
                )
            }
        }
    }

    fun refineCurrentFormula(instruction: String) {
        val currentState = _uiState.value
        if (currentState !is FormulaUiState.Success) return
        val previous = currentState.result

        viewModelScope.launch {
            _uiState.value = FormulaUiState.Loading
            try {
                val refined = aiRepository.refineFormula(
                    previous = previous,
                    refinementInstruction = instruction,
                    preferredDialect = _activeResultDialect.value
                )
                when (val validation = ExcelFormulaValidator.validateResult(refined)) {
                    is FormulaValidationResult.Invalid -> {
                        Log.e(TAG, "Refined formula validation failed before save/display: ${validation.summary()}")
                        _uiState.value = FormulaUiState.Error(
                            "Refined formula syntax check failed: ${validation.summary()}"
                        )
                    }
                    FormulaValidationResult.Valid -> {
                        val insertedId = historyRepository.saveFormula(refined)
                        _uiState.value = FormulaUiState.Success(refined.copy(id = insertedId))
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Formula refinement timed out", e)
                _uiState.value = FormulaUiState.Error("Refinement timed out. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refine formula", e)
                _uiState.value = FormulaUiState.Error(
                    e.localizedMessage ?: "Could not refine formula."
                )
            }
        }
    }

    fun toggleFavorite(id: Long, currentlyFavorite: Boolean) {
        if (id <= 0L) return
        val newFavorite = !currentlyFavorite
        viewModelScope.launch {
            historyRepository.setFavorite(id, newFavorite)
            val current = _uiState.value
            if (current is FormulaUiState.Success && current.result.id == id) {
                _uiState.value = FormulaUiState.Success(current.result.copy(isFavorite = newFavorite))
            }
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteById(id)
        }
    }

    fun clearNonFavoriteHistory() {
        viewModelScope.launch {
            historyRepository.clearNonFavorites()
        }
    }
}
