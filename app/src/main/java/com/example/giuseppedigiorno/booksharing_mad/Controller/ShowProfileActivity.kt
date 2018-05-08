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

    var user = User("","","","","")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)
        mCurrentUser = FirebaseAuth.getInstance().currentUser
        var userId = mCurrentUser!!.uid
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
                if(!TextUtils.isEmpty(user.city)){
                    cityTxt.text = user.city
                }else{
                    cityTxt.text = getString(R.string.hint_city)
                }
            }

            override fun onCancelled(error: DatabaseError?) {

            }
        })
    }

    fun editProfileBtnPressed(view: View){
        var editProfileActivity = Intent(this, EditProfileActivity::class.java)
        editProfileActivity.putExtra(EXTRA_USER, user)
        startActivity(editProfileActivity)
    }

    fun addBookButtonPressed(view: View) {
        var addBookActivity = Intent(this, AddBookActivity::class.java)
        startActivity(addBookActivity)
    }

    fun myBooksButtonClicked(view: View) {
        var bookListActivity = Intent(this, BookListActivity::class.java)
        startActivity(bookListActivity)
    }

}
