package com.example

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HistoryEntry
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: CalculatorViewModel by viewModels()
                CalculatorApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorApp(viewModel: CalculatorViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val expression by viewModel.expression.collectAsStateWithLifecycle()
    val displayResult by viewModel.displayResult.collectAsStateWithLifecycle()
    val isScientific by viewModel.isScientific.collectAsStateWithLifecycle()
    val isDegreeMode by viewModel.isDegreeMode.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    
    var showHistorySheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.height(56.dp))
                
                Text(
                    text = "Menu",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Basic Mode", fontSize = 16.sp) },
                    selected = !isScientific,
                    onClick = {
                        viewModel.isScientific.value = false
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Scientific Mode", fontSize = 16.sp) },
                    selected = isScientific,
                    onClick = {
                        viewModel.isScientific.value = true
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                NavigationDrawerItem(
                    label = { Text("History logs", fontSize = 16.sp) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showHistorySheet = true
                    },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Settings", fontSize = 16.sp) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showSettingsDialog = true
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Calculator",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("menu_trigger")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu Drawer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showHistorySheet = true },
                            modifier = Modifier.testTag("quick_history_trigger")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "View History",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // 1. Math Entry Display Screen Area
                    DisplayScreenArea(
                        expression = expression,
                        displayResult = displayResult,
                        isDegreeMode = isDegreeMode,
                        isScientific = isScientific,
                        onBackspace = { viewModel.backspace() },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Beautiful Mode Indicators Bar (RAD / DEG etc.)
                    if (isScientific) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            // Rad/Deg Mode selector pill
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        viewModel.isDegreeMode.value = !isDegreeMode
                                    }
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isDegreeMode) "DEG" else "RAD",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.05.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            // Active Scientific tag
                            Text(
                                text = "SCIENTIFIC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                letterSpacing = 0.1.sp
                            )
                        }
                    }

                    // 3. Mathematical Keypad Area
                    KeypadArea(
                        viewModel = viewModel,
                        isScientific = isScientific,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(bottom = 24.dp)
                    )
                }
            }
        }
    }

    // 4. Persistence History logs Slide up Bottom Sheet
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
        ) {
            HistorySheetContent(
                historyList = historyList,
                onUseEntry = { entry ->
                    viewModel.useHistoryItem(entry)
                    showHistorySheet = false
                },
                onDeleteItem = { id -> viewModel.deleteHistoryItem(id) },
                onClearAll = { viewModel.clearHistory() },
                onClose = { showHistorySheet = false }
            )
        }
    }

    // 5. App Setting Options Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            vibrationEnabled = vibrationEnabled,
            onVibrationToggle = { viewModel.vibrationEnabled.value = it },
            onClearHistory = {
                viewModel.clearHistory()
                showSettingsDialog = false
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun DisplayScreenArea(
    expression: String,
    displayResult: String,
    isDegreeMode: Boolean,
    isScientific: Boolean,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val view = LocalView.current
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
    ) {
        // Horizontal Row with expression formula
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = expression,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main resulting output
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            // Elegant backspace button floats left of result
            if (expression.isNotEmpty() || displayResult != "0") {
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onBackspace()
                    },
                    modifier = Modifier
                        .testTag("btn_backspace")
                        .padding(end = 16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Backspace",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Adaptive result styling based on string size
            val sizeClass = when {
                displayResult.length > 15 -> 32.sp
                displayResult.length > 10 -> 48.sp
                else -> 64.sp
            }

            Text(
                text = displayResult,
                fontSize = sizeClass,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .weight(1f)
                    .testTag("main_display_text")
                    .horizontalScroll(rememberScrollState()),
                maxLines = 1
            )
        }
    }
}

