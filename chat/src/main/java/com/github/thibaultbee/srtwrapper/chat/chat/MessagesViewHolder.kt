package com.github.thibaultbee.srtwrapper.chat.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.github.thibaultbee.srtwrapper.chat.R
import com.github.thibaultbee.srtwrapper.chat.models.Message

class MessagesViewHolder(
    private val context: Context,
    inflater: LayoutInflater,
    parent: ViewGroup
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.list_message, parent, false
    )
) {
    private var messageView: TextView = itemView.findViewById(R.id.messageBody)
    private var nameView: TextView = itemView.findViewById(R.id.name)


    fun bind(message: Message) {
        messageView.text = message.message
        if (message.amISender) {
            nameView.visibility = View.GONE
            messageView.background = getDrawable(context, R.drawable.list_row_my_message)
        } else {
            nameView.text = message.sender
        }
    }
}