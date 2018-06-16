package com.example.giuseppedigiorno.booksharing_mad.Model

class ChatItem {
    var userId: String? = null
    var userName: String? = null
    var bookTitle: String? = null
    var profileImageUrl: String? = null

    constructor(){

    }

    constructor(userId: String, userName: String, bookTitle: String, profileImageUrl: String) {
        this.userId = userId
        this.userName = userName
        this.bookTitle = bookTitle
        this.profileImageUrl = profileImageUrl
    }
}