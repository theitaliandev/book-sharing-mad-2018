package com.example.giuseppedigiorno.booksharing_mad.Model

class MessageItem {

    var text: String? = null
    var name: String? = null
    var fromId: String? = null

    constructor(){

    }

    constructor(text: String, name: String, fromId: String) {
        this.text = text
        this.name = name
        this.fromId = fromId
    }
}