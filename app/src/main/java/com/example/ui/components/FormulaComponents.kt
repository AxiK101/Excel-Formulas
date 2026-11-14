package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AltRoute
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ExcelDialect
import com.example.data.model.GeneratedFormulaResult
import com.example.data.validation.ExcelFormulaValidator
import com.example.data.validation.FormulaValidationResult
import com.example.ui.theme.FormulaCodeBg
import com.example.ui.theme.FormulaCodeBorder
import com.example.ui.theme.FormulaCodeGreekAccent
import com.example.ui.theme.FormulaCodeText
import com.example.ui.theme.FormulaCodeTextStyle
import kotlinx.coroutines.delay

@Composable
fun ThemeToggleButton(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggleTheme,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("theme_toggle_button"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                contentDescription = if (isDarkTheme) "Switch to light theme" else "Switch to dark theme",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = if (isDarkTheme) "Light" else "Dark",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DialectSegmentedSwitch(
    selectedDialect: ExcelDialect,
    onSelectDialect: (ExcelDialect) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "dialect"
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ExcelDialect.entries.forEach { dialect ->
                val isSelected = dialect == selectedDialect
                Surface(
                    onClick = { onSelectDialect(dialect) },
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("${testTagPrefix}_${dialect.name.lowercase()}"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${dialect.displayName} (${dialect.separator})",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormulaResultCard(
    result: GeneratedFormulaResult,
    activeDialect: ExcelDialect,
    onSelectResultDialect: (ExcelDialect) -> Unit,
    onCopyFormula: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onRefineFormula: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeFormula = result.primaryFormulaFor(activeDialect)
    var copiedRecently by remember(activeFormula) { mutableStateOf(false) }

    LaunchedEffect(copiedRecently) {
        if (copiedRecently) {
            delay(1_800L)
            copiedRecently = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header row: Category pill + Title + Favorite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = result.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = result.platform.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = result.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("favorite_result_button")
                    ) {
                        Icon(
                            imageVector = if (result.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (result.isFavorite) "Remove from favorites" else "Save to favorites",
                            tint = if (result.isFavorite) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                // Instant EN / ΕΛ Formula Language Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Translate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Excel Syntax",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ExcelDialect.entries.forEach { dialect ->
                            val selected = dialect == activeDialect
                            Surface(
                                onClick = { onSelectResultDialect(dialect) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("result_dialect_${dialect.name.lowercase()}"),
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                border = if (!selected) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                } else null
                            ) {
                                Text(
                                    text = dialect.shortBadge,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // High-contrast Monospace Formula Box (tap anywhere or use Copy button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(FormulaCodeBg)
                        .border(1.dp, FormulaCodeBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            onCopyFormula(activeFormula)
                            copiedRecently = true
                        }
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeFormula,
                                style = FormulaCodeTextStyle,
                                color = if (activeDialect == ExcelDialect.GREEK) {
                                    FormulaCodeGreekAccent
                                } else {
                                    FormulaCodeText
                                },
                                modifier = Modifier.testTag("generated_formula_text")
                            )
                        }

                        val isSyntaxVerified = remember(activeFormula, activeDialect) {
                            ExcelFormulaValidator.validateFormula(
                                activeFormula,
                                activeDialect
                            ) is FormulaValidationResult.Valid
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = if (activeDialect == ExcelDialect.GREEK) {
                                        "Ελληνικό Excel • διαχωριστικό ;"
                                    } else {
                                        "English Excel • separator ,"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                if (isSyntaxVerified) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.testTag("syntax_verified_badge")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Verified,
                                            contentDescription = "Syntax verified",
                                            tint = FormulaCodeGreekAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Syntax verified",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FormulaCodeGreekAccent
                                        )
                                    }
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    onCopyFormula(activeFormula)
                                    copiedRecently = true
                                },
                                modifier = Modifier.testTag("copy_formula_button"),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (copiedRecently) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.primaryContainer
                                    },
                                    contentColor = if (copiedRecently) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            ) {
                                Icon(
                                    imageVector = if (copiedRecently) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                    contentDescription = "Copy formula to clipboard",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (copiedRecently) "Copied!" else "Copy Formula",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                // Simple Explanation: Purpose & How It Functions
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Simple Explanation",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (result.purpose.isNotBlank()) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Purpose",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = result.purpose,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "How it works",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = result.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Greek Excel Tip Banner
                if (result.greekNotes.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Greek Excel Note (Ελληνικό Excel)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = result.greekNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Quick Refine Chips
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Refine Formula",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val refinements = listOf(
                            "Wrap in IFERROR / ΑΝ.ΣΦΑΛΜΑ" to "Wrap the formula in IFERROR / ΑΝ.ΣΦΑΛΜΑ so errors return blank or 0",
                            "Lock ranges ($)" to "Convert lookup/table cell ranges to absolute references using $ signs",
                            "Ignore blanks" to "Skip or ignore blank cells cleanly before calculating",
                            "Εξήγηση στα Ελληνικά" to "Keep the formulas accurate and provide the explanation and example in clear Greek"
                        )
                        refinements.forEach { (label, instruction) ->
                            AssistChip(
                                onClick = { onRefineFormula(instruction) },
                                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Illustrative Spreadsheet Example + Mini Sheet Preview
        if (result.usageExample.isNotBlank() || (result.sampleHeaders.isNotEmpty() && result.sampleRows.isNotEmpty())) {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TableChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Illustrative Spreadsheet Example",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (result.usageExample.isNotBlank()) {
                        Text(
                            text = result.usageExample,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (result.sampleHeaders.isNotEmpty() && result.sampleRows.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(vertical = 8.dp, horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "#",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(26.dp)
                                )
                                result.sampleHeaders.forEach { header ->
                                    Text(
                                        text = header,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 4.dp)
                                    )
                                }
                            }

                            result.sampleRows.forEach { row ->
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = row.rowNumber.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(26.dp)
                                    )
                                    result.sampleHeaders.indices.forEach { idx ->
                                        Text(
                                            text = row.cells.getOrElse(idx) { "" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }

                            if (result.sampleResultValue.isNotBlank()) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                                        .padding(vertical = 10.dp, horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Result in ${result.sampleResultCell}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = result.sampleResultValue,
                                        style = FormulaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Data Visualization for numeric / analytical formula requests
        result.chartSpec?.let { spec ->
            if (spec.shouldVisualize && spec.points.size >= 2) {
                SampleDataChartCard(chartSpec = spec)
            }
        }

        // Related & Alternative Formulas (1-2 suggestions)
        if (result.suggestions.isNotEmpty()) {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.AltRoute,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Related & Alternative Formulas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    result.suggestions.forEachIndexed { index, suggestion ->
                        if (index > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        val suggestionFormula = suggestion.formulaFor(activeDialect)
                        var suggestionCopied by remember(suggestionFormula) { mutableStateOf(false) }
                        LaunchedEffect(suggestionCopied) {
                            if (suggestionCopied) {
                                delay(1_800L)
                                suggestionCopied = false
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = suggestion.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = suggestion.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FormulaCodeBg)
                                    .clickable {
                                        onCopyFormula(suggestionFormula)
                                        suggestionCopied = true
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                    Text(
                                        text = suggestionFormula,
                                        style = FormulaCodeTextStyle,
                                        color = if (activeDialect == ExcelDialect.GREEK) {
                                            FormulaCodeGreekAccent
                                        } else {
                                            FormulaCodeText
                                        }
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        onCopyFormula(suggestionFormula)
                                        suggestionCopied = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("copy_suggestion_$index"),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (suggestionCopied) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        },
                                        contentColor = if (suggestionCopied) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        }
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (suggestionCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                        contentDescription = "Copy alternative formula to clipboard",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (suggestionCopied) "Copied!" else "Copy ${activeDialect.shortBadge}",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Breakdown Parts
        if (result.parts.isNotEmpty()) {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Formula Breakdown — Part by Part",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    result.parts.forEachIndexed { index, part ->
                        if (index > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = part.token,
                                        style = FormulaCodeTextStyle,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = part.role,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Text(
                                text = part.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
