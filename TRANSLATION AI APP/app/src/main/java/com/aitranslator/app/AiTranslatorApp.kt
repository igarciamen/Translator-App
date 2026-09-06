package com.aitranslator.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.aitranslator.app.domain.settings.AppTheme
import com.aitranslator.app.navigation.AiTranslatorBottomBar
import com.aitranslator.app.navigation.AiTranslatorNavHost
import com.aitranslator.app.ui.theme.AiTranslatorTheme

/**
 * Takes the current theme as a parameter, read once by MainActivity
 * directly from SettingsRepository — not through a hiltViewModel() call
 * here, which would create a *second*, separate SettingsViewModel
 * instance from the one the Settings screen itself uses (each
 * hiltViewModel() call site gets its own instance unless explicitly
 * scoped to a shared owner). Two independent instances observing the
 * same DataStore were found to briefly disagree with each other right
 * after a theme change, producing mismatched text/background colors.
 * A single read at the activity root avoids that duplication entirely.
 */
@Composable
fun AiTranslatorApp(theme: AppTheme = AppTheme.SYSTEM_DEFAULT) {
    val navController = rememberNavController()

    AiTranslatorTheme(appTheme = theme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                bottomBar = { AiTranslatorBottomBar(navController = navController) }
            ) { innerPadding ->
                AiTranslatorNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AiTranslatorAppPreview() {
    AiTranslatorApp()
}