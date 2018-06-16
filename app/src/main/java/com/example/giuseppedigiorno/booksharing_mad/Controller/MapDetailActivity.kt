package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.example.giuseppedigiorno.booksharing_mad.R
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_map_detail.*

class MapDetailActivity : AppCompatActivity() {

    lateinit var bookTitle: String
    lateinit var userName: String
    lateinit var userId: String
    lateinit var currentUserId: String
    lateinit var currentUserName: String
    lateinit var currentUserPhotoUrl: String
    lateinit var mCurrentUserDatabase: DatabaseReference
    lateinit var mDatabase: DatabaseReference
    lateinit var mBookDatabase: DatabaseReference
    lateinit var mRequestsDatabase: DatabaseReference
    lateinit var mChatsDatabase: DatabaseReference
    lateinit var mMessagesDatabase: DatabaseReference
    lateinit var mNotificationsDatabase: DatabaseReference
    lateinit var userPhotoUrl: String
    lateinit var bookImageUrl: String
    lateinit var review: String
    lateinit var prettyfiedBokTitle: String
    private var pushId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_detail)
        bookTitle = intent.getStringExtra("bookTitle")
        userName = intent.getStringExtra("userName")
        userId = intent.getStringExtra("userId")
        currentUserId = intent.getStringExtra("currentUserId")
        currentUserName = intent.getStringExtra("currentUserName")

        nameTxt_map_detail.text = userName
        book_title_map_detail.text = bookTitle

        val re =  Regex("[^A-Za-z0-9]")
        prettyfiedBokTitle = re.replace(bookTitle, "")

        mDatabase = FirebaseDatabase.getInstance().reference
                .child("users").child(userId)
        mCurrentUserDatabase = FirebaseDatabase.getInstance().reference
                .child("users").child(currentUserId)
        mBookDatabase = FirebaseDatabase.getInstance().reference
                .child("books").child(userId).child(prettyfiedBokTitle)
        mRequestsDatabase = FirebaseDatabase.getInstance().reference
                .child("requests")
        mChatsDatabase = FirebaseDatabase.getInstance().reference
                .child("chats")
        mMessagesDatabase = FirebaseDatabase.getInstance().reference
                .child("messages")
        mNotificationsDatabase = FirebaseDatabase.getInstance().reference
                .child("notifications").child(userId)

        mCurrentUserDatabase.addValueEventListener( object : ValueEventListener{
            override fun onDataChange(snap: DataSnapshot?) {
                currentUserPhotoUrl = snap!!.child("photoUrl").value.toString()
            }
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

        mDatabase.addValueEventListener( object : ValueEventListener {

            override fun onDataChange(snap: DataSnapshot?) {
                userPhotoUrl = snap!!.child("photoUrl").value.toString()
                if(!TextUtils.isEmpty(userPhotoUrl)){
                    Picasso.get()
                            .load(userPhotoUrl)
                            .into(profile_image_map_detail)
                }else{
                    profile_image_map_detail.setImageResource(R.drawable.profile_image_black)
                }
                var totalVote = snap!!.child("totalVote").value.toString()
                var sharedBooks = snap!!.child("sharedBooks").value.toString()
                rating_map_detail.text = totalVote
                shared_books_map_detail.text = sharedBooks
            }
            override fun onCancelled(snap: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

        mBookDatabase.addValueEventListener( object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot?) {
                bookImageUrl = snap!!.child("bookThumbUrl").value.toString()
                if(!TextUtils.isEmpty(bookImageUrl)){
                    Picasso.get()
                            .load(bookImageUrl)
                            .into(book_image_map_detail)
                }else{
                    book_image_map_detail.setImageResource(R.drawable.book_icon)
                }
                review = snap.child("bookMyReview").value.toString()
                book_review_map_detail.text = review
            }
            override fun onCancelled(snap: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

    }

    fun requestBookButtonPressed(view: View){
        var notificationObject = HashMap<String, Any>()
        notificationObject.put("from", currentUserId)
        notificationObject.put("type", "request")
        var mNotificationPushDatabase = mNotificationsDatabase.push()
        var pushNotificationId = mNotificationPushDatabase.key
        mNotificationsDatabase.child(pushNotificationId).setValue(notificationObject)
        var requestObject = HashMap<String, Any>()
        requestObject.put("bookTitle", bookTitle)
        requestObject.put("requestStatus", "sent")
        mRequestsDatabase.child(currentUserId).child(userId+prettyfiedBokTitle).setValue(requestObject)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        var requestObject = HashMap<String, Any>()
                        requestObject.put("bookTitle", bookTitle)
                        requestObject.put("requestStatus", "received")
                        mRequestsDatabase.child(userId).child(currentUserId+prettyfiedBokTitle).setValue(requestObject)
                                .addOnCompleteListener { task ->
                                    if(task.isSuccessful){
                                        var chatObject = HashMap<String, Any>()
                                        chatObject.put("bookTitle", bookTitle)
                                        chatObject.put("userId", userId)
                                        chatObject.put("userName", userName)
                                        chatObject.put("profileImageUrl", userPhotoUrl)
                                        mChatsDatabase.child(currentUserId).child(userId+prettyfiedBokTitle).setValue(chatObject)
                                                .addOnCompleteListener { task ->
                                                    if(task.isSuccessful){
                                                        var chatObject = HashMap<String, Any>()
                                                        chatObject.put("bookTitle", bookTitle)
                                                        chatObject.put("userId", currentUserId)
                                                        chatObject.put("userName", currentUserName)
                                                        chatObject.put("profileImageUrl", currentUserPhotoUrl)
                                                        mChatsDatabase.child(userId).child(currentUserId+prettyfiedBokTitle).setValue(chatObject)
                                                                .addOnCompleteListener { task ->
                                                                    if(task.isSuccessful) {
                                                                        var messageObject = HashMap<String, Any>()
                                                                        messageObject.put("text", "$currentUserName requested $bookTitle to $userName")
                                                                        messageObject.put("name", currentUserName)
                                                                        messageObject.put("fromId", currentUserId)
                                                                       var mMessagesPushDatabase = mMessagesDatabase.child(currentUserId).child(userId+prettyfiedBokTitle).push()
                                                                        pushId = mMessagesPushDatabase.key
                                                                        mMessagesDatabase.child(currentUserId).child(userId+prettyfiedBokTitle).child(pushId).setValue(messageObject)
                                                                                .addOnCompleteListener { task ->
                                                                                    if(task.isSuccessful) {
                                                                                        var messageObject = HashMap<String, Any>()
                                                                                        messageObject.put("text", "$currentUserName requested $bookTitle to $userName")
                                                                                        messageObject.put("name", currentUserName)
                                                                                        messageObject.put("fromId", currentUserId)
                                                                                        mMessagesDatabase.child(userId).child(currentUserId+prettyfiedBokTitle).child(pushId).setValue(messageObject)
                                                                                                .addOnCompleteListener { task ->
                                                                                                    if(task.isSuccessful) {
                                                                                                        var chatActivity = Intent(this, ChatActivity::class.java)
                                                                                                        chatActivity.putExtra("sendingRequestId", currentUserId)
                                                                                                        chatActivity.putExtra("sendingRequestName", currentUserName)
                                                                                                        chatActivity.putExtra("receivingRequestName", userName)
                                                                                                        chatActivity.putExtra("receivingRequestId", userId)
                                                                                                        chatActivity.putExtra("prettifyedBookTitle", prettyfiedBokTitle)
                                                                                                        chatActivity.putExtra("bookTitleFromRequest", bookTitle)
                                                                                                        startActivity(chatActivity)
                                                                                                    }
                                                                                                }
                                                                                    }
                                                                                }
                                                                    }
                                                                }
                                                    }
                                                }
                                    }
                                }
                    }
                }
    }
}
