package com.mfqan.creating.kurikulumwhatsappclone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mfqan.creating.kurikulumwhatsappclone.R
import com.mfqan.creating.kurikulumwhatsappclone.model.Message
import kotlinx.android.extensions.LayoutContainer

class ConversationAdapter(private val message : ArrayList<Message>,val userId: String?)
    : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    companion object {
        val MESSAGE_CURRENT_USER = 1 // pesan dari user
        val MESSAGE_OTHER_USER = 2 // pesan dari partner chat user
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        if (viewType == MESSAGE_CURRENT_USER){ // menghubungkan layout item
            return ConversationViewHolder( // ke currentUser sesuai dengan
                LayoutInflater.from(parent.context) // data yang didapat dari Message
                    .inflate(R.layout.item_current_user_message, parent, false))
        } else {
            return ConversationViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_other_user_message, parent, false))
        }
    }

    override fun getItemCount() = message.size

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.inisialisasiItematauview(message[position])
    }

    override fun getItemViewType(position: Int): Int {
        if (message[position].sentBy.equals(userId)){
            return MESSAGE_CURRENT_USER
        } else {
            return MESSAGE_OTHER_USER
        }
    }

    fun addMessage(addmessage: Message){
        message.add(addmessage)
        notifyDataSetChanged()
    }


    class ConversationViewHolder(override val containerView: View?)
        : RecyclerView.ViewHolder(containerView!!),LayoutContainer{
        fun inisialisasiItematauview(message: Message){
            if (containerView != null) {
                containerView.findViewById<TextView>(R.id.txt_message).text = message.message
            }
        }
    }
}