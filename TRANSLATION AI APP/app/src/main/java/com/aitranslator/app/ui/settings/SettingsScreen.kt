package com.aitranslator.app.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aitranslator.app.R
import com.aitranslator.app.domain.settings.AppLanguage
import com.aitranslator.app.domain.settings.AppTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    SettingsScreenContent(
        uiState = uiState,
        onLanguageSelected = { language ->
            viewModel.onLanguageSelected(language)
            applyAppLanguage(context, language)
        },
        onThemeSelected = viewModel::onThemeSelected,
        modifier = modifier
    )
}

/**
 * Applies a per-app language change directly, without relying on
 * AppCompatDelegate — MainActivity extends plain ComponentActivity (this
 * project uses no AppCompatActivity anywhere), and setApplicationLocales
 * only auto-applies/recreates on an AppCompatActivity. On Android 13+,
 * the framework's own LocaleManager is used instead (no AppCompat
 * dependency needed); on older versions, per-app language isn't
 * available as a system feature, so the app's default Locale is set
 * directly and the Activity is recreated manually so newly-inflated
 * resources pick it up.
 */
private fun applyAppLanguage(context: android.content.Context, language: com.aitranslator.app.domain.settings.AppLanguage) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
        localeManager.applicationLocales = if (language.localeTag != null) {
            android.os.LocaleList.forLanguageTags(language.localeTag)
        } else {
            android.os.LocaleList.getEmptyLocaleList()
        }
    } else {
        val locale = if (language.localeTag != null) {
            java.util.Locale.forLanguageTag(language.localeTag)
        } else {
            java.util.Locale.getDefault()
        }
        java.util.Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        (context as? android.app.Activity)?.recreate()
    }
}

@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        SettingsSection(
            titleRes = R.string.settings_language_section,
            options = AppLanguage.entries,
            selected = uiState.language,
            onSelected = onLanguageSelected
        )

        Text(
            text = stringResource(R.string.settings_language_restart_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SettingsSection(
            titleRes = R.string.settings_theme_section,
            options = AppTheme.entries,
            selected = uiState.theme,
            onSelected = onThemeSelected
        )
    }
}

private interface LabeledOption {
    val labelRes: Int
}

@Composable
private fun <T> SettingsSection(
    titleRes: Int,
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit
) where T : Enum<T> {
    Column {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        options.forEach { option ->
            val labelRes = when (option) {
                is AppLanguage -> option.labelRes
                is AppTheme -> option.labelRes
                else -> return@forEach
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = option == selected, onClick = { onSelected(option) })
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = option == selected, onClick = { onSelected(option) })
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}