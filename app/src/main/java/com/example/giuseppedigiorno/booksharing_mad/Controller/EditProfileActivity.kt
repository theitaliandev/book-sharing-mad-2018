package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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

class EditProfileActivity : AppCompatActivity() {

    lateinit var user: User
    private var mCurrentUser: FirebaseUser? = null
    private var mStorageRef: StorageReference? = null
    private var mDatabase: DatabaseReference?  = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        mCurrentUser = FirebaseAuth.getInstance().currentUser
        var userId = mCurrentUser!!.uid
        mDatabase = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(userId)
        mStorageRef = FirebaseStorage.getInstance().reference
        checkPermissions()
        user = intent.getParcelableExtra(EXTRA_USER)
        if(!TextUtils.isEmpty(user.name)){
            nameEditTxt.setText(user.name)
        }else{
            nameEditTxt.setText(getString(R.string.hint_full_name))
        }
        if(!TextUtils.isEmpty(user.favouriteBookGeneres)){
            favouriteBooksGeneresEditTxt.setText(user.favouriteBookGeneres)
        }else{
            favouriteBooksGeneresEditTxt.setText(getString(R.string.hint_favourite_books))
        }
        if(!TextUtils.isEmpty(user.bio)){
            bioEditTxt.setText(user.bio)
        }else{
            bioEditTxt.setText(getString(R.string.hint_bio))
        }
        if(!TextUtils.isEmpty(user.city)){
            cityEditTxt.setText(user.city)
        }else{
            cityEditTxt.setText(getString(R.string.hint_city))
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
        cityEditTxt.limitLength(30)
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
            Toast.makeText(this, "To modify your profile image you need an internet connection", Toast.LENGTH_LONG).show()
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
                                                .into(editProfileImage)
                                    }else{

                                    }
                                }
                            }
                        }
                Toast.makeText(this, getString(R.string.crop_ok), Toast.LENGTH_SHORT).show()
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
        if (!TextUtils.isEmpty(user.name) && !TextUtils.isEmpty(user.favouriteBookGeneres) && !TextUtils.isEmpty(user.bio) && !TextUtils.isEmpty(user.city)) {
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
        }

    }

    fun EditText.limitLength(maxLength: Int){
        filters = arrayOf(InputFilter.LengthFilter(maxLength))
    }
}
