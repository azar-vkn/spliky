package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.ScanProcessState
import com.example.ui.viewmodel.SplitViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: SplitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val scannedUri by viewModel.scannedImageUri.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val detectedItems by viewModel.detectedItems.collectAsState()
    val manualTotal by viewModel.manualTotal.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var showSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Helper to request camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempCameraUri = createTempImageUri(context)
            tempCameraUri?.let { uri ->
                // Wait, take picture launcher signature
            }
        } else {
            Toast.makeText(context, "Camera permission is required to scan bills", Toast.LENGTH_SHORT).show()
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { uri ->
                viewModel.performTextRecognition(context, uri)
            }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.performTextRecognition(context, uri)
        }
    }

    // Launch flow
    fun launchCamera() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            val uri = createTempImageUri(context)
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(permission)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgBlack,
        bottomBar = {
            // "Use This Amount" Fixed Bottom Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Button(
                    onClick = {
                        if (viewModel.activeSubtotal > 0) {
                            viewModel.setTab(AppTab.SPLIT)
                        } else {
                            Toast.makeText(context, "Please enter or scan a bill amount", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("use_amount_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = BgBlack
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = "Use ${currencySymbol}${String.format(java.util.Locale.US, "%.2f", viewModel.activeSubtotal)}",
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
                
                // Segmented Top Tab (Purely visual to mimic HTML and allow switching)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0FFFFFFF), RoundedCornerShape(999.dp))
                        .border(0.5.dp, EdgeBorder, RoundedCornerShape(999.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabPill("Scan", isActive = true) {
                        viewModel.setTab(AppTab.SCAN)
                    }
                    TabPill("Split", isActive = false) {
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

            // Main Scan Area
            item {
                if (scannedUri != null) {
                    // Display image cropped beautifully in a rounded card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(0.5.dp, EdgeBorder, RoundedCornerShape(20.dp))
                            .background(GlassSurface)
                            .clickable { showSourceDialog = true }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = scannedUri),
                            contentDescription = "Scanned bill",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x40000000))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(Color(0x99000000), RoundedCornerShape(30.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { showSourceDialog = true },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = PureWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Retake Bill",
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    val infiniteTransition = rememberInfiniteTransition()
                    val phase by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 40f, 
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )
                    val glowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.05f,
                        targetValue = 0.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    // Liquid Glass dotted-dashed rectangle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .drawBehind {
                                val stroke = Stroke(
                                    width = 1.5f * density,
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(10f * density, 10f * density),
                                        phase
                                    )
                                )
                                drawRoundRect(
                                    color = Color(0x66FFFFFF),
                                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                                    style = stroke
                                )
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x0AFFFFFF).copy(alpha = glowAlpha))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0x33FFFFFF), Color.Transparent),
                                    radius = 300f
                                )
                            )
                            .clickable { showSourceDialog = true }
                            .testTag("scan_trigger_area"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0x1BFFFFFF), CircleShape)
                                    .border(0.5.dp, Color(0x33FFFFFF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera Icon",
                                    tint = PureWhite,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Tap to scan your bill",
                                color = MutedText,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // OCR States representation
            item {
                AnimatedVisibility(
                    visible = scanState is ScanProcessState.Processing,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GlassSurface, RoundedCornerShape(12.dp))
                            .border(0.5.dp, EdgeBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = PureWhite, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Analyzing layout & extracting prices...",
                            color = MutedText,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                AnimatedVisibility(
                    visible = scanState is ScanProcessState.Error,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val errorState = scanState as? ScanProcessState.Error
                    Text(
                        text = errorState?.message ?: "Extraction failed",
                        color = Color(0xFFFF4D4D),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Display Detected Items List
            if (detectedItems.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "DETECTED ITEMS",
                            style = Typography.labelMedium,
                            color = TechnicalMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            detectedItems.forEach { receiptItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(GlassSurface, RoundedCornerShape(12.dp))
                                        .border(0.5.dp, EdgeBorder, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = receiptItem.name,
                                        style = Typography.bodyLarge,
                                        color = PureWhite,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${currencySymbol}${String.format(java.util.Locale.US, "%.2f", receiptItem.price)}",
                                        style = MonospaceDataMedium,
                                        color = PureWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Manual Input Section
            item {
                Column {
                    Text(
                        text = "OR ENTER MANUALLY",
                        style = Typography.labelMedium,
                        color = TechnicalMuted,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GlassSurface, RoundedCornerShape(12.dp))
                            .border(0.5.dp, EdgeBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currencySymbol,
                            style = MonospaceDataLarge,
                            color = MutedText,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        TextField(
                            value = manualTotal,
                            onValueChange = { viewModel.setManualTotal(it) },
                            placeholder = {
                                Text(
                                    "Total bill amount ($currencySymbol)",
                                    color = TechnicalMuted,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 16.sp
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
                                .testTag("manual_total_input")
                        )
                    }
                }
            }

            // High aesthetic moody shot mimicking HTML
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassSurface)
                        .border(0.5.dp, EdgeBorder, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Dark receipt styling graphic using Canvas
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        // Draw vertical aesthetic billing lines
                        drawLine(
                            color = Color(0x1BFFFFFF),
                            start = Offset(size.width * 0.15f, 0f),
                            end = Offset(size.width * 0.15f, size.height),
                            pathEffect = pathEffect,
                            strokeWidth = 1f
                        )
                    }
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "UTILITY SCANNER engine",
                            style = Typography.labelMedium,
                            color = TechnicalMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Optimized for thermal receipts, dining receipts, and invoices with rupee/INR notation parsing.",
                            style = Typography.bodySmall,
                            color = MutedText,
                            lineHeight = 20.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal dialogue of camera source Selection
    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            containerColor = BgBlack,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(0.5.dp, EdgeBorder, RoundedCornerShape(20.dp)),
            title = {
                Text(
                    text = "Capture Bill receipt",
                    style = Typography.headlineMedium,
                    color = PureWhite
                )
            },
            text = {
                Text(
                    text = "Select camera to take an instant photo, or browse from local photo gallery library.",
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
                        onClick = {
                            showSourceDialog = false
                            launchCamera()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassSurfaceActive,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, EdgeBorder)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Camera", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            showSourceDialog = false
                            pickImageLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassSurfaceActive,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, EdgeBorder)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery", fontSize = 14.sp)
                    }
                }
            }
        )
    }
}

@Composable
fun RowScope.TabPill(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (isActive) Color(0x22FFFFFF) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isActive) PureWhite else MutedText,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            style = Typography.labelMedium
        )
    }
}

// Generate temp URI
fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile("spliky_scan_", ".jpg", context.cacheDir).apply {
        deleteOnExit()
    }
    return FileProvider.getUriForFile(
        context,
        "com.aistudio.spliky.kdpmrx.fileprovider",
        tempFile
    )
}
