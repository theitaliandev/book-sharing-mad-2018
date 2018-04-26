package com.example.giuseppedigiorno.booksharing_mad.ViewHolder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.example.giuseppedigiorno.booksharing_mad.Model.BookItem
import com.example.giuseppedigiorno.booksharing_mad.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.view.*
import kotlinx.android.synthetic.main.book_item.view.*

class BookHolder(val customView: View, var bookItem: BookItem? = null) : RecyclerView.ViewHolder(customView) {
    fun bindBook(bookItem: BookItem) {
        with(bookItem){
            if(!TextUtils.equals("", bookThumbUrl)) {
                Picasso.get()
                        .load(bookThumbUrl)
                        .into(customView.bookItemImage)
            }else{
                customView.bookItemImage.setImageResource(R.drawable.book_icon)
            }
            customView.bookItemTitle.text = bookTitle
            customView.bookItemAuthor.text = bookAuthor
            customView.bookItemMyReview.text = "\"${bookMyReview}\""
        }
    }
}