package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ExcelDialect
import com.example.data.model.ExcelPlatform
import com.example.data.model.QuickPromptSuggestion
import com.example.ui.components.DialectSegmentedSwitch
import com.example.ui.components.FormulaResultCard
import com.example.ui.components.ThemeToggleButton
import com.example.ui.viewmodel.FormulaUiState

@Composable
fun GeneratorScreen(
    uiState: FormulaUiState,
    promptText: String,
    sheetContextText: String,
    preferredDialect: ExcelDialect,
    activeResultDialect: ExcelDialect,
    preferredPlatform: ExcelPlatform,
    quickSuggestions: List<QuickPromptSuggestion>,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onPromptChange: (String) -> Unit,
    onSheetContextChange: (String) -> Unit,
    onSelectPreferredDialect: (ExcelDialect) -> Unit,
    onSelectResultDialect: (ExcelDialect) -> Unit,
    onSelectPlatform: (ExcelPlatform) -> Unit,
    onGenerate: () -> Unit,
    onApplyQuickSuggestion: (QuickPromptSuggestion) -> Unit,
    onRefineFormula: (String) -> Unit,
    onCopyFormula: (String) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var showSheetContext by remember { mutableStateOf(sheetContextText.isNotBlank()) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Studio Brand Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Functions,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "English & Ελληνικό Excel Studio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme
                )
            }

            // Preferred Dialect Switch (English vs Ελληνικό Excel)
            DialectSegmentedSwitch(
                selectedDialect = preferredDialect,
                onSelectDialect = onSelectPreferredDialect,
                testTagPrefix = "preferred_dialect"
            )

            // Target Spreadsheet Platform Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExcelPlatform.entries.forEach { platform ->
                    val selected = platform == preferredPlatform
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectPlatform(platform) },
                        label = { Text(platform.label, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("platform_${platform.name.lowercase()}")
                    )
                }
            }

            // Prompt Composer Card
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.hero_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = promptText,
                        onValueChange = onPromptChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prompt_input"),
                        minLines = 3,
                        maxLines = 6,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.prompt_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (promptText.isNotEmpty()) {
                                IconButton(
                                    onClick = { onPromptChange("") },
                                    modifier = Modifier.testTag("clear_prompt_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear prompt"
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Collapsible Sheet Context
                    Surface(
                        onClick = { showSheetContext = !showSheetContext },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.testTag("toggle_sheet_context")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.GridOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Columns / Sheet Layout (Optional)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = if (showSheetContext) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = "Toggle columns context",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    AnimatedVisibility(visible = showSheetContext) {
                        OutlinedTextField(
                            value = sheetContextText,
                            onValueChange = onSheetContextChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("context_input"),
                            singleLine = true,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.context_hint),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            onGenerate()
                        },
                        enabled = uiState !is FormulaUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        if (uiState is FormulaUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.action_generating),
                                style = MaterialTheme.typography.labelLarge
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.action_generate),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // Bilingual Quick-Try Examples
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Examples (English & Ελληνικά)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickSuggestions.forEach { suggestion ->
                        AssistChip(
                            onClick = {
                                keyboardController?.hide()
                                onApplyQuickSuggestion(suggestion)
                            },
                            label = {
                                Text(
                                    text = suggestion.label,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }
            }

            // State-driven Output Area
            when (uiState) {
                is FormulaUiState.Idle -> {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                painter = painterResource(id = R.drawable.img_formula_hero),
                                contentDescription = "Spreadsheet formula studio illustration",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                            )
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Ready for English & Ελληνικό Excel",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Type what you need in English or Greek. Every answer generates both standard English syntax (comma ,) and Greek Excel syntax (localized functions like ΑΘΡΟΙΣΜΑ, ΑΝ, ΚΑΘΕΤΗ.ΑΝΑΖΗΤΗΣΗ with semicolon ;).",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                is FormulaUiState.Loading -> {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Crafting your Excel formulas…",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Generating English & Greek syntax, breakdown, and sample preview.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                is FormulaUiState.Error -> {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Could not generate formula",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Text(
                                text = uiState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            OutlinedButton(
                                onClick = onGenerate,
                                modifier = Modifier.testTag("retry_button"),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Try Again",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                is FormulaUiState.Success -> {
                    FormulaResultCard(
                        result = uiState.result,
                        activeDialect = activeResultDialect,
                        onSelectResultDialect = onSelectResultDialect,
                        onCopyFormula = onCopyFormula,
                        onToggleFavorite = {
                            onToggleFavorite(uiState.result.id, uiState.result.isFavorite)
                        },
                        onRefineFormula = onRefineFormula
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
