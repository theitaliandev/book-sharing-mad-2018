package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.app.ActionBar
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RatingBar
import com.example.giuseppedigiorno.booksharing_mad.Model.MessageItem
import com.example.giuseppedigiorno.booksharing_mad.R
import com.example.giuseppedigiorno.booksharing_mad.ViewHolder.MessageHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_show_profile.*
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.linearLayoutCompat
import org.jetbrains.anko.custom.customView
import java.math.RoundingMode
import java.text.DecimalFormat

class ChatActivity : AppCompatActivity() {

    var mMessagesDatabase: DatabaseReference? = null
    var sendingRequestId: String? = null
    var sendingRequestName: String? = null
    var receivingRequestId: String? = null
    var receivingRequestName: String? = null
    var prettifyedBookTitle: String? = null
    lateinit var mRecyclerView: RecyclerView
    lateinit var messageText: String
    var pushId: String? = null
    var mNewMessagesDatabase: DatabaseReference? = null
    var mUserDatabase: DatabaseReference? = null
    var profileImageUrl: String? = null
    var mRequestsDatabase: DatabaseReference? = null
    var requestStatus: String? = null
    var bookTitle: String? = null
    var rating: String? = null
    var sharedBooks: String? = null
    var rateGave: String? = null
    var sharedBooksString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        if (!TextUtils.isEmpty(intent.getStringExtra("sendingRequestId"))) {
            sendingRequestId = intent.getStringExtra("sendingRequestId")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("currentUserId"))) {
            sendingRequestId = intent.getStringExtra("currentUserId")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("sendingRequestName"))) {
            sendingRequestName = intent.getStringExtra("sendingRequestName")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("currentUserName"))) {
            sendingRequestName = intent.getStringExtra("currentUserName")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("receivingRequestId"))) {
            receivingRequestId = intent.getStringExtra("receivingRequestId")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("userId"))) {
            receivingRequestId = intent.getStringExtra("userId")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("receivingRequestName"))) {
            receivingRequestName = intent.getStringExtra("receivingRequestName")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("userName"))) {
            receivingRequestName = intent.getStringExtra("userName")
        }
        if(!TextUtils.isEmpty(intent.getStringExtra("bookTitleFromRequest"))) {
            bookTitle = intent.getStringExtra("bookTitleFromRequest")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("prettifyedBookTitle"))) {
            prettifyedBookTitle = intent.getStringExtra("prettifyedBookTitle")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("bookTitle"))) {
            bookTitle = intent.getStringExtra("bookTitle")
            val re = Regex("[^A-Za-z0-9]")
            prettifyedBookTitle = re.replace(bookTitle!!, "")
        }
        if (!TextUtils.isEmpty(sendingRequestId)) {
            mUserDatabase = FirebaseDatabase.getInstance().reference
                    .child("users").child(receivingRequestId)
            mUserDatabase!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot?) {
                    profileImageUrl = snap!!.child("photoUrl").value.toString()
                    if (!TextUtils.isEmpty(profileImageUrl)) {
                        Picasso.get()
                                .load(profileImageUrl)
                                .into(profileImageChat)
                    }
                    rating = snap!!.child("totalVote").value.toString()
                    sharedBooks = snap!!.child("sharedBooks").value.toString()
                    voteChat.text = "Rating: $rating/5, shared books: $sharedBooks"
                }

                override fun onCancelled(p0: DatabaseError?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            })
            mRequestsDatabase = FirebaseDatabase.getInstance().reference
                    .child("requests").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
            mRequestsDatabase!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot?) {
                    requestStatus = snap!!.child("requestStatus").value.toString()
                    if (requestStatus == "sent") {
                        nameChat.text = receivingRequestName
                        requestChat.text = "Requested: $bookTitle"
                        acceptButton.text = getString(R.string.cancella)
                        rejectButton.visibility = View.INVISIBLE
                        sendMessageButton.visibility = View.INVISIBLE
                    }
                    if (requestStatus == "received") {
                        nameChat.text = receivingRequestName
                        requestChat.text = "Requested: $bookTitle"
                        sendMessageButton.visibility = View.INVISIBLE
                    }
                    if (requestStatus == "accepted") {
                        nameChat.text = receivingRequestName
                        acceptButton.visibility = View.VISIBLE
                        rejectButton.visibility = View.VISIBLE
                        acceptButton.text = getString(R.string.cancella)
                        rejectButton.text = getString(R.string.conclude)
                        requestChat.text = bookTitle
                        sendMessageButton.visibility = View.VISIBLE
                    }
                    if (requestStatus == "cancelled") {
                        alert(" Request cancelled") {
                            title = "The request has been cancelled"
                            yesButton { cancelRequest() }
                        }.show()
                    }
                    if (requestStatus == "rejected") {
                        alert("Request Rejected") {
                            title = "The request has been rejected"
                            yesButton {
                                mRequestsDatabase = FirebaseDatabase.getInstance().reference
                                        .child("chats").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
                                mRequestsDatabase!!.removeValue()
                                mRequestsDatabase = FirebaseDatabase.getInstance().reference
                                        .child("messages").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
                                mRequestsDatabase!!.removeValue()
                                mRequestsDatabase = FirebaseDatabase.getInstance().reference
                                        .child("requests").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
                                mRequestsDatabase!!.removeValue().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        var showProfileActivity = Intent(this@ChatActivity, ShowProfileActivity::class.java)
                                        startActivity(showProfileActivity)
                                    }
                                }
                            }
                        }.show()
                    }
                    if(requestStatus == "completed") {
                        alert {
                            title = "Rate your experience with $receivingRequestName"
                            customView {
                                linearLayout {
                                    ratingBar {
                                        numStars = 5
                                        rating = 4f
                                        setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                                            rateGave = rating.toString()
                                        }
                                    }
                                }
                            }
                            positiveButton("Rate") {
                                mRequestsDatabase = FirebaseDatabase.getInstance().reference
                                        .child("chats").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
                                mRequestsDatabase!!.removeValue()
                                mRequestsDatabase = FirebaseDatabase.getInstance().reference
                                        .child("messages").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
                                mRequestsDatabase!!.removeValue()
                                mRequestsDatabase = FirebaseDatabase.getInstance().reference
                                        .child("requests").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
                                mRequestsDatabase!!.removeValue()
                                rate() }
                        }.show()

                    }
                }

                override fun onCancelled(p0: DatabaseError?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            })

        }
        mNewMessagesDatabase = FirebaseDatabase.getInstance().reference
                .child("messages")
        mMessagesDatabase = FirebaseDatabase.getInstance().reference
                .child("messages").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
        mRecyclerView = findViewById(R.id.messaggesRecyclerView)
        var layoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.setHasFixedSize(true)
        loadMessages()
    }

    private fun loadMessages() {
        val options = FirebaseRecyclerOptions.Builder<MessageItem>()
                .setQuery(mMessagesDatabase!!, MessageItem::class.java)
                .setLifecycleOwner(this)
                .build()

        val adapter = object : FirebaseRecyclerAdapter<MessageItem, MessageHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
                return MessageHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_message, parent, false))
            }

            override fun onBindViewHolder(holder: MessageHolder, position: Int, model: MessageItem) {
                holder.bindMessageItem(model)
            }
        }

        mRecyclerView.adapter = adapter
    }

    fun sendMessageButtonPressed(view: View) {
        messageText = messaggeEditText.text.toString()
        if (!TextUtils.isEmpty(messageText)) {
            var messageObject = HashMap<String, Any>()
            messageObject.put("text", messageText)
            messageObject.put("name", sendingRequestName!!)
            messageObject.put("fromId", sendingRequestId!!)
            var mMessagesPushDatabase = mNewMessagesDatabase!!.child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle).push()
            pushId = mMessagesPushDatabase.key
            mNewMessagesDatabase!!.child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle).child(pushId).setValue(messageObject)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var messageObject = HashMap<String, Any>()
                            messageObject.put("text", messageText)
                            messageObject.put("name", sendingRequestName!!)
                            messageObject.put("fromId", sendingRequestId!!)
                            mNewMessagesDatabase!!.child(receivingRequestId).child(sendingRequestId + prettifyedBookTitle).child(pushId).setValue(messageObject)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            messaggeEditText.setText("")
                                        }
                                    }
                        }
                    }
        }
    }

    fun acceptButtonPressed(view: View) {
        if (acceptButton.text == getString(R.string.accetta)) {
            var request = HashMap<String, Any>()
            request.put("requestStatus", "accepted")
            mRequestsDatabase = FirebaseDatabase.getInstance().reference
                    .child("requests").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
            mRequestsDatabase!!.updateChildren(request)
            mRequestsDatabase = FirebaseDatabase.getInstance().reference
                    .child("requests").child(receivingRequestId).child(sendingRequestId + prettifyedBookTitle)
            mRequestsDatabase!!.updateChildren(request)
        }
        if (acceptButton.text == getString(R.string.cancella)) {
            mRequestsDatabase = FirebaseDatabase.getInstance().reference
                    .child("requests").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
            mRequestsDatabase!!.removeValue()
            mRequestsDatabase = FirebaseDatabase.getInstance().reference
                    .child("requests").child(receivingRequestId).child(sendingRequestId + prettifyedBookTitle)
            var request = HashMap<String, Any>()
            request.put("requestStatus", "cancelled")
            mRequestsDatabase!!.updateChildren(request)
            mRequestsDatabase = FirebaseDatabase.getInstance().reference
                    .child("chats").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
            mRequestsDatabase!!.removeValue()
            mRequestsDatabase = FirebaseDatabase.getInstance().reference
                    .child("messages").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
            mRequestsDatabase!!.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var showProfileActivity = Intent(this@ChatActivity, ShowProfileActivity::class.java)
                    startActivity(showProfileActivity)
                }
            }
        }
    }

    fun rejectButtonPressed(view: View) {
        if (rejectButton.text == getString(R.string.rifiuta)) {
            rejectRequest()
        }
        if(rejectButton.text == getString(R.string.conclude
                )) {
            completeRequest()
        }
    }

    fun cancelRequest() {
        mRequestsDatabase = FirebaseDatabase.getInstance().reference
                .child("chats").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
        mRequestsDatabase!!.removeValue()
        mRequestsDatabase = FirebaseDatabase.getInstance().reference
                .child("messages").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
        mRequestsDatabase!!.removeValue()
        mRequestsDatabase = FirebaseDatabase.getInstance().reference
                .child("requests").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
        mRequestsDatabase!!.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var showProfileActivity = Intent(this@ChatActivity, ShowProfileActivity::class.java)
                startActivity(showProfileActivity)
            }
        }
    }

    fun rejectRequest() {
        mRequestsDatabase = FirebaseDatabase.getInstance().reference
                .child("chats").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
        mRequestsDatabase!!.removeValue()
        mRequestsDatabase = FirebaseDatabase.getInstance().reference
                .child("messages").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
        mRequestsDatabase!!.removeValue()
        mRequestsDatabase = FirebaseDatabase.getInstance().reference
                .child("requests").child(receivingRequestId).child(sendingRequestId + prettifyedBookTitle)
        var request = HashMap<String, Any>()
        request.put("requestStatus", "rejected")
        mRequestsDatabase!!.updateChildren(request)
        mRequestsDatabase = FirebaseDatabase.getInstance().reference
                .child("requests").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
        mRequestsDatabase!!.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var showProfileActivity = Intent(this@ChatActivity, ShowProfileActivity::class.java)
                startActivity(showProfileActivity)
            }
        }
    }

    fun completeRequest() {
        mRequestsDatabase = FirebaseDatabase.getInstance().reference
                .child("chats").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
        mRequestsDatabase!!.removeValue()
        mRequestsDatabase = FirebaseDatabase.getInstance().reference
                .child("messages").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
        mRequestsDatabase!!.removeValue()
        mRequestsDatabase = FirebaseDatabase.getInstance().reference
                .child("requests").child(sendingRequestId).child(receivingRequestId + prettifyedBookTitle)
        mRequestsDatabase!!.removeValue()
        mRequestsDatabase = FirebaseDatabase.getInstance().reference
                .child("requests").child(receivingRequestId).child(sendingRequestId + prettifyedBookTitle)
        var request = HashMap<String, Any>()
        request.put("requestStatus", "completed")
        mRequestsDatabase!!.updateChildren(request)
        alert {
            title = "Rate your experience with $receivingRequestName"
            customView {
                linearLayout {
                    ratingBar {
                        numStars = 5
                        rating = 4f
                        setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                            rateGave = rating.toString()
                        }
                    }
                }
            }
            positiveButton("Rate") { rate() }
        }.show()
    }

    fun rate() {
        sharedBooks = (sharedBooks!!.toInt() + 1).toString()
        var sharedBooksObject = HashMap<String, Any>()
        mUserDatabase = FirebaseDatabase.getInstance().reference
              .child("users").child(receivingRequestId)
        sharedBooksObject.put("sharedBooks", sharedBooks!!)
        mUserDatabase!!.updateChildren(sharedBooksObject)
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        var ratingFloat = ((rating!!.toFloat() + rateGave!!.toFloat())/(sharedBooks!!.toFloat()))
        rating = df.format(ratingFloat).toString()
        var ratingObject = HashMap<String, Any>()
        ratingObject.put("totalVote", rating!!)
        mUserDatabase!!.updateChildren(ratingObject).addOnCompleteListener { task ->
            if(task.isSuccessful){
                var showProfileActivity = Intent(this@ChatActivity, ShowProfileActivity::class.java)
                startActivity(showProfileActivity)
            }
        }
    }
}
