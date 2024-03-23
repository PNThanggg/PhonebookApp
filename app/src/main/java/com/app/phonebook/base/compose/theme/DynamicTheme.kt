package com.app.phonebook.base.compose.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.app.phonebook.base.compose.theme.model.Theme
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getProperTextColor
import com.app.phonebook.base.extension.isBlackAndWhiteTheme
import com.app.phonebook.base.extension.isWhiteTheme

fun getTheme(context: Context, materialYouTheme: Theme.SystemDefaultMaterialYou): Theme {
    val baseConfig = context.config
    val primaryColorInt = baseConfig.primaryColor
    val isSystemInDarkTheme = context.isDarkMode()
    val accentColor = baseConfig.accentColor


    val backgroundColorTheme = if (baseConfig.isUsingSystemTheme || baseConfig.isUsingAutoTheme) {
        if (isSystemInDarkTheme) {
            theme_dark_background_color
        } else {
            Color.White
        }
    } else {
        Color(baseConfig.backgroundColor)
    }

    val backgroundColor = backgroundColorTheme.toArgb()
    val textColor = context.getProperTextColor()

    val theme = when {
        baseConfig.isUsingSystemTheme -> materialYouTheme
        context.isBlackAndWhiteTheme() -> Theme.BlackAndWhite(
            accentColor = accentColor,
            primaryColorInt = primaryColorInt,
            backgroundColorInt = backgroundColor,
            textColorInt = textColor
        )

        context.isWhiteTheme() -> Theme.White(
            accentColor = accentColor,
            primaryColorInt = primaryColorInt,
            backgroundColorInt = backgroundColor,
            textColorInt = textColor
        )

        else -> {
            Theme.Custom(
                primaryColorInt = baseConfig.primaryColor,
                backgroundColorInt = backgroundColor,
                textColorInt = textColor
            )
        }
    }

    return theme
}
