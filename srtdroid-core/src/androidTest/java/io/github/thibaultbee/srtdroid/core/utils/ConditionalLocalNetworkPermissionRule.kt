package io.github.thibaultbee.srtdroid.core.utils

import android.Manifest
import android.os.Build
import androidx.test.rule.GrantPermissionRule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class ConditionalLocalNetworkPermissionRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return if (Build.VERSION.SDK_INT >= 37) {
            GrantPermissionRule.grant(Manifest.permission.ACCESS_LOCAL_NETWORK)
                .apply(base, description)
        } else {
            base
        }
    }
}
