package com.example.giuseppedigiorno.booksharing_mad.ViewHolder

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.example.giuseppedigiorno.booksharing_mad.Model.ChatItem
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_chat.view.*

class ChatHolder(val customView: View, val chatItem: ChatItem? = null): RecyclerView.ViewHolder(customView) {
    fun bindChatItem(chatItem: ChatItem){
        with(chatItem) {
            customView.userNameChatList.text = userName
            customView.bookTitleChatList.text = bookTitle
            if(!TextUtils.isEmpty(profileImageUrl)){
                Picasso.get()
                        .load(profileImageUrl)
                        .into(customView.profileImageChatList)
            }
        }
    }
}