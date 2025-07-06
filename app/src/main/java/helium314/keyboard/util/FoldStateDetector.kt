package helium314.keyboard.util

import android.app.Activity
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.latin.settings.Settings



class FoldStateDetector(
    private val activity: Activity,
    private val onFoldStateChanged: (isFolded: Boolean) -> Unit
) : DefaultLifecycleObserver {

    private val windowInfoTracker = WindowInfoTracker.getOrCreate(activity)

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        CoroutineScope(Dispatchers.Main).launch {
            windowInfoTracker.windowLayoutInfo(activity)
                .collectLatest { layoutInfo: WindowLayoutInfo ->

                    // 🔍 Log the entire feature list for visibility
                    Log.d("FoldTest", "DisplayFeatures: ${layoutInfo.displayFeatures}")

                    // 👇 Determine if device is FOLDED (not flat/unfolded)
                    val treatHalfOpenedAsFolded = activity.prefs()
                        .getBoolean(Settings.PREF_TREAT_HALF_OPENED_AS_FOLDED, true)

                    val foldFeature = layoutInfo.displayFeatures
                        .filterIsInstance<FoldingFeature>()
                        .firstOrNull()

                    val isFolded = when {
                        foldFeature == null -> {
                            Log.d("FoldTest", "No fold feature found — treating as FOLDED")
                            true
                        }
                        foldFeature.state == FoldingFeature.State.HALF_OPENED -> {
                            Log.d("FoldTest", "Fold state: HALF_OPENED, treated as ${if (treatHalfOpenedAsFolded) "FOLDED" else "UNFOLDED"}")
                            treatHalfOpenedAsFolded
                        }
                        foldFeature.state == FoldingFeature.State.FLAT -> {
                            Log.d("FoldTest", "Fold state: FLAT (unfolded)")
                            false
                        }
                        else -> {
                            Log.d("FoldTest", "Fold state: UNKNOWN — treating as FOLDED")
                            true
                        }
                    }


                    Log.d("FoldTest", "Detected fold state change: isFolded = $isFolded")
                    onFoldStateChanged(isFolded)
                }
        }
    }
}
