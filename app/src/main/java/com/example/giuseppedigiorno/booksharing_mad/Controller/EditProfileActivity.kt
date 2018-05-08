package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.giuseppedigiorno.booksharing_mad.Model.User
import com.example.giuseppedigiorno.booksharing_mad.R
import com.example.giuseppedigiorno.booksharing_mad.Utilities.EXTRA_USER
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    lateinit var user: User
    private var mAuth: FirebaseAuth? = null
    private var mCurrentUser: FirebaseUser? = null
    private var userId: String? = null
    private var mStorageRef: StorageReference? = null
    private var mDatabase: DatabaseReference?  = null
    private var mGeoDatabase: DatabaseReference? = null
    private var geoFire: GeoFire? = null
    private var geoLocation: GeoLocation? = null
    private var geocoder: Geocoder? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        geocoder = Geocoder(this, Locale.getDefault())
        mAuth = FirebaseAuth.getInstance()
        mCurrentUser = FirebaseAuth.getInstance().currentUser
        userId = mCurrentUser!!.uid
        mGeoDatabase = FirebaseDatabase.getInstance().reference
                .child("geofire")
        geoFire = GeoFire(mGeoDatabase)
        mDatabase = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(userId)
        mStorageRef = FirebaseStorage.getInstance().reference
        checkPermissions()
        user = intent.getParcelableExtra(EXTRA_USER)
        if(!TextUtils.isEmpty(user.name)){
            nameEditTxt.setText(user.name)
        }
        if(!TextUtils.isEmpty(user.favouriteBookGeneres)){
            favouriteBooksGeneresEditTxt.setText(user.favouriteBookGeneres)
        }
        if(!TextUtils.isEmpty(user.bio)){
            bioEditTxt.setText(user.bio)
        }
        if(!TextUtils.isEmpty(user.city)){
            cityEditTxt.setText(user.city)
        }
        if(!TextUtils.isEmpty(user.photoUrl)){
            Picasso.get()
                    .load(user.photoUrl)
                    .into(editProfileImage)
        }else{
            editProfileImage.setImageResource(R.drawable.profile_image)
        }

        nameEditTxt.limitLength(30)
        favouriteBooksGeneresEditTxt.limitLength(30)
        bioEditTxt.limitLength(100)
        cityEditTxt.limitLength(100)
    }

    private fun checkPermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.ACCESS_NETWORK_STATE
                ).withListener( object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report != null) {
                            if (report.isAnyPermissionPermanentlyDenied()) {
                                Toast.makeText(this@EditProfileActivity, getString(R.string.not_all_permission_granted), Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        AlertDialog.Builder(this@EditProfileActivity)
                                .setTitle(getString(R.string.permission_rationale_title))
                                .setMessage(getString(R.string.permission_rationale_message))
                                .setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener {
                                    dialogInterface, i ->
                                    dialogInterface.dismiss()
                                    token?.cancelPermissionRequest()
                                })
                                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener {
                                    dialogInterface, i ->
                                    dialogInterface.dismiss()
                                    token?.continuePermissionRequest()
                                })
                                .show()
                    }

                }).check()
    }

    fun changeProfileImageBtnPressed(view: View){
        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED && networkInfo != null && networkInfo.isConnected){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(this)
            }else {
                Toast.makeText(this@EditProfileActivity, getString(R.string.not_all_permission_granted), Toast.LENGTH_LONG).show()
            }
            }else{
            Toast.makeText(this, getString(R.string.profile_image_internet_needed), Toast.LENGTH_LONG).show()
        }


    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = CropImage.getPickImageResultUri(this@EditProfileActivity, data)
            startCropImageActivity(imageUri)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                progressBarLayout.visibility = View.VISIBLE
                val resultUri = result.uri
                var userId = mCurrentUser!!.uid
                var imageFile = File(resultUri.path)
                var compressedImage = Compressor(this)
                        .setMaxWidth(300)
                        .setMaxHeight(300)
                        .setQuality(65)
                        .compressToBitmap(imageFile)

                var byteArray = ByteArrayOutputStream()
                compressedImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArray)
                var imageByteArray: ByteArray
                imageByteArray = byteArray.toByteArray()


                var filePath = mStorageRef!!.child("user_profile_images")
                        .child(userId +".jpg")
                filePath.putBytes(imageByteArray)
                        .addOnCompleteListener {
                            task: Task<UploadTask.TaskSnapshot> ->
                            if(task.isSuccessful) {
                                user.photoUrl = task.result.downloadUrl.toString()
                                var updateObject = HashMap<String, Any>()
                                updateObject.put("photoUrl", user.photoUrl)
                                mDatabase!!.updateChildren(updateObject).addOnCompleteListener { task ->
                                    if(task.isSuccessful){
                                        Picasso.get()
                                                .load(user.photoUrl)
                                                .into(editProfileImage, object: Callback {
                                                    override fun onSuccess() {
                                                        progressBarLayout.visibility = View.INVISIBLE
                                                    }

                                                    override fun onError(e: Exception?) {
                                                    }

                                                })
                                    }else{

                                    }
                                }
                            }
                        }
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Toast.makeText(this, "${getString(R.string.crop_not_ok)}${result.error}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startCropImageActivity(imageUri: Uri?) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setFixAspectRatio(true)
                .setMultiTouchEnabled(true)
                .start(this)
    }

    fun updateProfileButtonPressed(view: View) {
        user.name = nameEditTxt.text.toString().trim()
        user.favouriteBookGeneres = favouriteBooksGeneresEditTxt.text.toString().trim()
        user.bio = bioEditTxt.text.toString().trim()
        user.city = cityEditTxt.text.toString().trim()
        if(!TextUtils.isEmpty(user.city)) {
            var userLocation = geocoder!!.getFromLocationName(user.city, 1)
            if(!userLocation.isEmpty()) {
                if (!TextUtils.isEmpty(user.name) && !TextUtils.isEmpty(user.favouriteBookGeneres) && !TextUtils.isEmpty(user.bio)) {
                    latitude = userLocation[0].latitude
                    longitude = userLocation[0].longitude
                    geoLocation = GeoLocation(latitude!!, longitude!!)
                    geoFire!!.setLocation(userId, geoLocation)
                    var updateObject = HashMap<String, Any>()
                    updateObject.put("name", user.name)
                    updateObject.put("favouriteBooksGeneres", user.favouriteBookGeneres)
                    updateObject.put("bio", user.bio)
                    updateObject.put("city", user.city)
                    mDatabase!!.updateChildren(updateObject).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i("update_profile", "Profile succesfully updated")
                        }else{
                        }
                    }
                    var showProfileActivity = Intent(this, ShowProfileActivity::class.java)
                    startActivity(showProfileActivity)
                }else{
                    Toast.makeText(this, getString(R.string.fill_the_fields), Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Please enter a correct city name", Toast.LENGTH_LONG).show()
            }
        }else{
            Toast.makeText(this, "City is missing", Toast.LENGTH_LONG).show()
        }

    }

    fun backButtonPressed(view: View) {
        var showProfileActivity = Intent(this, ShowProfileActivity::class.java)
        startActivity(showProfileActivity)
    }

    fun signOutButtonPressed(view: View) {
        mAuth!!.signOut()
        var loginActivity = Intent(this, LoginActivity::class.java)
        startActivity(loginActivity)
    }

    fun EditText.limitLength(maxLength: Int){
        filters = arrayOf(InputFilter.LengthFilter(maxLength))
    }
}
