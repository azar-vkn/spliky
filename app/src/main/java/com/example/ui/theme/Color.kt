package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val BgBlack = Color(0xFF0A0A0A)
val PureWhite = Color(0xFFFFFFFF)
val MutedText = Color(0x99FFFFFF)        // 60% opacity
val TechnicalMuted = Color(0x59FFFFFF)   // 35% opacity
val TechnicalDim = Color(0x4DFFFFFF)     // 30% opacity
val EdgeBorder = Color(0x26FFFFFF)       // 15% opacity
val GlassSurface = Color(0x0FFFFFFF)     // 6% opacity
val GlassSurfaceActive = Color(0x1AFFFFFF) // 10% opacity
val GlassGlow = Color(0x14FFFFFF)         // 8% opacity

val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = PureWhite,
    onPrimary = BgBlack,
    background = BgBlack,
    onBackground = PureWhite,
    surface = BgBlack,
    onSurface = PureWhite,
    outline = EdgeBorder
)
