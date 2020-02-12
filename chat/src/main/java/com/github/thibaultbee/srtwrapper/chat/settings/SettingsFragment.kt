package com.github.thibaultbee.srtwrapper.chat.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.github.thibaultbee.srtwrapper.chat.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}