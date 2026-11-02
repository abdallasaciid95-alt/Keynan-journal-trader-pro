package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = EmeraldProfit,
  onPrimary = Color(0xFF042116),
  primaryContainer = Color(0xFF0B3827),
  onPrimaryContainer = Color(0xFFA1F5D2),

  secondary = GoldAccent,
  onSecondary = Color(0xFF332700),
  secondaryContainer = Color(0xFF473911),
  onSecondaryContainer = Color(0xFFFBE49E),

  tertiary = RedLoss,
  onTertiary = Color(0xFF3B0007),
  tertiaryContainer = Color(0xFF5A0914),
  onTertiaryContainer = Color(0xFFFFD1D6),

  background = JournalBackground,
  onBackground = TextPrimary,

  surface = JournalSurface,
  onSurface = TextPrimary,
  surfaceVariant = JournalSurfaceVariant,
  onSurfaceVariant = TextSecondary,

  outline = JournalBorder,
  outlineVariant = Color(0xFF1B222A),

  error = RedLoss,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
