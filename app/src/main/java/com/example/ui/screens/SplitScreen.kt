package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.SplitViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitScreen(
    viewModel: SplitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val subtotal = viewModel.activeSubtotal
    val peopleCount by viewModel.peopleCount.collectAsState()
    val tipPercent by viewModel.tipPercent.collectAsState()
    val customTipText by viewModel.customTipText.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var isCustomTipSelected by remember { mutableStateOf(false) }

    // Dynamic calculations
    val tipAmount = subtotal * (tipPercent / 100.0)
    val grandTotal = subtotal + tipAmount
    val perPersonAmount = if (peopleCount > 0) grandTotal / peopleCount else grandTotal

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgBlack,
        bottomBar = {
            // Save to History premium bottom button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Button(
                    onClick = {
                        if (subtotal <= 0.0) {
                            Toast.makeText(context, "No subtotal to save. Please scan or enter total first.", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.saveSplitRecord {
                                Toast.makeText(context, "Split saved to history!", Toast.LENGTH_SHORT).show()
                                viewModel.setTab(AppTab.HISTORY)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_to_history_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = BgBlack
                    )
                ) {
                    Text(
                        text = "Save to History",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Top Segmented Tab Bar for navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0FFFFFFF), RoundedCornerShape(999.dp))
                        .border(0.5.dp, EdgeBorder, RoundedCornerShape(999.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabPill("Scan", isActive = false) {
                        viewModel.setTab(AppTab.SCAN)
                    }
                    TabPill("Split", isActive = true) {
                        viewModel.setTab(AppTab.SPLIT)
                    }
                    TabPill("History", isActive = false) {
                        viewModel.setTab(AppTab.HISTORY)
                    }
                    TabPill("Settings", isActive = false) {
                        viewModel.setTab(AppTab.SETTINGS)
                    }
                }
            }

            // Subtotal glass display card
            item {
                Column {
                    Text(
                        text = "SUBTOTAL",
                        style = Typography.labelMedium,
                        color = TechnicalMuted,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GlassSurface, RoundedCornerShape(20.dp))
                            .border(0.5.dp, EdgeBorder, RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Base Amount",
                                style = Typography.bodySmall,
                                color = MutedText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${currencySymbol}${String.format(Locale.US, "%.2f", subtotal)}",
                                style = MonospaceDataLarge,
                                color = PureWhite
                            )
                        }
                        
                        // Edit / Override button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x1BFFFFFF))
                                .border(0.5.dp, EdgeBorder, RoundedCornerShape(12.dp))
                                .clickable { viewModel.setTab(AppTab.SCAN) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Adjust",
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // People Counter Panel
            item {
                Column {
                    Text(
                        text = "PEOPLE COUNTER",
                        style = Typography.labelMedium,
                        color = TechnicalMuted,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GlassSurface, RoundedCornerShape(20.dp))
                            .border(0.5.dp, EdgeBorder, RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decrement Button
                        IconButton(
                            onClick = { viewModel.decrementPeople() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0x1BFFFFFF), CircleShape)
                                .border(0.5.dp, EdgeBorder, CircleShape)
                                .testTag("people_decrement_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrement people count",
                                tint = PureWhite
                            )
                        }

                        // Big Monospaced count
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = String.format("%02d", peopleCount),
                                style = MonospaceDataHuge,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (peopleCount == 1) "Person" else "People",
                                style = Typography.bodySmall,
                                color = TechnicalMuted
                            )
                        }

                        // Increment Button
                        IconButton(
                            onClick = { viewModel.incrementPeople() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0x1BFFFFFF), CircleShape)
                                .border(0.5.dp, EdgeBorder, CircleShape)
                                .testTag("people_increment_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increment people count",
                                tint = PureWhite
                            )
                        }
                    }
                }
            }

            // Tip selector chip options
            item {
                Column {
                    Text(
                        text = "CHOOSE TIP PERCENTAGE",
                        style = Typography.labelMedium,
                        color = TechnicalMuted,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Grid logic or simple scrollable Row of chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val staticTips = listOf(0.0, 10.0, 15.0, 18.0)
                        staticTips.forEach { percent ->
                            val isSelected = !isCustomTipSelected && tipPercent == percent
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PureWhite else GlassSurface)
                                    .border(
                                        0.5.dp,
                                        if (isSelected) PureWhite else EdgeBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        isCustomTipSelected = false
                                        viewModel.setTipPercent(percent)
                                    }
                                    .testTag("tip_${percent.toInt()}_chip"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${percent.toInt()}%",
                                    color = if (isSelected) BgBlack else PureWhite,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Custom tip chip
                        val isCustomActive = isCustomTipSelected
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCustomActive) PureWhite else GlassSurface)
                                .border(
                                    0.5.dp,
                                    if (isCustomActive) PureWhite else EdgeBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                    .clickable {
                                        isCustomTipSelected = true
                                        viewModel.setCustomTip(customTipText)
                                    }
                                .testTag("custom_tip_chip"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Custom",
                                color = if (isCustomActive) BgBlack else PureWhite,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Slide down custom input if selected
                    AnimatedVisibility(
                        visible = isCustomTipSelected,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(GlassSurface, RoundedCornerShape(12.dp))
                                    .border(0.5.dp, EdgeBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "%",
                                    style = MonospaceDataLarge,
                                    color = MutedText,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                TextField(
                                    value = customTipText,
                                    onValueChange = { viewModel.setCustomTip(it) },
                                    placeholder = {
                                        Text(
                                            "Enter custom percentage (e.g. 12)",
                                            color = TechnicalMuted,
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 14.sp
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        errorContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        focusedTextColor = PureWhite,
                                        unfocusedTextColor = PureWhite
                                    ),
                                    textStyle = MonospaceDataLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("custom_tip_input")
                                )
                            }
                        }
                    }
                }
            }

            // Real-time calculation summary card
            item {
                Column {
                    Text(
                        text = "BILL SUMMARY",
                        style = Typography.labelMedium,
                        color = TechnicalMuted,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GlassSurfaceActive, RoundedCornerShape(24.dp))
                            .border(0.5.dp, EdgeBorder, RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SummaryLine("Subtotal", "${currencySymbol}${String.format(Locale.US, "%.2f", subtotal)}")
                        SummaryLine("Tip Amount (${tipPercent.toInt()}%)", "${currencySymbol}${String.format(Locale.US, "%.2f", tipAmount)}")
                        SummaryLine("Grand Total", "${currencySymbol}${String.format(Locale.US, "%.2f", grandTotal)}")
                        
                        Divider(color = Color(0x1AFFFFFF), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Per Person Share",
                                    color = PureWhite,
                                    style = Typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Divided by $peopleCount ${if (peopleCount == 1) "person" else "people"}",
                                    color = TechnicalMuted,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                            Text(
                                "${currencySymbol}${String.format(Locale.US, "%.2f", perPersonAmount)}",
                                style = MonospaceDataLarge,
                                color = PureWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("per_person_result")
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = Typography.bodyLarge,
            color = MutedText
        )
        Text(
            text = value,
            style = MonospaceDataMedium,
            color = PureWhite
        )
    }
}
