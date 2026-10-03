package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SplitRecord
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.SplitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: SplitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val historyList by viewModel.allHistoryRecords.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgBlack,
        bottomBar = {
            // "Clear All" Bottom button if there are items
            if (historyList.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Button(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("clear_all_history_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF141313),
                            contentColor = Color(0xFFFF4D4D)
                        ),
                        border = BorderStroke(0.5.dp, Color(0x4DFF4D4D))
                    ) {
                        Text(
                            text = "Clear All Splits",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Top Tab row
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
                    TabPill("Split", isActive = false) {
                        viewModel.setTab(AppTab.SPLIT)
                    }
                    TabPill("History", isActive = true) {
                        viewModel.setTab(AppTab.HISTORY)
                    }
                    TabPill("Settings", isActive = false) {
                        viewModel.setTab(AppTab.SETTINGS)
                    }
                }
            }

            if (historyList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillParentMaxHeight(0.65f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(Color(0x0FFFFFFF), RoundedCornerShape(20.dp))
                                .border(0.5.dp, EdgeBorder, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = "History Icon",
                                tint = TechnicalMuted,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No saved splits yet",
                            color = PureWhite,
                            style = Typography.headlineMedium,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Save your split records from the split summary card to access them here instantly.",
                            color = TechnicalMuted,
                            style = Typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "SAVED SPLITS",
                        style = Typography.labelMedium,
                        color = TechnicalMuted,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                items(historyList, key = { it.id }) { record ->
                    HistoryCard(
                        record = record,
                        currencySymbol = currencySymbol,
                        onDelete = { viewModel.deleteRecord(record.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Confirmation dialog for clearing all records
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = BgBlack,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(0.5.dp, EdgeBorder, RoundedCornerShape(20.dp)),
            title = {
                Text(
                    text = "Clear All Splits?",
                    style = Typography.headlineMedium,
                    color = PureWhite
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete all split history records? This action cannot be undone.",
                    style = Typography.bodyLarge,
                    color = MutedText
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showClearConfirmDialog = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassSurfaceActive,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, EdgeBorder)
                    ) {
                        Text("Cancel", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            showClearConfirmDialog = false
                            viewModel.clearAllRecords()
                            Toast.makeText(context, "All splits cleared!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF4D4D),
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Clear All", fontSize = 14.sp)
                    }
                }
            }
        )
    }
}

@Composable
fun HistoryCard(
    record: SplitRecord,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM d, yyyy  •  h:mm a", Locale.getDefault()) }
    val formattedDate = remember(record.timestamp) { formatter.format(Date(record.timestamp)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassSurface)
            .border(0.5.dp, EdgeBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("history_card_${record.id}")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Date + Trash button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = Typography.labelMedium,
                    color = TechnicalMuted,
                    fontSize = 11.sp
                )
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0x14FFFFFF), CircleShape)
                        .testTag("delete_button_${record.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete record",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Results summary Grid (Total split info)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "PER PERSON SHARE",
                        style = Typography.labelMedium,
                        color = TechnicalDim,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${currencySymbol}${String.format(Locale.US, "%.2f", record.perPersonAmount)}",
                        style = MonospaceDataLarge,
                        color = PureWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "PEOPLE",
                            style = Typography.labelMedium,
                            color = TechnicalDim,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${record.peopleCount}",
                            style = MonospaceDataMedium,
                            color = MutedText
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "TIP",
                            style = Typography.labelMedium,
                            color = TechnicalDim,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${record.tipPercent.toInt()}%",
                            style = MonospaceDataMedium,
                            color = MutedText
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "TOTAL",
                            style = Typography.labelMedium,
                            color = TechnicalDim,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${currencySymbol}${record.grandTotal.toInt()}",
                            style = MonospaceDataMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Optional items list scanned representation
            val itemList = record.items
            if (itemList.isNotEmpty()) {
                Divider(color = Color(0x10FFFFFF), thickness = 0.5.dp)
                Text(
                    text = "Parsed receipt scan saved: ${itemList.size} items",
                    fontSize = 11.sp,
                    color = TechnicalMuted,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}
