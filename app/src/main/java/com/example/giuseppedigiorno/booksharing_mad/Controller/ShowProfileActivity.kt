package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationProvider
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.text.InputFilter
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.giuseppedigiorno.booksharing_mad.Model.User
import com.example.giuseppedigiorno.booksharing_mad.R
import com.example.giuseppedigiorno.booksharing_mad.Utilities.EXTRA_USER
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_show_profile.*

class ShowProfileActivity : AppCompatActivity() {


    private var mDatabase: DatabaseReference?  = null
    private var mCurrentUser: FirebaseUser? = null
    private var userId: String? = null

    var user = User("","","","","", "", 0.0, 0.0, "", "0", "", "5")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)
        mCurrentUser = FirebaseAuth.getInstance().currentUser
        userId = mCurrentUser!!.uid
        mDatabase = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(userId)
        mDatabase!!.addValueEventListener( object: ValueEventListener{
            override fun onDataChange(snap: DataSnapshot?) {
                user.name = snap!!.child("name").value.toString()
                user.favouriteBookGeneres = snap.child("favouriteBooksGeneres").value.toString()
                user.bio = snap.child("bio").value.toString()
                user.city = snap.child("city").value.toString()
                user.photoUrl = snap.child("photoUrl").value.toString()
                user.address = snap.child("address").value.toString()
                user.countryCode = snap.child("countryCode").value.toString()
                user.sharedBooks = snap.child("sharedBooks").value.toString()
                user.totalVote = snap.child("totalVote").value.toString()

                if(!TextUtils.isEmpty(user.photoUrl)){
                    Picasso.get()
                            .load(user.photoUrl)
                            .into(profile_image)
                }else{
                    profile_image.setImageResource(R.drawable.profile_image_black)
                }

                if(!TextUtils.isEmpty(user.name)){
                    nameTxt.text = user.name
                }else{
                    nameTxt.text = getString(R.string.hint_full_name)
                }
                if(!TextUtils.isEmpty(user.favouriteBookGeneres)){
                    favouriteBooksGeneresTxt.text = user.favouriteBookGeneres
                }else{
                    favouriteBooksGeneresTxt.text = getString(R.string.hint_favourite_books)
                }
                if(!TextUtils.isEmpty(user.bio)){
                    bioTxt.text = user.bio
                }else{
                    bioTxt.text = getString(R.string.hint_bio)
                }
                if(!TextUtils.isEmpty(user.city) && !TextUtils.isEmpty(user.countryCode)){
                    cityTxt.text = "${user.city}, ${user.countryCode}"
                }else{
                    cityTxt.text = getString(R.string.hint_city)
                }

                totalVote.text = user.totalVote
                sharedBooks.text = user.sharedBooks

            }

            override fun onCancelled(error: DatabaseError?) {

            }
        })

        var tokenId = FirebaseInstanceId.getInstance().token
        var tokenObject = HashMap<String, Any>()
        tokenObject.put("tokenId", tokenId!!)
        mDatabase!!.updateChildren(tokenObject)
    }

    fun editProfileBtnPressed(view: View){
        var editProfileActivity = Intent(this, EditProfileActivity::class.java)
        editProfileActivity.putExtra(EXTRA_USER, user)
        startActivity(editProfileActivity)
    }

    fun addBookButtonPressed(view: View) {
        var chatListActivity = Intent(this, ChatListActivity::class.java)
        chatListActivity.putExtra("currentUserId", userId)
        chatListActivity.putExtra("currentUserName", user.name)
        startActivity(chatListActivity)
    }

    fun myBooksButtonClicked(view: View) {
        var bookListActivity = Intent(this, BookListActivity::class.java)
        startActivity(bookListActivity)
    }

    fun findABookButtonPressed(view: View) {
        var searchBookActivity = Intent(this, SearchBookActivity::class.java)
        startActivity(searchBookActivity)
    }

}
