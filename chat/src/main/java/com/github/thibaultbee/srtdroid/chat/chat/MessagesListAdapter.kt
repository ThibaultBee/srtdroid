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