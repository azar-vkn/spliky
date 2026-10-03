package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.screens.SplitScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.SplitViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SplitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: SplitViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgBlack,
        topBar = {
            // Elegant top glass bar mimicking the HTML design
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x990A0A0A))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Spliky",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "fair splits, zero drama",
                            color = TechnicalMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    // Luxury header action icons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Wallet",
                            tint = PureWhite,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { /* purely aesthetic */ }
                        )
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = PureWhite,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { viewModel.setTab(AppTab.SETTINGS) }
                        )
                    }
                }
            }
        },
        bottomBar = {
            // BottomNavBar Shell matching the HTML structure exactly
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x990A0A0A))
                    .navigationBarsPadding()
                    .border(0.5.dp, Color(0x1BFFFFFF))
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavTabItem(
                        label = "Scan",
                        icon = Icons.Default.CameraAlt,
                        isActive = currentTab == AppTab.SCAN,
                        testTag = "bottom_tab_scan"
                    ) {
                        viewModel.setTab(AppTab.SCAN)
                    }

                    BottomNavTabItem(
                        label = "Split",
                        icon = Icons.Default.DeviceHub,
                        isActive = currentTab == AppTab.SPLIT,
                        testTag = "bottom_tab_split"
                    ) {
                        viewModel.setTab(AppTab.SPLIT)
                    }

                    BottomNavTabItem(
                        label = "History",
                        icon = Icons.Default.History,
                        isActive = currentTab == AppTab.HISTORY,
                        testTag = "bottom_tab_history"
                    ) {
                        viewModel.setTab(AppTab.HISTORY)
                    }
                }
            }
        }
    ) { innerPadding ->
        // Animated Tab content switching for smooth premium transitions
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.SCAN -> ScanScreen(viewModel = viewModel)
                AppTab.SPLIT -> SplitScreen(viewModel = viewModel)
                AppTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BottomNavTabItem(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isActive) Color(0x1EFFFFFF) else Color.Transparent,
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) PureWhite else MutedText,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isActive) PureWhite else TechnicalMuted,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.SansSerif
        )
    }
}
