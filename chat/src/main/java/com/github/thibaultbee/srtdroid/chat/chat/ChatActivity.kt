package com.github.thibaultbee.srtdroid.chat.chat

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.thibaultbee.srtdroid.chat.R
import com.github.thibaultbee.srtdroid.chat.interfaces.SocketManagerInterface
import com.github.thibaultbee.srtdroid.chat.models.Message
import com.github.thibaultbee.srtdroid.chat.singleton.SocketHandler
import com.github.thibaultbee.srtdroid.chat.utils.DialogUtils


class ChatActivity : AppCompatActivity(), SocketManagerInterface {
    private val TAG = ChatActivity::class.qualifiedName

    @BindView(R.id.messageRecyclerView)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.editText)
    lateinit var editText: EditText

    private lateinit var adapter: MessagesListAdapter
    private val messages: MutableList<Message> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        ButterKnife.bind(this)

        adapter = MessagesListAdapter(
            this,
            messages
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        SocketHandler.socketManagerInterface = this
    }

    fun sendMessage(view: View?) {
        val message: String = editText.text.toString()
        editText.text.clear()
        if (message.isNotEmpty()) {
            updateList(message, "me", true)
            SocketHandler.sendMessage(message)
        }
    }

    private fun updateList(message: String, sender: String, amISender: Boolean) {
        messages.add(Message(message, sender, amISender))
        adapter.notifyDataSetChanged();
    }

    override fun onBackPressed() {
        SocketHandler.close()
        super.onBackPressed()
    }

    // SocketManagerInterface
    override fun onRecvMsg(message: String) {
        val peer = SocketHandler.peerName
        var sender: String
        peer.let { sender = "${peer?.address}:${peer?.port}" }
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
