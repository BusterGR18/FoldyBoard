/*
// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.preferences

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import helium314.keyboard.keyboard.KeyboardSwitcher
import helium314.keyboard.latin.R
import helium314.keyboard.latin.common.FileUtils
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.Log
import helium314.keyboard.latin.utils.getActivity
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.settings.Setting
import helium314.keyboard.settings.SettingsActivity
import helium314.keyboard.settings.dialogs.ConfirmationDialog
import helium314.keyboard.settings.dialogs.InfoDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Environment
import androidx.compose.runtime.LaunchedEffect
import com.yalantis.ucrop.UCrop
import java.io.File


@Composable
fun BackgroundImagePref(setting: Setting, isLandscape: Boolean) {
    var showDayNightDialog by rememberSaveable { mutableStateOf(false) }
    var showSelectionDialog by rememberSaveable { mutableStateOf(false) }
    var showErrorDialog by rememberSaveable { mutableStateOf(false) }
    var isNight by rememberSaveable { mutableStateOf(false) }
    val ctx = LocalContext.current
    fun getFile() = Settings.getCustomBackgroundFile(ctx, isNight, isLandscape)
    val b = (ctx.getActivity() as? SettingsActivity)?.prefChanged?.collectAsState()
    if ((b?.value ?: 0) < 0) // necessary to reload dayNightPref
        Log.v("irrelevant", "stupid way to trigger recomposition on preference change")
    val dayNightPref = ctx.prefs().getBoolean(Settings.PREF_THEME_DAY_NIGHT, Defaults.PREF_THEME_DAY_NIGHT)
    if (!dayNightPref)
        isNight = false
    val scope = rememberCoroutineScope()
    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { cropResult ->
        if (cropResult.resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(cropResult.data!!)
            if (resultUri != null) {
                scope.launch(Dispatchers.IO) {
                    if (!setBackgroundImage(ctx, resultUri, isNight, isLandscape))
                        showErrorDialog = true
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        showSelectionDialog = false
        showDayNightDialog = false

        val destUri = Uri.fromFile(File(ctx.cacheDir, "cropped_bg.jpg"))
        val options = UCrop.Options().apply {
            setToolbarTitle(ctx.getString(R.string.customize_background_image))
            setToolbarColor(0xFF121212.toInt()) // Dark gray background
            setStatusBarColor(0xFF000000.toInt()) // Black status bar
            setActiveControlsWidgetColor(0xFF00BCD4.toInt()) // Cyan accents
            setToolbarWidgetColor(0xFFFFFFFF.toInt()) // White icons/text
            setFreeStyleCropEnabled(true)
            setShowCropGrid(true)
            setHideBottomControls(false)
        }
        val cropIntent = UCrop.of(uri, destUri)
            .withOptions(options)
            .withAspectRatio(9f, 4f)
            .withMaxResultSize(2000, 1000)
            .getIntent(ctx)
        cropLauncher.launch(cropIntent)

    }

    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        .addCategory(Intent.CATEGORY_OPENABLE)
        .setType("image/*")
    Preference(
        name = setting.title,
        onClick = {
            if (dayNightPref) {
                showDayNightDialog = true
            } else if (!getFile().exists()) {
                launcher.launch(intent)
            } else {
                showSelectionDialog = true
            }
        }
    )
    if (showDayNightDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDayNightDialog = false },
            onConfirmed = {
                isNight = false
                if (getFile().exists())
                    showSelectionDialog = true
                else launcher.launch(intent)
            },
            confirmButtonText = stringResource(R.string.day_or_night_day),
            cancelButtonText = "",
            onNeutral = {
                isNight = true
                if (getFile().exists())
                    showSelectionDialog = true
                else launcher.launch(intent)
            },
            neutralButtonText = stringResource(R.string.day_or_night_night),
            title = { Text(stringResource(R.string.day_or_night_image)) },
        )
    }
    if (showSelectionDialog) {
        ConfirmationDialog(
            onDismissRequest = { showSelectionDialog = false },
            title = { Text(stringResource(R.string.customize_background_image)) },
            confirmButtonText = stringResource(R.string.button_load_custom),
            onConfirmed = { launcher.launch(intent) },
            neutralButtonText = stringResource(R.string.delete),
            onNeutral = {
                getFile().delete()
                Settings.clearCachedBackgroundImages()
                KeyboardSwitcher.getInstance().setThemeNeedsReload()
                showSelectionDialog = false
            }
        )
    }
    if (showErrorDialog) {
        InfoDialog(stringResource(R.string.file_read_error)) { showErrorDialog = false }
    }
}

private fun setBackgroundImage(ctx: Context, uri: Uri, isNight: Boolean, isLandscape: Boolean): Boolean {
    val imageFile = Settings.getCustomBackgroundFile(ctx, isNight, isLandscape)
    Log.d("CustomBg", "Saving background to: ${imageFile.absolutePath}")
    FileUtils.copyContentUriToNewFile(uri, ctx, imageFile)
    Log.d("CustomBg", "File copied, reloading theme")
    KeyboardSwitcher.getInstance().setThemeNeedsReload()
    return try {
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        Log.d("CustomBg", "Decoded image dimensions: ${bitmap?.width}x${bitmap?.height}")
        Settings.clearCachedBackgroundImages()
        true
    } catch (e: Exception) {
        Log.e("CustomBg", "Failed to decode image: ${e.message}")
        imageFile.delete()
        false
    }
}

*/



 */


