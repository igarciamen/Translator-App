package com.aitranslator.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aitranslator.app.ui.common.ComingSoonScreen
import com.aitranslator.app.ui.conversation.ConversationScreen
import com.aitranslator.app.ui.history.HistoryScreen
import com.aitranslator.app.ui.liveocr.LiveOcrScreen
import com.aitranslator.app.ui.ocr.OcrScreen
import com.aitranslator.app.ui.translate.TranslateScreen
import com.aitranslator.app.ui.translate.TranslateViewModel
import com.aitranslator.app.ui.dictionary.DictionaryScreen
import com.aitranslator.app.ui.phrases.PhrasesScreen
import com.aitranslator.app.ui.settings.SettingsScreen


@Composable
fun AiTranslatorNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Translate.route,
        modifier = modifier
    ) {
        composable(Screen.Translate.route) {
            TranslateScreen(
                onHistoryClick = { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.History.route) {
            val translateEntry = navController.getBackStackEntry(Screen.Translate.route)
            val translateViewModel: TranslateViewModel = hiltViewModel(translateEntry)

            HistoryScreen(
                onEntryClick = { entry ->
                    translateViewModel.loadFromHistory(
                        sourceText = entry.sourceText,
                        translatedText = entry.translatedText,
                        sourceLanguage = entry.sourceLanguage,
                        targetLanguage = entry.targetLanguage
                    )
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Camera.route) {
            OcrScreen(
                onLiveTranslateClick = { navController.navigate(Screen.LiveOcr.route) }
            )
        }

        composable(Screen.LiveOcr.route) {
            LiveOcrScreen()
        }

        composable(Screen.Conversation.route) {
            ConversationScreen()
        }

        composable(Screen.Dictionary.route) {
            DictionaryScreen()
        }

        composable(Screen.Phrases.route) {
            PhrasesScreen()
        }

        composable(Screen.More.route) {
            SettingsScreen()
        }

        Screen.bottomNavItems
            .filter {
                it != Screen.Translate && it != Screen.Camera &&
                        it != Screen.Conversation && it != Screen.Dictionary &&
                        it != Screen.Phrases && it != Screen.More
            }
            .forEach { screen ->
                composable(screen.route) {
                    ComingSoonScreen(titleRes = screen.labelRes)
                }
            }
    }
}