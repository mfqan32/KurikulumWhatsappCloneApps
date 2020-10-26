package com.mfqan.creating.kurikulumwhatsappclone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mfqan.creating.kurikulumwhatsappclone.R
import com.mfqan.creating.kurikulumwhatsappclone.listener.ChatsClickListener
import com.mfqan.creating.kurikulumwhatsappclone.model.User
import com.mfqan.creating.kurikulumwhatsappclone.util.DATA_CHATS
import com.mfqan.creating.kurikulumwhatsappclone.util.DATA_CHAT_PARTICIPANTS
import com.mfqan.creating.kurikulumwhatsappclone.util.DATA_USERS
import com.mfqan.creating.kurikulumwhatsappclone.util.populateImage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_chats.*

class ChatsAdapter(private val chats: ArrayList<String>) :
    RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    private var chatsClickListener: ChatsClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ChatsViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_chats,
                    parent, false
                )
        )

    override fun getItemCount() = chats.size

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.bindItem(chats[position], chatsClickListener)
    }

    fun setOnItemClickListener(listener: ChatsClickListener) {
        chatsClickListener = listener
        notifyDataSetChanged()
    }

    fun updateChats(updatedChats: ArrayList<String>) {
        chats.clear()
        chats.addAll(updatedChats)
        notifyDataSetChanged()
    }

    class ChatsViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        private val firebaseDb = FirebaseFirestore.getInstance()
        private val userId = FirebaseAuth.getInstance().currentUser?.uid
        private var partnerId: String? = null
        private var chatName: String? = null
        private var chatImageUrl: String? = null

        fun bindItem(chatId: String, listener: ChatsClickListener?) {
            // menghubungkan gambar dengan ImageView, jika terjadi error gambar diset ic_user
            populateImage(img_chats.context, "", img_chats, R.drawable.ic_user)
            txt_chats.text = chatId

            firebaseDb.collection(DATA_CHATS)
                .document(chatId)
                .get()
                .addOnSuccessListener { it ->
                    val chatParticipants = it[DATA_CHAT_PARTICIPANTS] // jika data dalam
                    if (chatParticipants != null) {
                        for (participant in chatParticipants as ArrayList<String?>) {
                            if (participant != null && participant != userId){
                                partnerId = participant // menampung data partisipan sesuai userId
                                firebaseDb.collection(DATA_USERS)
                                    .document(partnerId!!)
                                    .get()
                                    .addOnSuccessListener {
                                        val user = it.toObject(User::class.java) // menampung data table User
                                        chatImageUrl = user?.imageUrl // menampung data image
                                        chatName = user?.name // menampung data nama

                                        txt_chats.text = user?.name // memasangkan ke TextView
                                        populateImage(
                                            img_chats.context,
                                            user?.imageUrl,
                                            img_chats,
                                            R.drawable. ic_user) // memasangkan ke ImageView
                                        progress_layout_chats.visibility = View.GONE
                                    } .addOnFailureListener {
                                        it.printStackTrace()
                                        progress_layout_chats.visibility = View.GONE
                                    }
                            }
                        }
                    }
                    progress_layout_chats.visibility = View.GONE
                }.addOnFailureListener {
                    it.printStackTrace()
                    progress_layout_chats.visibility = View.GONE
                }
            itemView.setOnClickListener {
                listener?.onChatClicked(chatId, userId, chatImageUrl, chatName)
            }
        }
    }
}