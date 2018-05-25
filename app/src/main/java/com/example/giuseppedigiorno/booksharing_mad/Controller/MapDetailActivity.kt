package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.example.giuseppedigiorno.booksharing_mad.R
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_map_detail.*

class MapDetailActivity : AppCompatActivity() {

    lateinit var bookTitle: String
    lateinit var userName: String
    lateinit var userId: String
    lateinit var mDatabase: DatabaseReference
    lateinit var mBookDatabase: DatabaseReference
    lateinit var userPhotoUrl: String
    lateinit var bookImageUrl: String
    lateinit var review: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_detail)
        bookTitle = intent.getStringExtra("bookTitle")
        userName = intent.getStringExtra("userName")
        userId = intent.getStringExtra("userId")

        nameTxt_map_detail.text = userName
        book_title_map_detail.text = bookTitle

        val re =  Regex("[^A-Za-z0-9]")
        var prettyfiedBokTitle = re.replace(bookTitle, "")

        mDatabase = FirebaseDatabase.getInstance().reference
                .child("users").child(userId)
        mBookDatabase = FirebaseDatabase.getInstance().reference
                .child("books").child(userId).child(prettyfiedBokTitle)

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
}
