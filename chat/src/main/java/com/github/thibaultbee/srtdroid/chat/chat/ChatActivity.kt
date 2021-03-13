/*
 * Copyright (C) 2021 Thibault Beyou
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
package com.github.thibaultbee.srtdroid.chat.chat

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.thibaultbee.srtdroid.chat.R
import com.github.thibaultbee.srtdroid.chat.databinding.ActivityChatBinding
import com.github.thibaultbee.srtdroid.chat.interfaces.SocketHandlerListener
import com.github.thibaultbee.srtdroid.chat.models.Message
import com.github.thibaultbee.srtdroid.chat.singleton.SocketHandler
import com.github.thibaultbee.srtdroid.chat.utils.DialogUtils


class ChatActivity : AppCompatActivity(), SocketHandlerListener {
    private val TAG = ChatActivity::class.qualifiedName
    private lateinit var binding: ActivityChatBinding

    private lateinit var adapter: MessagesListAdapter
    private val messages: MutableList<Message> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MessagesListAdapter(
            this,
            messages
        )
        binding.messageRecyclerView.adapter = adapter
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)

        SocketHandler.socketHandlerListener = this
    }

    fun sendMessage(view: View?) {
        val message: String = binding.editText.text.toString()
        binding.editText.text.clear()
        if (message.isNotEmpty()) {
            updateList(message, "me", true)
            SocketHandler.sendMessage(message)
        }
    }

    private fun updateList(message: String, sender: String, amISender: Boolean) {
        messages.add(Message(message, sender, amISender))
        adapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        SocketHandler.close()
        super.onBackPressed()
    }

    // SocketManagerInterface
    override fun onRecvMsg(message: String) {
        val peer = SocketHandler.peerName
        val sender = "${peer.address}:${peer.port}"
        this.runOnUiThread { updateList(message, sender, false) }
    }

    override fun onConnectionClose(reason: String) {
        this.runOnUiThread {
            DialogUtils.showAlertCloseActivity(
                this,
                getString(R.string.connection_error),
                reason
            )
        }
    }
}
