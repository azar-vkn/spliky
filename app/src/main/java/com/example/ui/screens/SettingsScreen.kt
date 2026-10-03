package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.SplitViewModel

@Composable
fun SettingsScreen(
    viewModel: SplitViewModel,
    modifier: Modifier = Modifier
) {
    val currentCurrency by viewModel.currencySymbol.collectAsState()
    
    val currencies = listOf(
        Pair("₹", "Indian Rupee (INR)"),
        Pair("$", "US Dollar (USD)"),
        Pair("€", "Euro (EUR)"),
        Pair("£", "British Pound (GBP)"),
        Pair("¥", "Japanese Yen (JPY)"),
        Pair("A$", "Australian Dollar (AUD)"),
        Pair("C$", "Canadian Dollar (CAD)")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgBlack
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
                
                // Segmented Top Tab
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
                    TabPill("History", isActive = false) {
                        viewModel.setTab(AppTab.HISTORY)
                    }
                    TabPill("Settings", isActive = true) {
                        viewModel.setTab(AppTab.SETTINGS)
                    }
                }
            }

            item {
                Text(
                    text = "CURRENCY SETTINGS",
                    style = Typography.labelMedium,
                    color = TechnicalMuted
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassSurface)
                        .border(0.5.dp, EdgeBorder, RoundedCornerShape(20.dp))
                ) {
                    currencies.forEachIndexed { index, (symbol, name) ->
                        val isSelected = symbol == currentCurrency
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setCurrencySymbol(symbol) }
                                .background(if (isSelected) Color(0x1BFFFFFF) else Color.Transparent)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = symbol,
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PureWhite else MutedText,
                                    modifier = Modifier.width(40.dp)
                                )
                                Text(
                                    text = name,
                                    style = Typography.bodyLarge,
                                    color = if (isSelected) PureWhite else MutedText
                                )
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(0xFF4CAF50), RoundedCornerShape(50))
                                )
                            }
                        }
                        
                        if (index < currencies.size - 1) {
                            Divider(color = EdgeBorder, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}
