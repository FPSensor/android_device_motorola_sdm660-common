/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.fpnav

import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.preference.ListPreference
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import lineageos.hardware.LineageHardwareManager
import lineageos.providers.LineageSettings

class FingerprintNavigationFragment : SettingsBasePreferenceFragment() {

    private lateinit var mainSwitch: MainSwitchPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fpnav_settings, rootKey)

        mainSwitch = findPreference(KEY_ENABLED)!!
        mainSwitch.isPersistent = false
        mainSwitch.isChecked = isNavigationEnabled()
        mainSwitch.setOnPreferenceChangeListener { _, newValue ->
            setNavigationEnabled(newValue as Boolean)
            true
        }

        for (key in GESTURE_KEYS) {
            findPreference<ListPreference>(key)?.apply {
                isPersistent = false
                value = Settings.Secure.getInt(
                    requireContext().contentResolver, key, Constants.getDefaultAction(key)
                ).toString()
                setOnPreferenceChangeListener { _, newValue ->
                    putSecureInt(key, (newValue as String).toInt())
                }
            }
        }

        findPreference<SwitchPreferenceCompat>(Constants.KEY_HAPTIC)?.apply {
            isPersistent = false
            isChecked = Settings.Secure.getInt(
                requireContext().contentResolver, Constants.KEY_HAPTIC, 1) != 0
            setOnPreferenceChangeListener { _, newValue ->
                putSecureInt(Constants.KEY_HAPTIC, if (newValue as Boolean) 1 else 0)
            }
        }

        updateDependents(mainSwitch.isChecked)
    }

    private fun setNavigationEnabled(isChecked: Boolean) {
        // Same settings LineageParts writes for its "navigation bar" toggle, so
        // both stay in sync and the state is restored on boot.
        LineageSettings.System.putInt(
            requireContext().contentResolver, LineageSettings.System.FORCE_SHOW_NAVBAR,
            if (isChecked) 0 else 1)
        LineageHardwareManager.getInstance(requireContext())
            .set(LineageHardwareManager.FEATURE_KEY_DISABLE, !isChecked)
        updateDependents(isChecked)
    }

    private fun isNavigationEnabled() = LineageSettings.System.getInt(
        requireContext().contentResolver, LineageSettings.System.FORCE_SHOW_NAVBAR, 1) == 0

    private fun updateDependents(enabled: Boolean) {
        for (key in GESTURE_KEYS + Constants.KEY_HAPTIC) {
            findPreference<androidx.preference.Preference>(key)?.isEnabled = enabled
        }
    }

    private fun putSecureInt(key: String, value: Int) = Settings.Secure.putInt(
        requireContext().contentResolver, key, value)

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            activity?.onBackPressedDispatcher?.onBackPressed()
            true
        } else {
            false
        }
    }

    companion object {
        private const val KEY_ENABLED = "fpnav_enabled"

        private val GESTURE_KEYS = listOf(
            Constants.KEY_TAP,
            Constants.KEY_HOLD,
            Constants.KEY_SWIPE_LEFT,
            Constants.KEY_SWIPE_RIGHT,
        )
    }
}
