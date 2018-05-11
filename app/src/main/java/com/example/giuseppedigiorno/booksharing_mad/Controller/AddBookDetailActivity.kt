package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.InputFilter
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.giuseppedigiorno.booksharing_mad.Model.Book
import com.example.giuseppedigiorno.booksharing_mad.R
import com.example.giuseppedigiorno.booksharing_mad.Utilities.EXTRA_BOOK
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_add_book.*
import kotlinx.android.synthetic.main.activity_add_book_detail.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception

class AddBookDetailActivity : AppCompatActivity() {

    var book = Book("", "", "", "", "", "", "")
    var prettyfiedTitle = ""
    var firebaseSearchQuery: Query? = null
    private var mCurrentUser: FirebaseUser? = null
    private var mStorageRef: StorageReference? = null
    private var mDatabase: DatabaseReference? = null
    private var mSearchDatabase: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book_detail)
        mCurrentUser = FirebaseAuth.getInstance().currentUser
        var userId = mCurrentUser!!.uid
        mDatabase = FirebaseDatabase.getInstance().reference
                .child("books")
                .child(userId)
        mSearchDatabase = FirebaseDatabase.getInstance().reference
                .child("searchBooks")
        mStorageRef = FirebaseStorage.getInstance().reference

        bookTitleEditTxt.limitLength(50)
        bookAuthorEditText.limitLength(40)
        categoryEditText.limitLength(40)
        myReviewEditText.limitLength(120)

        if(intent.getParcelableExtra<Book>(EXTRA_BOOK) != null)
        {
            book = intent.getParcelableExtra(EXTRA_BOOK)
            bookTitleEditTxt.setText(book.bookTitle)
            bookAuthorEditText.setText(book.bookAuthor)
            categoryEditText.setText(book.bookCategory)
        }

    }

    fun changeBookImageBtnPressed(view: View){
        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED && networkInfo != null && networkInfo.isConnected){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                book.bookTitle = bookTitleEditTxt.text.toString()
                if(!TextUtils.isEmpty(book.bookTitle)){
                    CropImage.startPickImageActivity(this)
                }else{
                    Toast.makeText(this, getString(R.string.book_title_needed), Toast.LENGTH_LONG).show()
                }

            }else {
                Toast.makeText(this@AddBookDetailActivity, getString(R.string.not_all_permission_granted), Toast.LENGTH_LONG).show()
            }
        }else{
            Toast.makeText(this, getString(R.string.modify_image_internet_nedeed), Toast.LENGTH_LONG).show()
        }


    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = CropImage.getPickImageResultUri(this@AddBookDetailActivity, data)
            startCropImageActivity(imageUri)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                progressBar.visibility = View.VISIBLE
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


                var filePath = mStorageRef!!.child("book_images")
                        .child(book.bookTitle +"_"+userId +".jpg")
                filePath.putBytes(imageByteArray)
                        .addOnCompleteListener {
                            task: Task<UploadTask.TaskSnapshot> ->
                            if(task.isSuccessful) {
                                book.bookImageUrl = task.result.downloadUrl.toString()
                                Picasso.get()
                                        .load(book.bookImageUrl)
                                        .into(editBookImage, object : Callback{
                                            override fun onSuccess() {
                                                progressBar.visibility = View.INVISIBLE
                                            }

                                            override fun onError(e: Exception?) {
                                            }

                                        })
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

    fun addBookToLibraryButtonPressed(view: View){
        book.bookTitle = bookTitleEditTxt.text.toString()
        book.bookAuthor = bookAuthorEditText.text.toString()
        book.bookCategory = categoryEditText.text.toString()
        book.myBookReview = myReviewEditText.text.toString()
        if(!TextUtils.isEmpty(book.bookTitle) && !TextUtils.isEmpty(book.bookAuthor) && !TextUtils.isEmpty(book.bookCategory) && !TextUtils.isEmpty(book.myBookReview) && !TextUtils.isEmpty(book.bookImageUrl)){
            var bookObject = HashMap<String, Any>()
            bookObject.put("bookTitle", book.bookTitle)
            bookObject.put("bookAuthor", book.bookAuthor)
            bookObject.put("bookCategory", book.bookCategory)
            bookObject.put("bookMyReview", book.myBookReview)
            bookObject.put("bookImageUrl", book.bookImageUrl)
            bookObject.put("bookThumbUrl", book.bookThumbUrl)
            val re =  Regex("[^A-Za-z0-9]")
            prettyfiedTitle = re.replace(book.bookTitle, "")
            mDatabase!!.child(prettyfiedTitle).setValue(bookObject)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            var bookSearchObject = HashMap<String, Any>()
                            bookSearchObject.put("title", book.bookTitle)
                            bookSearchObject.put("author", book.bookAuthor)
                            mSearchDatabase!!.child(prettyfiedTitle).setValue(bookSearchObject)
                            var bookListActivity = Intent(this, BookListActivity::class.java)
                            startActivity(bookListActivity)
                        }
                    }
        }else{
            Toast.makeText(this, getString(R.string.fill_the_fields), Toast.LENGTH_LONG).show()
        }
                }

    fun EditText.limitLength(maxLength: Int){
        filters = arrayOf(InputFilter.LengthFilter(maxLength))
    }
}
