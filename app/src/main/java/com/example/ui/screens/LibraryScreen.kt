package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ExcelDialect
import com.example.data.model.GeneratedFormulaResult
import com.example.ui.components.ThemeToggleButton
import com.example.ui.theme.FormulaCodeBg
import com.example.ui.theme.FormulaCodeGreekAccent
import com.example.ui.theme.FormulaCodeText
import com.example.ui.theme.FormulaCodeTextStyle
import com.example.ui.viewmodel.LibraryFilter
import java.text.DateFormat
import java.util.Date

@Composable
fun LibraryScreen(
    items: List<GeneratedFormulaResult>,
    totalHistoryCount: Int,
    filter: LibraryFilter,
    historyCategories: List<String>,
    selectedCategory: String,
    searchQuery: String,
    availableFunctions: List<String>,
    selectedFunction: String?,
    preferredDialect: ExcelDialect,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onFilterChange: (LibraryFilter) -> Unit,
    onSelectCategory: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleFunctionFilter: (String) -> Unit,
    onClearFilters: () -> Unit,
    onOpenItem: (GeneratedFormulaResult) -> Unit,
    onCopyFormula: (String) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearNonFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Saved & Recent Formulas",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Cached locally on your device for instant reuse",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (items.any { !it.isFavorite }) {
                        TextButton(
                            onClick = onClearNonFavorites,
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Text("Clear")
                        }
                    }
                    ThemeToggleButton(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_input"),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                placeholder = { Text("Search formulas, prompts, or functions…") },
                shape = RoundedCornerShape(14.dp)
            )

            // Category Filter Chips (e.g., Data Analysis, Finance, String Manipulation)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                historyCategories.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(category) },
                        label = { Text(category, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        modifier = Modifier.testTag(
                            "library_category_${category.lowercase().replace(" ", "_")}"
                        )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LibraryFilter.entries.forEach { option ->
                    FilterChip(
                        selected = option == filter,
                        onClick = { onFilterChange(option) },
                        label = { Text(option.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("library_filter_${option.name.lowercase()}")
                    )
                }
                availableFunctions.forEach { fnName ->
                    val isSelected = selectedFunction == fnName
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggleFunctionFilter(fnName) },
                        label = { Text(fnName, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("library_fn_chip_$fnName")
                    )
                }
            }

            if (items.isEmpty()) {
                val hasActiveFilters = totalHistoryCount > 0 &&
                    (searchQuery.isNotBlank() ||
                        selectedFunction != null ||
                        selectedCategory != "All Categories" ||
                        filter != LibraryFilter.ALL)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(
                                imageVector = if (hasActiveFilters) {
                                    Icons.Filled.Search
                                } else {
                                    Icons.Outlined.BookmarkBorder
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(18.dp)
                                    .size(34.dp)
                            )
                        }
                        Text(
                            text = if (hasActiveFilters) {
                                "No matching formulas found"
                            } else {
                                stringResource(R.string.empty_library_title)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (hasActiveFilters) {
                                "Try another keyword or clear the active Excel function filter."
                            } else {
                                stringResource(R.string.empty_library_subtitle)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (hasActiveFilters) {
                            TextButton(
                                onClick = onClearFilters,
                                modifier = Modifier.testTag("reset_library_search_button")
                            ) {
                                Text("Reset Search & Filters")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        val previewFormula = item.primaryFormulaFor(preferredDialect)
                        Card(
                            onClick = { onOpenItem(item) },
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                width = if (item.isFavorite) 1.5.dp else 1.dp,
                                color = if (item.isFavorite) {
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f)
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = item.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            Text(
                                                text = DateFormat.getDateTimeInstance(
                                                    DateFormat.SHORT,
                                                    DateFormat.SHORT
                                                ).format(Date(item.timestampMillis)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Row {
                                        IconButton(
                                            onClick = { onToggleFavorite(item.id, item.isFavorite) },
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .testTag("library_favorite_${item.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                                contentDescription = if (item.isFavorite) {
                                                    "Remove from favorites"
                                                } else {
                                                    "Star formula as favorite"
                                                },
                                                tint = if (item.isFavorite) {
                                                    MaterialTheme.colorScheme.tertiary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteItem(item.id) },
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .testTag("library_delete_${item.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.DeleteOutline,
                                                contentDescription = "Delete formula",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = item.userPrompt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FormulaCodeBg)
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                        Text(
                                            text = previewFormula,
                                            style = FormulaCodeTextStyle,
                                            color = if (preferredDialect == ExcelDialect.GREEK) {
                                                FormulaCodeGreekAccent
                                            } else {
                                                FormulaCodeText
                                            }
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = { onCopyFormula(item.englishFormula) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("library_copy_en_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = "Copy English formula",
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy EN", style = MaterialTheme.typography.labelMedium)
                                    }
                                    FilledTonalButton(
                                        onClick = { onCopyFormula(item.greekFormula) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("library_copy_el_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = "Copy Greek formula",
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy ΕΛ", style = MaterialTheme.typography.labelMedium)
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    TextButton(onClick = { onOpenItem(item) }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Open")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
