package com.app.phonebook.base.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import com.app.phonebook.base.compose.theme.model.Theme
import com.app.phonebook.base.compose.theme.model.Theme.Companion.systemDefaultMaterialYou

@Composable
internal fun Theme(
    theme: Theme = systemDefaultMaterialYou(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current

    val colorScheme = if (!view.isInEditMode) {
        when (theme) {
            is Theme.Custom, is Theme.Dark -> darkColorScheme(
                primary = theme.primaryColor, surface = theme.backgroundColor, onSurface = theme.textColor
            )

            is Theme.White -> darkColorScheme(
                primary = Color(theme.accentColor),
                surface = theme.backgroundColor,
                tertiary = theme.primaryColor,
                onSurface = theme.textColor,
            )

            is Theme.BlackAndWhite -> darkColorScheme(
                primary = Color(theme.accentColor),
                surface = theme.backgroundColor,
                tertiary = theme.primaryColor,
                onSurface = theme.textColor
            )

            else -> darkColorScheme
        }
    } else {
        previewColorScheme()
    }

    val dimensions = CommonDimensions

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes,
        content = {
            CompositionLocalProvider(
                LocalRippleTheme provides DynamicThemeRipple, LocalTheme provides theme, LocalDimensions provides dimensions
            ) {
                content()
            }
        },
    )
}

val LocalTheme: ProvidableCompositionLocal<Theme> = staticCompositionLocalOf {
    Theme.Custom(
        1,
        1,
        1,
    )
}

@Composable
private fun previewColorScheme() = if (isSystemInDarkTheme()) {
    darkColorScheme
} else {
    lightColorScheme
}

