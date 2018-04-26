package com.example.giuseppedigiorno.booksharing_mad.Model

import android.os.Parcel
import android.os.Parcelable

class Book constructor(var bookTitle: String, var bookAuthor: String, var bookCategory: String, var bookPublishedDate: String, var myBookReview: String, var bookImageUrl: String, var bookThumbUrl: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bookTitle)
        parcel.writeString(bookAuthor)
        parcel.writeString(bookCategory)
        parcel.writeString(bookPublishedDate)
        parcel.writeString(myBookReview)
        parcel.writeString(bookImageUrl)
        parcel.writeString(bookThumbUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Book> {
        override fun createFromParcel(parcel: Parcel): Book {
            return Book(parcel)
        }

        override fun newArray(size: Int): Array<Book?> {
            return arrayOfNulls(size)
        }
    }

}