package com.github.thibaultbee.srtdroid.examples

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class MainClientSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_client, rootKey)
    }
}