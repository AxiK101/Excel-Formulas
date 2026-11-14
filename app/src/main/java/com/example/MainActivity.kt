package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.AppThemeMode
import com.example.firebase.configureAppCheck
import com.example.ui.screens.DictionaryScreen
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.theme.FormulaGridTheme
import com.example.ui.viewmodel.FormulaViewModel
import kotlinx.coroutines.launch

const val ROUTE_GENERATE = "generate"
const val ROUTE_LIBRARY = "library"
const val ROUTE_DICTIONARY = "dictionary"

private data class NavDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check before UI, ViewModels, or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            val viewModel: FormulaViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> systemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            LaunchedEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
            }

            FormulaGridTheme(darkTheme = isDarkTheme) {
                FormulaGridApp(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleLightDarkTheme(isDarkTheme) }
                )
            }
        }
    }
}

@Composable
fun FormulaGridApp(
    viewModel: FormulaViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_GENERATE

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val promptText by viewModel.promptText.collectAsStateWithLifecycle()
    val sheetContextText by viewModel.sheetContextText.collectAsStateWithLifecycle()
    val preferredDialect by viewModel.preferredDialect.collectAsStateWithLifecycle()
    val activeResultDialect by viewModel.activeResultDialect.collectAsStateWithLifecycle()
    val preferredPlatform by viewModel.preferredPlatform.collectAsStateWithLifecycle()
    val allHistory by viewModel.allHistory.collectAsStateWithLifecycle()
    val libraryItems by viewModel.filteredHistory.collectAsStateWithLifecycle()
    val libraryFilter by viewModel.libraryFilter.collectAsStateWithLifecycle()
    val selectedHistoryCategory by viewModel.selectedHistoryCategory.collectAsStateWithLifecycle()
    val librarySearchQuery by viewModel.librarySearchQuery.collectAsStateWithLifecycle()
    val availableHistoryFunctions by viewModel.availableHistoryFunctions.collectAsStateWithLifecycle()
    val selectedHistoryFunction by viewModel.selectedHistoryFunction.collectAsStateWithLifecycle()
    val dictionaryEntries by viewModel.filteredDictionary.collectAsStateWithLifecycle()
    val dictionaryQuery by viewModel.dictionaryQuery.collectAsStateWithLifecycle()
    val dictionaryCategory by viewModel.dictionaryCategory.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val copiedMessage = stringResource(R.string.copied_message)

    val copyToClipboard: (String) -> Unit = { formula ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Excel Formula", formula))
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(copiedMessage)
        }
    }

    val destinations = remember {
        listOf(
            NavDestination(
                route = ROUTE_GENERATE,
                labelRes = R.string.nav_generate,
                selectedIcon = Icons.Filled.AutoAwesome,
                unselectedIcon = Icons.Outlined.AutoAwesome,
                testTag = "nav_generate"
            ),
            NavDestination(
                route = ROUTE_LIBRARY,
                labelRes = R.string.nav_library,
                selectedIcon = Icons.Filled.Bookmark,
                unselectedIcon = Icons.Outlined.BookmarkBorder,
                testTag = "nav_library"
            ),
            NavDestination(
                route = ROUTE_DICTIONARY,
                labelRes = R.string.nav_guide,
                selectedIcon = Icons.Filled.Translate,
                unselectedIcon = Icons.Outlined.Translate,
                testTag = "nav_dictionary"
            )
        )
    }

    val navigateTo: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_GENERATE,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ROUTE_GENERATE) {
                        GeneratorScreen(
                            uiState = uiState,
                            promptText = promptText,
                            sheetContextText = sheetContextText,
                            preferredDialect = preferredDialect,
                            activeResultDialect = activeResultDialect,
                            preferredPlatform = preferredPlatform,
                            quickSuggestions = viewModel.quickSuggestions,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = onToggleTheme,
                            onPromptChange = viewModel::updatePromptText,
                            onSheetContextChange = viewModel::updateSheetContextText,
                            onSelectPreferredDialect = viewModel::setPreferredDialect,
                            onSelectResultDialect = viewModel::setActiveResultDialect,
                            onSelectPlatform = viewModel::setPreferredPlatform,
                            onGenerate = viewModel::generateFormula,
                            onApplyQuickSuggestion = viewModel::applyQuickSuggestion,
                            onRefineFormula = viewModel::refineCurrentFormula,
                            onCopyFormula = copyToClipboard,
                            onToggleFavorite = viewModel::toggleFavorite
                        )
                    }

                    composable(ROUTE_LIBRARY) {
                        LibraryScreen(
                            items = libraryItems,
                            totalHistoryCount = allHistory.size,
                            filter = libraryFilter,
                            historyCategories = viewModel.historyCategories,
                            selectedCategory = selectedHistoryCategory,
                            searchQuery = librarySearchQuery,
                            availableFunctions = availableHistoryFunctions,
                            selectedFunction = selectedHistoryFunction,
                            preferredDialect = preferredDialect,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = onToggleTheme,
                            onFilterChange = viewModel::setLibraryFilter,
                            onSelectCategory = viewModel::setHistoryCategoryFilter,
                            onSearchQueryChange = viewModel::updateLibrarySearchQuery,
                            onToggleFunctionFilter = viewModel::toggleHistoryFunctionFilter,
                            onClearFilters = viewModel::clearHistorySearchAndFilters,
                            onOpenItem = { item ->
                                viewModel.openSavedResult(item)
                                navigateTo(ROUTE_GENERATE)
                            },
                            onCopyFormula = copyToClipboard,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onDeleteItem = viewModel::deleteHistoryItem,
                            onClearNonFavorites = viewModel::clearNonFavoriteHistory
                        )
                    }

                    composable(ROUTE_DICTIONARY) {
                        DictionaryScreen(
                            entries = dictionaryEntries,
                            query = dictionaryQuery,
                            selectedCategory = dictionaryCategory,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = onToggleTheme,
                            onQueryChange = viewModel::updateDictionaryQuery,
                            onSelectCategory = viewModel::setDictionaryCategory,
                            onCopySyntax = copyToClipboard,
                            onTryInGenerator = { samplePrompt ->
                                viewModel.prefillPrompt(samplePrompt)
                                navigateTo(ROUTE_GENERATE)
                            }
                        )
                    }
                }
            }
        }
    }
}
