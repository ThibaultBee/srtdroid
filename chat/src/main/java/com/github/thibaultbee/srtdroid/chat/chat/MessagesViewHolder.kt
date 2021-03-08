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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.github.thibaultbee.srtdroid.chat.R
import com.github.thibaultbee.srtdroid.chat.models.Message

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