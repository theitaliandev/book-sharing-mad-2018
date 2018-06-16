package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.giuseppedigiorno.booksharing_mad.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_book.*
import org.jetbrains.anko.themedImageSwitcher

class EditBookActivity : AppCompatActivity() {

    lateinit var userId: String
    lateinit var bookTitle: String
    lateinit var mBookDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_book)

        userId = intent.getStringExtra("userId")
        bookTitle = intent.getStringExtra("bookTitle")

        val re =  Regex("[^A-Za-z0-9]")
        var prettyfiedTitle = re.replace(bookTitle, "")

        mBookDatabase = FirebaseDatabase.getInstance().reference
                .child("books").child(userId).child(prettyfiedTitle)

        Picasso.get()
                .load(intent.getStringExtra("photoUrl"))
                .into(editBookImageEdit)

        bookTitleEdit.text = bookTitle
        bookAuthorEdit.text = intent.getStringExtra("author")
        categoryEdit.text = intent.getStringExtra("category")
        myReviewEdit.text = intent.getStringExtra("myReview")
    }

    fun removeBookPressed(view: View) {
        mBookDatabase.removeValue().addOnCompleteListener {task ->
            if(task.isSuccessful){
                var bookListActivity = Intent(this, BookListActivity::class.java)
                startActivity(bookListActivity)
            }
        }
    }
}
