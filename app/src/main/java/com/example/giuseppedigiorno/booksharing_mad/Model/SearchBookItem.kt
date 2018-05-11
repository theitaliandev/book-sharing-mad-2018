package com.example.giuseppedigiorno.booksharing_mad.Model

class SearchBookItem {

    var title: String? = null
    var author: String? = null

    constructor() {

    }

    constructor(title: String?, author: String?) {
        this.title = title
        this.author = author
    }
}