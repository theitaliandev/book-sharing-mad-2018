package com.example.giuseppedigiorno.booksharing_mad.ViewHolder

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.example.giuseppedigiorno.booksharing_mad.Model.MessageItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.item_message.view.*

class MessageHolder(val customView: View, val messageItem: MessageItem? = null) : RecyclerView.ViewHolder(customView) {

    fun bindMessageItem(messageItem: MessageItem) {

        var mCurrentUser = FirebaseAuth.getInstance().currentUser
        var userId = mCurrentUser!!.uid

        with(messageItem){
            if(userId == fromId) {
                var layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                layoutParams.setMargins(8, 8, 8, 8)
                customView.messageCardView.layoutParams = layoutParams
                customView.messageCardView.setCardBackgroundColor(Color.rgb(190, 215, 176))
                customView.messangerTextView.text = name
                customView.messageTextView.text = text
            }else{
                customView.messangerTextView.text = name
                customView.messageTextView.text = text
            }
        }
    }
}