package com.example.giuseppedigiorno.booksharing_mad.Model

import android.os.Parcel
import android.os.Parcelable

class MapData constructor(var userId: String, var bookTitle: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(bookTitle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MapData> {
        override fun createFromParcel(parcel: Parcel): MapData {
            return MapData(parcel)
        }

        override fun newArray(size: Int): Array<MapData?> {
            return arrayOfNulls(size)
        }
    }

}