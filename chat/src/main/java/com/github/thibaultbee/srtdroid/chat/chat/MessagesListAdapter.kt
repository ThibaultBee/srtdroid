package com.github.thibaultbee.srtdroid.chat.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.thibaultbee.srtdroid.chat.models.Message

class MessagesListAdapter(private val context: Context, private val messages: List<Message>) :
    RecyclerView.Adapter<MessagesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MessagesViewHolder(
            context,
            inflater,
            parent
        )
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        val message: Message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size
}