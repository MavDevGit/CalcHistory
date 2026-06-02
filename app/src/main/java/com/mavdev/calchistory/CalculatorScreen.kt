package com.mavdev.calchistory

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

val LightBg = Color(0xFFF8FAFC)
val DarkBg = Color(0xFF0A0F1C)
val LightSurface = Color(0xFFFFFFFF)
val DarkSurface = Color(0xFF12182B)
val Indigo500 = Color(0xFF6366F1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel, innerPadding: PaddingValues) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val expression by viewModel.expression.collectAsState()
    val preview by viewModel.preview.collectAsState()
    val history by viewModel.history.collectAsState()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = if (isDarkMode) DarkBg.toArgb() else LightBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
        }
    }

    val bgColor = if (isDarkMode) DarkBg else LightBg
    val contentColor = if (isDarkMode) Color.White else Color.Black

    var showClearDialog by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (isDarkMode) darkColorScheme(background = DarkBg, surface = DarkSurface) 
                      else lightColorScheme(background = LightBg, surface = LightSurface)
    ) {
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CalcHistory",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = contentColor
                    )
                    Row {
                        IconButton(onClick = { viewModel.toggleTheme() }) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Filled.WbSunny else Icons.Filled.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = contentColor.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Clear History",
                                tint = Color.Red.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // History
                val listState = rememberLazyListState()
                LaunchedEffect(history.size) {
                    if (history.isNotEmpty()) {
                        listState.animateScrollToItem(history.size - 1)
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (history.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = contentColor.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("El historial está vacío", color = contentColor.copy(alpha = 0.5f))
                            }
                        }
                    } else {
                        items(history) { item ->
                            HistoryCard(
                                item = item,
                                isDarkMode = isDarkMode,
                                onReuseFormula = { viewModel.reuseFormula(item.rawExpr) },
                                onReuseResult = { viewModel.reuseResult(item.result) },
                                onDelete = { viewModel.deleteHistoryItem(item.id) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                // Display
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    val scrollState = rememberScrollState()
                    LaunchedEffect(expression) { scrollState.animateScrollTo(scrollState.maxValue) }
                    
                    Text(
                        text = if (expression.isEmpty()) "0" else viewModel.formatDisplay(expression),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.sumHistory() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Sumar historial", fontSize = 14.sp, color = Indigo500)
                        }
                        Text(
                            text = preview,
                            fontSize = 20.sp,
                            color = contentColor.copy(alpha = 0.5f),
                            textAlign = TextAlign.End,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Keyboard
                Keyboard(
                    isDarkMode = isDarkMode,
                    onKeyPress = { viewModel.onKeyPress(it) }
                )
            }

            if (showClearDialog) {
                AlertDialog(
                    onDismissRequest = { showClearDialog = false },
                    title = { Text("¿Borrar todo?") },
                    text = { Text("Esta acción eliminará todos los registros de la calculadora.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.clearHistory()
                                showClearDialog = false
                            }
                        ) {
                            Text("Confirmar", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HistoryCard(
    item: HistoryItem,
    isDarkMode: Boolean,
    onReuseFormula: () -> Unit,
    onReuseResult: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val surfaceColor = if (isDarkMode) Color(0xFF1E253A) else Color.White
    val contentColor = if (isDarkMode) Color.White else Color.Black

    Card(
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.formattedExpr,
                color = contentColor.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onReuseFormula() },
                textAlign = TextAlign.End,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "= ${item.result}",
                color = contentColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onReuseResult() },
                textAlign = TextAlign.End,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    TextButton(onClick = onReuseFormula, contentPadding = PaddingValues(0.dp)) {
                        Text("Usar fórmula", fontSize = 12.sp, color = Indigo500)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onReuseResult, contentPadding = PaddingValues(0.dp)) {
                        Text("Usar valor", fontSize = 12.sp, color = Color(0xFF10B981))
                    }
                }
                Row {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("calc", item.result))
                            Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = contentColor.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun Keyboard(isDarkMode: Boolean, onKeyPress: (String) -> Unit) {
    val buttons = listOf(
        listOf("AC", "%", "DEL", "/"),
        listOf("7", "8", "9", "*"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDarkMode) DarkBg else LightBg)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (row in buttons) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (btn in row) {
                    val weight = if (btn == "0") 2.1f else 1f
                    CalcButton(
                        text = btn,
                        modifier = Modifier.weight(weight),
                        isDarkMode = isDarkMode,
                        isEqual = (btn == "="),
                        onClick = { onKeyPress(btn) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean,
    isEqual: Boolean = false,
    onClick: () -> Unit
) {
    val isOperator = listOf("/", "*", "-", "+", "%").contains(text)
    val isAction = listOf("AC", "(", ")", "DEL").contains(text)
    
    val bgColor = when {
        isEqual -> Indigo500
        isAction -> if (isDarkMode) Color(0xFF1E253A) else Color(0xFFE2E8F0)
        isOperator -> if (isDarkMode) Color(0xFF1E253A) else Color(0xFFEEF2FF)
        else -> if (isDarkMode) Color(0xFF1E253A) else Color.White
    }

    val textColor = when {
        isEqual -> Color.White
        text == "AC" || text == "DEL" -> Color.Red
        isOperator -> Indigo500
        else -> if (isDarkMode) Color.White else Color.Black
    }

    Box(
        modifier = modifier
            .height(60.dp)
            .shadow(
                elevation = if (isDarkMode || isEqual) 0.dp else 2.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text == "DEL") {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "DEL", tint = textColor)
        } else {
            val displayText = when (text) {
                "/" -> "÷"
                "*" -> "×"
                else -> text
            }
            Text(
                text = displayText,
                fontSize = if (isEqual) 20.sp else 24.sp,
                fontWeight = if (isEqual || isOperator || isAction) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}
