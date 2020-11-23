package com.github.thibaultbee.srtdroid.chat.chat

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.thibaultbee.srtdroid.chat.R
import com.github.thibaultbee.srtdroid.chat.databinding.ActivityChatBinding
import com.github.thibaultbee.srtdroid.chat.interfaces.SocketManagerInterface
import com.github.thibaultbee.srtdroid.chat.models.Message
import com.github.thibaultbee.srtdroid.chat.singleton.SocketHandler
import com.github.thibaultbee.srtdroid.chat.utils.DialogUtils


class ChatActivity : AppCompatActivity(), SocketManagerInterface {
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

        SocketHandler.socketManagerInterface = this
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
        val sender = "${peer?.address ?: "Unknown address"}:${peer?.port ?: "Unknown port"}"
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
