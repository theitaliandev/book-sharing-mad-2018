package com.example.giuseppedigiorno.booksharing_mad.ViewHolder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.example.giuseppedigiorno.booksharing_mad.Model.SearchBookItem
import com.example.giuseppedigiorno.booksharing_mad.R
import kotlinx.android.synthetic.main.search_book_item.view.*


class SearchBookHolder(val customView: View, var searchBookItem: SearchBookItem? = null): RecyclerView.ViewHolder(customView) {
    fun bindSearchBook(searchBookItem: SearchBookItem) {
        with(searchBookItem) {
            customView.searchBookItemTitle.text = title
            customView.searchBookItemAuthor.text = author
        }
    }
}

