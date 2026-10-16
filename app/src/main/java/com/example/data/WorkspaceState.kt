package com.example.data

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import org.json.JSONObject

/** Small interruption state, separate from committed academic records. */
class WorkspaceState(context: Context) {
    private val prefs = context.getSharedPreferences("evolve_workspace_v2", Context.MODE_PRIVATE)
    val bootCount = Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)
    fun read(key: String): JSONObject? = runCatching { JSONObject(prefs.getString(key, null) ?: return null) }.getOrNull()
    fun write(key: String, value: JSONObject?) {
        val editor = prefs.edit()
        if (value == null) editor.remove(key) else editor.putString(key, value.toString())
        // Called on IO by the ViewModel; commit completes before save/discard is acknowledged.
        check(editor.commit()) { "Unable to preserve workspace state" }
    }
    fun focusActive(): Boolean {
        val state = read("study") ?: return false
        return state.optBoolean("running") && remaining(state, bootCount) > 0
    }
    companion object {
        fun remaining(state: JSONObject, currentBoot: Int = -1): Int {
            if (!state.optBoolean("running")) return state.optInt("seconds", 1500)
            val nowWall = System.currentTimeMillis()
            val nowElapsed = SystemClock.elapsedRealtime()
            val storedBoot = state.optInt("bootCount", -1)
            val sameBoot = if (storedBoot >= 0 && currentBoot >= 0) storedBoot == currentBoot
                else kotlin.math.abs((nowWall - nowElapsed) - state.optLong("bootOffset")) < 5000
            val millis = if (sameBoot) state.optLong("endElapsed") - nowElapsed else state.optLong("endWall") - nowWall
            return ((millis + 999) / 1000).toInt().coerceIn(0, 7200)
        }
    }
}
