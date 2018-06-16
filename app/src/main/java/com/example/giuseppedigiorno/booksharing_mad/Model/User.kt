package com.example.giuseppedigiorno.booksharing_mad.Model

import android.os.Parcel
import android.os.Parcelable

class User constructor(var name:String, var favouriteBookGeneres:String, var bio: String, var city: String, var address: String, var countryCode: String, var latitude: Double, var longitude: Double, var photoUrl: String, var sharedBooks: String, var tokenId: String, var totalVote: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(favouriteBookGeneres)
        parcel.writeString(bio)
        parcel.writeString(city)
        parcel.writeString(address)
        parcel.writeString(countryCode)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(photoUrl)
        parcel.writeString(sharedBooks)
        parcel.writeString(tokenId)
        parcel.writeString(totalVote)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

}