@Composable
fun KeypadArea(
    viewModel: CalculatorViewModel,
    isScientific: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Toggle animation for the Scientific operators grid
        AnimatedVisibility(
            visible = isScientific,
            enter = expandVertically(animationSpec = spring()) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring()) + fadeOut()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // Row S1: sin(, cos(, tan(, (, )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ScientificButton(text = "sin", onClick = { viewModel.appendCharacter("sin(") }, modifier = Modifier.weight(1f), testTag = "btn_sin")
                    ScientificButton(text = "cos", onClick = { viewModel.appendCharacter("cos(") }, modifier = Modifier.weight(1f), testTag = "btn_cos")
                    ScientificButton(text = "tan", onClick = { viewModel.appendCharacter("tan(") }, modifier = Modifier.weight(1f), testTag = "btn_tan")
                    ScientificButton(text = "(", onClick = { viewModel.appendCharacter("(") }, modifier = Modifier.weight(1f), testTag = "btn_paren_open")
                    ScientificButton(text = ")", onClick = { viewModel.appendCharacter(")") }, modifier = Modifier.weight(1f), testTag = "btn_paren_close")
                }
                // Row S2: log(, ln(, sqrt(, ^, π
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ScientificButton(text = "log", onClick = { viewModel.appendCharacter("log(") }, modifier = Modifier.weight(1f), testTag = "btn_log")
                    ScientificButton(text = "ln", onClick = { viewModel.appendCharacter("ln(") }, modifier = Modifier.weight(1f), testTag = "btn_ln")
                    ScientificButton(text = "√", onClick = { viewModel.appendCharacter("sqrt(") }, modifier = Modifier.weight(1f), testTag = "btn_sqrt")
                    ScientificButton(text = "^", onClick = { viewModel.appendOperator("^") }, modifier = Modifier.weight(1f), testTag = "btn_power")
                    ScientificButton(text = "π", onClick = { viewModel.appendCharacter("π") }, modifier = Modifier.weight(1f), testTag = "btn_pi")
                }
                // Row S3: e, RAD/DEG switcher row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ScientificButton(text = "e", onClick = { viewModel.appendCharacter("e") }, modifier = Modifier.weight(1f), testTag = "btn_e")
                    
                    // Separator/Spacer to align beautifully
                    Spacer(modifier = Modifier.weight(4f))
                }
            }
        }

        // Standard Keyboard rows
        // Row 1: AC, +/-, %, ÷
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalculatorButton(
                text = "AC",
                backgroundColor = MaterialTheme.colorScheme.secondary,
                textColor = MaterialTheme.colorScheme.onSecondary,
                onClick = { viewModel.clear() },
                modifier = Modifier.weight(1f),
                testTag = "btn_ac"
            )
            CalculatorButton(
                text = "+/-",
                backgroundColor = MaterialTheme.colorScheme.secondary,
                textColor = MaterialTheme.colorScheme.onSecondary,
                onClick = { viewModel.handleSignToggle() },
                modifier = Modifier.weight(1f),
                testTag = "btn_toggle_sign"
            )
            CalculatorButton(
                text = "%",
                backgroundColor = MaterialTheme.colorScheme.secondary,
                textColor = MaterialTheme.colorScheme.onSecondary,
                onClick = { viewModel.handlePercent() },
                modifier = Modifier.weight(1f),
                testTag = "btn_percent"
            )
            CalculatorButton(
                text = "÷",
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { viewModel.appendOperator("÷") },
                modifier = Modifier.weight(1f),
                testTag = "btn_divide"
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalculatorButton(
                text = "7",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.appendCharacter("7") },
                modifier = Modifier.weight(1f),
                testTag = "btn_7"
            )
            CalculatorButton(
                text = "8",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.appendCharacter("8") },
                modifier = Modifier.weight(1f),
                testTag = "btn_8"
            )
            CalculatorButton(
                text = "9",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.appendCharacter("9") },
                modifier = Modifier.weight(1f),
                testTag = "btn_9"
            )
            CalculatorButton(
                text = "×",
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { viewModel.appendOperator("×") },
                modifier = Modifier.weight(1f),
                testTag = "btn_multiply"
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalculatorButton(
                text = "4",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.appendCharacter("4") },
                modifier = Modifier.weight(1f),
                testTag = "btn_4"
            )
            CalculatorButton(
                text = "5",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.appendCharacter("5") },
                modifier = Modifier.weight(1f),
                testTag = "btn_5"
            )
            CalculatorButton(
                text = "6",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.appendCharacter("6") },
                modifier = Modifier.weight(1f),
                testTag = "btn_6"
            )
            CalculatorButton(
                text = "−",
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { viewModel.appendOperator("−") },
                modifier = Modifier.weight(1f),
                testTag = "btn_subtract"
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalculatorButton(
                text = "1",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.appendCharacter("1") },
                modifier = Modifier.weight(1f),
                testTag = "btn_1"
            )
            CalculatorButton(
                text = "2",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.appendCharacter("2") },
                modifier = Modifier.weight(1f),
                testTag = "btn_2"
            )
            CalculatorButton(
                text = "3",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.appendCharacter("3") },
                modifier = Modifier.weight(1f),
                testTag = "btn_3"
            )
            CalculatorButton(
                text = "+",
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { viewModel.appendOperator("+") },
                modifier = Modifier.weight(1f),
                testTag = "btn_add"
            )
        }

        // Row 5: 0, ., =
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Spans 2 columns using 2f weight
            CalculatorButton(
                text = "0",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.appendCharacter("0") },
                modifier = Modifier.weight(2f),
                testTag = "btn_0"
            )
            CalculatorButton(
                text = ".",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.appendCharacter(".") },
                modifier = Modifier.weight(1f),
                testTag = "btn_dot"
            )
            CalculatorButton(
                text = "=",
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { viewModel.evaluate() },
                modifier = Modifier.weight(1f),
                testTag = "btn_equals"
            )
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val view = LocalView.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag(testTag)
            .height(72.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ScientificButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val view = LocalView.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag(testTag)
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                RoundedCornerShape(18.dp)
            )
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun HistorySheetContent(
    historyList: List<HistoryEntry>,
    onUseEntry: (HistoryEntry) -> Unit,
    onDeleteItem: (Int) -> Unit,
    onClearAll: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History Tape",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (historyList.isNotEmpty()) {
                TextButton(onClick = onClearAll) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No calculations yet.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Perform operations to view logs here.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(historyList, key = { it.id }) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                            .clickable { onUseEntry(entry) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.expression,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "= ${entry.result}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        IconButton(onClick = { onDeleteItem(entry.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete entry",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Close", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp)
        }
    }
}

@Composable
fun SettingsDialog(
    vibrationEnabled: Boolean,
    onVibrationToggle: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                // Vibration Haptics option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Key vibration",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Haptic tap feedback on keypad clicks",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = onVibrationToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Database reset option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reset history",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Delete all calculations",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onClearHistory,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reset", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Done", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
