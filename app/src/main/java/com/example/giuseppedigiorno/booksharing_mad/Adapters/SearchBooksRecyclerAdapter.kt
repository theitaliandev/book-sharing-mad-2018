package com.example.giuseppedigiorno.booksharing_mad.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.giuseppedigiorno.booksharing_mad.Model.SearchBookItem
import com.example.giuseppedigiorno.booksharing_mad.R
import kotlinx.android.synthetic.main.search_book_item.view.*

class SearchBooksRecyclerAdapter(val context: Context, val foundBooks: List<SearchBookItem>, val itemClick: (SearchBookItem) -> Unit): RecyclerView.Adapter<SearchBooksRecyclerAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.search_book_item, parent, false)
        return Holder(view, itemClick)
    }

    override fun getItemCount(): Int {
        return foundBooks.count()
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindFoundBooks(foundBooks[position])
    }


    inner class Holder(itemView: View?, itemClick: (SearchBookItem) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val bookTitleTextView = itemView?.findViewById<TextView>(R.id.searchBookItemTitle)
        val bookAuthorTextView = itemView?.findViewById<TextView>(R.id.searchBookItemAuthor)

        fun bindFoundBooks(foundBook: SearchBookItem) {
            bookTitleTextView?.text = foundBook.title
            bookAuthorTextView?.text = foundBook.author
            itemView.setOnClickListener { itemClick(foundBook) }
        }
    }
}