package helium314.keyboard.settings.preferences

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.yalantis.ucrop.UCrop
import helium314.keyboard.keyboard.KeyboardSwitcher
import helium314.keyboard.latin.R
import helium314.keyboard.latin.common.FileUtils
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.Log
import helium314.keyboard.latin.utils.getActivity
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.settings.Setting
import helium314.keyboard.settings.SettingsActivity
import helium314.keyboard.settings.dialogs.ConfirmationDialog
import helium314.keyboard.settings.dialogs.InfoDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun BackgroundImagePref(setting: Setting, isLandscape: Boolean) {
    var showDayNightDialog by rememberSaveable { mutableStateOf(false) }
    var showSelectionDialog by rememberSaveable { mutableStateOf(false) }
    var showErrorDialog by rememberSaveable { mutableStateOf(false) }
    var isNight by rememberSaveable { mutableStateOf(false) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    fun getFile() = Settings.getCustomBackgroundFile(ctx, isNight, isLandscape)

    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = UCrop.getOutput(result.data!!)
            if (uri != null) {
                scope.launch(Dispatchers.IO) {
                    if (!setBackgroundImage(ctx, uri, isNight, isLandscape)) showErrorDialog = true
                }
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            Log.e("CustomBg", "Crop error: ${UCrop.getError(result.data!!)}")
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult

        val destinationUri = Uri.fromFile(File(ctx.cacheDir, "cropped_background.jpg"))

        val cropIntent = UCrop.of(uri, destinationUri)
            .withAspectRatio(9f, 4f)
            .withMaxResultSize(2000, 1000)
            .getIntent(ctx)

        cropLauncher.launch(cropIntent)
    }

    val b = (ctx.getActivity() as? SettingsActivity)?.prefChanged?.collectAsState()
    if ((b?.value ?: 0) < 0)
        Log.v("irrelevant", "trigger recomposition on preference change")

    val dayNightPref = ctx.prefs().getBoolean(Settings.PREF_THEME_DAY_NIGHT, Defaults.PREF_THEME_DAY_NIGHT)
    if (!dayNightPref) isNight = false

    Preference(
        name = setting.title,
        onClick = {
            if (dayNightPref) showDayNightDialog = true
            else if (!getFile().exists()) imagePickerLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            })
            else showSelectionDialog = true
        }
    )

    if (showDayNightDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDayNightDialog = false },
            onConfirmed = {
                isNight = false
                if (getFile().exists()) showSelectionDialog = true
                else imagePickerLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                })
            },
            confirmButtonText = stringResource(R.string.day_or_night_day),
            cancelButtonText = "",
            onNeutral = {
                isNight = true
                if (getFile().exists()) showSelectionDialog = true
                else imagePickerLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                })
            },
            neutralButtonText = stringResource(R.string.day_or_night_night),
            title = { Text(stringResource(R.string.day_or_night_image)) },
        )
    }

    if (showSelectionDialog) {
        ConfirmationDialog(
            onDismissRequest = { showSelectionDialog = false },
            title = { Text(stringResource(R.string.customize_background_image)) },
            confirmButtonText = stringResource(R.string.button_load_custom),
            onConfirmed = {
                imagePickerLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                })
            },
            neutralButtonText = stringResource(R.string.delete),
            onNeutral = {
                getFile().delete()
                Settings.clearCachedBackgroundImages()
                KeyboardSwitcher.getInstance().setThemeNeedsReload()
                showSelectionDialog = false
            }
        )
    }

    if (showErrorDialog) {
        InfoDialog(stringResource(R.string.file_read_error)) { showErrorDialog = false }
    }






}

private fun setBackgroundImage(ctx: Context, uri: Uri, isNight: Boolean, isLandscape: Boolean): Boolean {
    val imageFile = Settings.getCustomBackgroundFile(ctx, isNight, isLandscape)
    FileUtils.copyContentUriToNewFile(uri, ctx, imageFile)
    KeyboardSwitcher.getInstance().setThemeNeedsReload()
    return try {
        BitmapFactory.decodeFile(imageFile.absolutePath)
        Settings.clearCachedBackgroundImages()
        true
    } catch (_: Exception) {
        imageFile.delete()
        false
    }
}


@Composable
fun AddOpacitySlider(setting: Setting, isLandscape: Boolean, isNight: Boolean, label: String) {
    val ctx = LocalContext.current
    val sharedPrefs = ctx.prefs()
    val key = setting.key
    var opacity by rememberSaveable {
        mutableStateOf(sharedPrefs.getFloat(key, 1.0f))
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = "$label: ${(opacity * 100).toInt()}%")
        Slider(
            value = opacity,
            onValueChange = {
                opacity = it
                sharedPrefs.edit().putFloat(key, opacity).apply()
                Settings.clearCachedBackgroundImages()
                KeyboardSwitcher.getInstance().setThemeNeedsReload()
            },
            valueRange = 0f..1f
        )
    }
}

@Composable
fun AddBrightnessSlider(setting: Setting, isLandscape: Boolean, isNight: Boolean) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("inputmethod", Context.MODE_PRIVATE)

    val key = "background_brightness_" + (if (isNight) "night" else "day") + "_" + (if (isLandscape) "land" else "port")

    var brightness by rememberSaveable(key) {
        mutableStateOf(prefs.getFloat(key, 1.0f))
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = "Brightness: ${(brightness * 100).toInt()}%")
        Slider(
            value = brightness.coerceIn(0f, 1f),
            onValueChange = {
                brightness = it
                prefs.edit().putFloat(key, brightness).apply()
                Log.d("BrightnessDebug", "Saved $brightness to key = $key")

                Settings.clearCachedBackgroundImages()
                KeyboardSwitcher.getInstance().setThemeNeedsReload()
            },
            valueRange = 0f..1f
        )
    }
}
