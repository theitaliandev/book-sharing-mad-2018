package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.giuseppedigiorno.booksharing_mad.Model.BookItem
import com.example.giuseppedigiorno.booksharing_mad.R
import com.example.giuseppedigiorno.booksharing_mad.ViewHolder.BookHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class BookListActivity : AppCompatActivity() {

    lateinit var mRecyclerView: RecyclerView
    lateinit var mDatabase: DatabaseReference
    lateinit var mCurrentUser: FirebaseUser
    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        mCurrentUser = FirebaseAuth.getInstance().currentUser!!
        userId = mCurrentUser.uid

        mDatabase = FirebaseDatabase.getInstance().reference
                .child("books").child(userId)

        mRecyclerView = findViewById(R.id.bookListRecyclerView)
        var layoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.setHasFixedSize(true)
        loadBookData()


    }

    private fun loadBookData(){

        val options = FirebaseRecyclerOptions.Builder<BookItem>()
                .setQuery(mDatabase, BookItem::class.java)
                .setLifecycleOwner(this)
                .build()

        val adapter = object : FirebaseRecyclerAdapter<BookItem, BookHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookHolder {
                return BookHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.book_item, parent, false))
            }

            override fun onBindViewHolder(holder: BookHolder, position: Int, model: BookItem) {
                holder.bindBook(model)
                var bookTitle = model.bookTitle
                var author = model.bookAuthor
                var category = model.bookCategory
                var myReview = model.bookMyReview
                var photoUrl = model.bookImageUrl
                holder.customView.setOnClickListener { startEditBookActivity(userId, bookTitle!!, author!!, category!!, photoUrl!!, myReview!!) }
            }

        }

        mRecyclerView.adapter = adapter
    }

    fun addBookButtonPressed(view: View){
        var addBookActivity = Intent(this, AddBookActivity::class.java)
        startActivity(addBookActivity)
    }

    fun backButtonPressed(view: View){
        var showProfileActivity = Intent(this, ShowProfileActivity::class.java)
        startActivity(showProfileActivity)
    }

    private fun startEditBookActivity(userId: String, bookTitle: String, author: String, category: String, photoUrl: String, myReview: String) {
        var editBookActivity = Intent(this, EditBookActivity::class.java)
        editBookActivity.putExtra("userId", userId)
        editBookActivity.putExtra("bookTitle", bookTitle)
        editBookActivity.putExtra("author", author)
        editBookActivity.putExtra("category", category)
        editBookActivity.putExtra("photoUrl", photoUrl)
        editBookActivity.putExtra("myReview", myReview)
        startActivity(editBookActivity)
    }


}

