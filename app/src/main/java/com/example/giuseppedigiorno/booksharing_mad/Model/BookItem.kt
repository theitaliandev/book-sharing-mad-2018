package com.example.giuseppedigiorno.booksharing_mad.Model

class BookItem {

    var bookTitle: String? = null
    var bookAuthor: String? = null
    var bookCategory: String? = null
    var bookMyReview: String? = null
    var bookImageUrl: String? = null
    var bookThumbUrl: String? = null

    constructor(){

    }

    constructor(bookTitle: String?, bookAuthor: String?, bookCategory: String?, bookMyReview: String?, bookImageUrl: String?, bookThumbUrl: String?) {
        this.bookTitle = bookTitle
        this.bookAuthor = bookAuthor
        this.bookCategory = bookCategory
        this.bookMyReview = bookMyReview
        this.bookImageUrl = bookImageUrl
        this.bookThumbUrl = bookThumbUrl
    }


}