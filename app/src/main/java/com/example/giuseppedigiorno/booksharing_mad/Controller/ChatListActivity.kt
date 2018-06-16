package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.giuseppedigiorno.booksharing_mad.Model.ChatItem
import com.example.giuseppedigiorno.booksharing_mad.R
import com.example.giuseppedigiorno.booksharing_mad.ViewHolder.ChatHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ChatListActivity : AppCompatActivity() {

    lateinit var mRecyclerView: RecyclerView
    lateinit var mDatabase: DatabaseReference
    lateinit var currentUserId: String
    lateinit var currentUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        currentUserId = intent.getStringExtra("currentUserId")
        currentUserName = intent.getStringExtra("currentUserName")

        mDatabase = FirebaseDatabase.getInstance().reference
                .child("chats").child(currentUserId)

        mRecyclerView = findViewById(R.id.chatListRecyclerView)
        var layoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.setHasFixedSize(true)
        loadChatData()
    }

    private fun loadChatData() {
        val options = FirebaseRecyclerOptions.Builder<ChatItem>()
                .setQuery(mDatabase, ChatItem::class.java)
                .setLifecycleOwner(this)
                .build()

        val adapter = object : FirebaseRecyclerAdapter<ChatItem, ChatHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
                return ChatHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_chat, parent, false))
            }

            override fun onBindViewHolder(holder: ChatHolder, position: Int, model: ChatItem) {
                holder.bindChatItem(model)
                var userId = model.userId!!.toString()
                var bookTitle = model.bookTitle!!.toString()
                var userName = model.userName!!.toString()
                holder.customView.setOnClickListener { startMessageActivity(this@ChatListActivity, currentUserId, currentUserName, userId, userName, bookTitle) } }
            }
        mRecyclerView.adapter = adapter
    }

    private fun startMessageActivity(context: Context, currentUserId: String, currentUserName: String, userId: String, userName: String, bookTitle: String){
        var chatActivity = Intent(context, ChatActivity::class.java)
        chatActivity.putExtra("currentUserId", currentUserId)
        chatActivity.putExtra("currentUserName", currentUserName)
        chatActivity.putExtra("userId", userId)
        chatActivity.putExtra("userName", userName)
        chatActivity.putExtra("bookTitle", bookTitle)
        startActivity(chatActivity)
    }
}
