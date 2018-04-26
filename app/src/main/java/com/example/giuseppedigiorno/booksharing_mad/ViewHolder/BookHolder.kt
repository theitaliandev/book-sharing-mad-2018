package com.example.giuseppedigiorno.booksharing_mad.ViewHolder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.example.giuseppedigiorno.booksharing_mad.Model.BookItem
import kotlinx.android.synthetic.main.activity_login.view.*
import kotlinx.android.synthetic.main.book_item.view.*

class BookHolder(val customView: View, var bookItem: BookItem? = null) : RecyclerView.ViewHolder(customView) {
    fun bindBook(bookItem: BookItem) {
        with(bookItem){
            customView.bookTitleItem.text = bookTitle
            customView.bookAuthorItem.text = bookAuthor
        }
    }
}