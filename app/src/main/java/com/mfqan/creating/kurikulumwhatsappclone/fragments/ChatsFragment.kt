package com.mfqan.creating.kurikulumwhatsappclone.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mfqan.creating.kurikulumwhatsappclone.R
import com.mfqan.creating.kurikulumwhatsappclone.activities.ConversationActivity
import com.mfqan.creating.kurikulumwhatsappclone.adapter.ChatsAdapter
import com.mfqan.creating.kurikulumwhatsappclone.listener.ChatsClickListener
import com.mfqan.creating.kurikulumwhatsappclone.listener.FailureCallback
import com.mfqan.creating.kurikulumwhatsappclone.model.Chat
import com.mfqan.creating.kurikulumwhatsappclone.util.DATA_CHATS
import com.mfqan.creating.kurikulumwhatsappclone.util.DATA_USERS
import com.mfqan.creating.kurikulumwhatsappclone.util.DATA_USER_CHATS
import kotlinx.android.synthetic.main.fragment_chats.*

class ChatsFragment : Fragment(), ChatsClickListener {

    private var chatsAdapter = ChatsAdapter(arrayListOf())
    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var failureCallback: FailureCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (userId.isNullOrEmpty()) { // ketika userId kosong maka proses dilanjutkan fungsi
            failureCallback?.onUserError() // onUserError akan memindahkan Activity ke Login
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatsAdapter.setOnItemClickListener(this)
        rv_chats.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = chatsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        firebaseDb.collection(DATA_USERS).document(userId!!)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException == null) {
                    refreshChat()
                }
            }
    }

    fun setFailureCallbackListener(listener: FailureCallback) {
        failureCallback = listener
    }

    override fun onChatClicked(
        chatId: String?,
        otherUserId: String?,
        chatsImageUrl: String?,
        chatsName: String?
    ) {
        startActivity(
            ConversationActivity.newIntent(context, chatId, otherUserId, chatsImageUrl, chatsName)
        )
    }

    private fun refreshChat() {
        firebaseDb.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener {
                if (it.contains(DATA_USER_CHATS)) {
                    val partners = it[DATA_USER_CHATS]
                    val chats = arrayListOf<String>()
                    for (partner in (partners as HashMap<String, String>).keys) {
                        if (partners[partner] != null) {
                            chats.add(partners[partner]!!)
                        }
                    }
                    chatsAdapter.updateChats(chats)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun newChat(partnerID: String) {
        firebaseDb.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener { userDocument ->
                run {
                    val userChatPartners = hashMapOf<String, String>() // menampung data user chat
                    if (userDocument[DATA_USER_CHATS] != null
                        && userDocument[DATA_USER_CHATS] is HashMap<*, *>
                    ) {
                        val userDocumentMap =
                            userDocument[DATA_USER_CHATS] as HashMap<String, String>
                        if (userDocumentMap.containsKey(partnerID)) {
                            return@addOnSuccessListener
                        } else {
                            userChatPartners.putAll(userDocumentMap)
                        }
                    }

                    firebaseDb.collection(DATA_USERS)
                        .document(partnerID)
                        .get()
                        .addOnSuccessListener {
                            val partnerChatPartners = hashMapOf<String, String>()
                            if (it[DATA_USER_CHATS] != null &&
                                it[DATA_USER_CHATS] is HashMap<*, *>
                            ) {
                                val partnerDocumentMap =
                                    it[DATA_USER_CHATS] as HashMap<String, String>
                                partnerChatPartners.putAll(partnerDocumentMap)
                            }
                            val chatParticipants = arrayListOf(userId, partnerID)
                            val chat = Chat(chatParticipants)
                            val chatRef = firebaseDb.collection(DATA_CHATS).document()
                            val userRef = firebaseDb.collection(DATA_USERS).document(userId!!)
                            val partnerRef =
                                firebaseDb.collection(DATA_USERS).document(partnerID)
                            userChatPartners[partnerID] = chatRef.id
                            partnerChatPartners[userId] = chatRef.id
                            val batch = firebaseDb.batch()
                            batch.set(chatRef, chat)
                            batch.update(userRef, DATA_USER_CHATS, userChatPartners)
                            batch.update(partnerRef, DATA_USER_CHATS, partnerChatPartners)
                            batch.commit()
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
    }


}

