package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ChartKind
import com.example.data.model.FormulaChartSpec
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun SampleDataChartCard(
    chartSpec: FormulaChartSpec,
    modifier: Modifier = Modifier
) {
    val points = chartSpec.points
    if (!chartSpec.shouldVisualize || points.size < 2) return

    var chartKind by remember(chartSpec) { mutableStateOf(chartSpec.chartType) }
    var selectedIndex by remember(chartSpec) {
        mutableIntStateOf(points.indexOfFirst { it.isHighlighted }.takeIf { it >= 0 } ?: 0)
    }
    var animateTrigger by remember(chartSpec, chartKind) { mutableStateOf(false) }

    LaunchedEffect(chartSpec, chartKind) {
        animateTrigger = true
    }

    val progress by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "chartReveal"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val mutedColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.48f)
    val selectedColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val lineColor = MaterialTheme.colorScheme.primary
    val surfaceBg = MaterialTheme.colorScheme.surface

    val maxVal = remember(points) { max(points.maxOf { it.value }, 0.0) }
    val minVal = remember(points) { min(points.minOf { it.value }, 0.0) }
    val range = remember(maxVal, minVal) { max(maxVal - minVal, 1.0) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sample_chart_card"),
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
            // Header + Bar/Line Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = chartSpec.title.ifBlank { "Sample Data Visualization" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (chartSpec.yAxisLabel.isNotBlank()) {
                        Text(
                            text = chartSpec.yAxisLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        ChartKind.entries.forEach { kind ->
                            val isSelected = kind == chartKind
                            Surface(
                                onClick = { chartKind = kind },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("chart_type_${kind.name.lowercase()}"),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                },
                                contentColor = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (kind == ChartKind.BAR) {
                                            Icons.Outlined.BarChart
                                        } else {
                                            Icons.Outlined.ShowChart
                                        },
                                        contentDescription = if (kind == ChartKind.BAR) "Bar chart" else "Line chart",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (kind == ChartKind.BAR) "Bar" else "Line",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Selected Point Readout Pill
            val activePoint = points.getOrNull(selectedIndex)
            if (activePoint != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (activePoint.isHighlighted) primaryColor else selectedColor
                                    )
                            )
                            Text(
                                text = activePoint.label + if (activePoint.isHighlighted) " (Matched)" else "",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = formatNumericValue(activePoint.value),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Native Compose Canvas Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Y-Axis Labels
                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .height(160.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatCompactValue(maxVal),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = formatCompactValue((maxVal + minVal) / 2.0),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = formatCompactValue(minVal),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val slotWidth = size.width.toFloat() / points.size.coerceAtLeast(1)
                                val tapped = (offset.x / slotWidth).toInt()
                                    .coerceIn(0, points.lastIndex)
                                selectedIndex = tapped
                            }
                        }
                ) {
                    val topPad = 12.dp.toPx()
                    val bottomPad = 12.dp.toPx()
                    val chartHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)
                    val zeroY = topPad + chartHeight * ((maxVal - 0.0) / range).toFloat()

                    // Horizontal Gridlines (top, mid, bottom)
                    listOf(0f, 0.5f, 1f).forEach { fraction ->
                        val y = topPad + chartHeight * fraction
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val slotWidth = size.width / points.size

                    if (chartKind == ChartKind.BAR) {
                        val barWidth = (slotWidth * 0.54f).coerceAtMost(42.dp.toPx())
                        points.forEachIndexed { idx, pt ->
                            val centerX = slotWidth * idx + slotWidth / 2f
                            val valueY = topPad + chartHeight * ((maxVal - pt.value) / range).toFloat()
                            val barTop = minOf(valueY, zeroY)
                            val rawHeight = abs(zeroY - valueY) * progress
                            val animatedTop = if (pt.value >= 0) zeroY - rawHeight else zeroY

                            val barColor = when {
                                idx == selectedIndex -> selectedColor
                                pt.isHighlighted -> primaryColor
                                else -> mutedColor
                            }

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(centerX - barWidth / 2f, animatedTop),
                                size = Size(barWidth, rawHeight.coerceAtLeast(3.dp.toPx())),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    } else {
                        val coords = points.mapIndexed { idx, pt ->
                            val x = slotWidth * idx + slotWidth / 2f
                            val targetY = topPad + chartHeight * ((maxVal - pt.value) / range).toFloat()
                            val y = zeroY + (targetY - zeroY) * progress
                            Offset(x, y)
                        }

                        if (coords.size >= 2) {
                            val areaPath = Path().apply {
                                moveTo(coords.first().x, zeroY)
                                coords.forEach { lineTo(it.x, it.y) }
                                lineTo(coords.last().x, zeroY)
                                close()
                            }
                            drawPath(
                                path = areaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        lineColor.copy(alpha = 0.28f),
                                        lineColor.copy(alpha = 0.02f)
                                    ),
                                    startY = topPad,
                                    endY = zeroY
                                )
                            )

                            val linePath = Path().apply {
                                moveTo(coords.first().x, coords.first().y)
                                for (i in 1 until coords.size) {
                                    lineTo(coords[i].x, coords[i].y)
                                }
                            }
                            drawPath(
                                path = linePath,
                                color = lineColor,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        coords.forEachIndexed { idx, offset ->
                            val pt = points[idx]
                            val dotColor = when {
                                idx == selectedIndex -> selectedColor
                                pt.isHighlighted -> primaryColor
                                else -> mutedColor
                            }
                            drawCircle(
                                color = surfaceBg,
                                radius = 6.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = dotColor,
                                radius = if (idx == selectedIndex) 5.dp.toPx() else 4.dp.toPx(),
                                center = offset
                            )
                        }
                    }
                }
            }

            // X-Axis Labels aligned under each slot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 52.dp)
            ) {
                points.forEachIndexed { idx, pt ->
                    Text(
                        text = pt.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (idx == selectedIndex) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Legend
            if (points.any { it.isHighlighted }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = primaryColor, label = "Matched / Target")
                    LegendDot(color = mutedColor, label = "Other Sample Rows")
                    LegendDot(color = selectedColor, label = "Selected")
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = CircleShape,
            color = color
        ) {}
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatNumericValue(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        String.format("%.2f", value)
    }
}

private fun formatCompactValue(value: Double): String {
    val absVal = abs(value)
    return when {
        absVal >= 1_000_000 -> String.format("%.1fM", value / 1_000_000)
        absVal >= 1_000 -> String.format("%.1fk", value / 1_000)
        value % 1.0 == 0.0 -> value.toLong().toString()
        else -> String.format("%.1f", value)
    }
}
