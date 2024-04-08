/*
 * Copyright (C) 2021 Thibault B.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.thibaultbee.srtdroid.examples

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.github.thibaultbee.srtdroid.examples.R
import io.github.thibaultbee.srtdroid.Srt
import io.github.thibaultbee.srtdroid.examples.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preferenceClientLayout, MainClientSettingsFragment())
            .replace(R.id.preferenceServerLayout, MainServerSettingsFragment())
            .commit()

        bindProperties()
    }

    private fun bindProperties() {
        binding.testClientButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.launchTestClient()
            } else {
                viewModel.cancelTestClient()
            }
        }

        binding.testServerButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.launchTestServer()
            } else {
                viewModel.cancelTestServer()
            }
        }

        binding.recvFileButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.launchRecvFile()
            } else {
                viewModel.cancelRecvFile()
            }
        }

        binding.sendFileButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.launchSendFile()
            } else {
                viewModel.cancelSendFile()
            }
        }

        viewModel.error.observe(this) {
            Utils.showAlertDialog(this, getString(R.string.Error), it)
        }

        viewModel.success.observe(this) {
            Utils.showAlertDialog(this, getString(R.string.Success), "Noice! $it")
        }

        viewModel.testClientCompletion.observe(this) {
            binding.testClientButton.isChecked = false
        }

        viewModel.testServerCompletion.observe(this) {
            binding.testServerButton.isChecked = false
        }

        viewModel.recvFileCompletion.observe(this) {
            binding.recvFileButton.isChecked = false
        }

        viewModel.sendFileCompletion.observe(this) {
            binding.sendFileButton.isChecked = false
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Srt.cleanUp()
    }
}
