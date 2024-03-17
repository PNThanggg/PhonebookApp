package com.app.phonebook.base.compose.lists

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import com.app.phonebook.base.compose.extensions.onEventValue
import com.app.phonebook.base.compose.system_ui_controller.rememberSystemUiController
import com.app.phonebook.base.compose.theme.LocalTheme
import com.app.phonebook.base.compose.theme.SimpleTheme
import com.app.phonebook.base.compose.theme.isNotLitWell
import com.app.phonebook.base.compose.theme.isSurfaceLitWell
import com.app.phonebook.base.compose.theme.model.Theme
import com.app.phonebook.base.extension.getColoredMaterialStatusBarColor
import com.app.phonebook.base.extension.getContrastColor

@Composable
internal fun SystemUISettingsScaffoldStatusBarColor(scrolledColor: Color) {
    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController) {
        systemUiController.statusBarDarkContentEnabled = scrolledColor.isNotLitWell()
        onDispose { }
    }
}

@Composable
internal fun ScreenBoxSettingsScaffold(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SimpleTheme.colorScheme.surface)
            .padding(
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection)
            )
    ) {
        content()
    }
}

@Composable
internal fun statusBarAndContrastColor(context: Context): Pair<Int, Color> {
    val statusBarColor = onEventValue { context.getColoredMaterialStatusBarColor() }
    val contrastColor by remember(statusBarColor) {
        derivedStateOf { Color(statusBarColor.getContrastColor()) }
    }
    return Pair(statusBarColor, contrastColor)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun transitionFractionAndScrolledColor(
    scrollBehavior: TopAppBarScrollBehavior,
    contrastColor: Color,
): Pair<Float, Color> {
    val systemUiController = rememberSystemUiController()
    val colorTransitionFraction = scrollBehavior.state.overlappedFraction
    val scrolledColor = lerp(
        start = if (isSurfaceLitWell()) Color.Black else Color.White,
        stop = contrastColor,
        fraction = if (colorTransitionFraction > 0.01f) 1f else 0f
    )

    systemUiController.setStatusBarColor(
        color = Color.Transparent,
        darkIcons = scrolledColor.isNotLitWell() || (LocalTheme.current is Theme.SystemDefaultMaterialYou && !isSystemInDarkTheme())
    )
    return Pair(colorTransitionFraction, scrolledColor)
}
