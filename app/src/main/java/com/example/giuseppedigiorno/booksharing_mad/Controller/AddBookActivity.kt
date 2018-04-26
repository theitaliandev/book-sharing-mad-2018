package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.giuseppedigiorno.booksharing_mad.Model.Book
import com.example.giuseppedigiorno.booksharing_mad.R
import com.example.giuseppedigiorno.booksharing_mad.Utilities.EXTRA_BARCODE
import com.example.giuseppedigiorno.booksharing_mad.Utilities.EXTRA_BOOK
import com.example.giuseppedigiorno.booksharing_mad.Utilities.URL
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_book.*
import okhttp3.*
import java.io.IOException

class AddBookActivity : AppCompatActivity() {

    var barcodeNumber: String? = null
    var book = Book("", "", "", "", "", "", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)
        if(!TextUtils.isEmpty(intent.getStringExtra(EXTRA_BARCODE))){
            barcodeNumber = intent.getStringExtra(EXTRA_BARCODE)
            isbnEditText.setText(barcodeNumber)
        }
    }

    fun startScanButtonPressed(view: View) {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            val zBarScannerActivity = Intent(this, ZBarScannerActivity::class.java)
            startActivity(zBarScannerActivity)
        }else{
            Toast.makeText(this, getString(R.string.camera_permission), Toast.LENGTH_LONG).show()
        }
    }

    fun findBookButtonPressed(view: View) {
        fetchJSON()
        }

    private fun fetchJSON() {
        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED && networkInfo != null && networkInfo.isConnected && !TextUtils.isEmpty(barcodeNumber)){
            var url = URL + barcodeNumber
            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()
            client.newCall(request).enqueue( object : Callback{
                override fun onResponse(call: Call?, response: Response?) {
                    val body = response?.body()?.string()

                    val gson = GsonBuilder().create()
                    val bookInfo =  gson.fromJson(body, Items::class.java)
                    if(bookInfo.totalItems > 0) {
                        val bookItem = bookInfo.items.get(0)
                        if(!TextUtils.isEmpty(bookItem.volumeInfo.imageLinks.smallThumbnail)){
                            book.bookThumbUrl = bookItem.volumeInfo.imageLinks.smallThumbnail
                        }else{
                            book.bookThumbUrl = ""
                        }
                        if(!TextUtils.isEmpty(bookItem.volumeInfo.title)){
                            book.bookTitle = bookItem.volumeInfo.title
                        }else{
                            book.bookTitle = ""
                        }
                        if(bookItem.volumeInfo.authors != null){
                            book.bookAuthor = bookItem.volumeInfo.authors[0]
                        }else{
                            book.bookAuthor = ""
                        }
                        if(!TextUtils.isEmpty(bookItem.volumeInfo.publishedDate)){
                            book.bookPublishedDate = bookItem.volumeInfo.publishedDate
                        }else{
                            book.bookPublishedDate = ""
                        }
                        if(bookItem.volumeInfo.categories != null){
                            book.bookCategory = bookItem.volumeInfo.categories[0]
                        }else{
                            book.bookCategory = ""
                        }

                        runOnUiThread {
                            if(!TextUtils.isEmpty(book.bookThumbUrl) || !TextUtils.isEmpty(book.bookTitle) || !TextUtils.isEmpty(book.bookAuthor) || !TextUtils.isEmpty(book.bookPublishedDate) || !TextUtils.isEmpty(book.bookCategory)){
                                addBookManuallyLinearLayout.visibility = View.INVISIBLE
                                bookInfoCardView.visibility = View.VISIBLE
                                Picasso.get()
                                        .load(book.bookThumbUrl)
                                        .into(bookImageView)
                                bookTitleTextView.text = book.bookTitle
                                writtenByTextView.text = getString(R.string.written_by) + book.bookAuthor
                                categoryBooktextView.text = getString(R.string.category_) + book.bookCategory
                                bookPublishedTextView.text = getString(R.string.published_in) + book.bookPublishedDate
                            }
                        }
                    }else{
                        runOnUiThread {
                            addBookManuallyLinearLayout.visibility = View.VISIBLE
                        }
                    }


                }
                override fun onFailure(call: Call?, e: IOException?) {
                }

            })
        }else{
            Toast.makeText(this@AddBookActivity, getString(R.string.check_ISBN_or_internet), Toast.LENGTH_SHORT).show()
        }
    }

    fun addBookButtonPressed(view: View){
        var addBookDetailActivity = Intent(this@AddBookActivity, AddBookDetailActivity::class.java)
        addBookDetailActivity.putExtra(EXTRA_BOOK, book)
        startActivity(addBookDetailActivity)
    }
    fun addBookManuallyButtonPressed(view: View){
        var addBookDetailActivity = Intent(this@AddBookActivity, AddBookDetailActivity::class.java)
        startActivity(addBookDetailActivity)
    }

    fun backButtonPressed(view: View){
        var showProfileActivity = Intent(this, ShowProfileActivity::class.java)
        startActivity(showProfileActivity)
    }

}

class Items(val items: List<BookItems>, val totalItems: Int)
class BookItems(val volumeInfo: BookDetail)
class BookDetail(val title: String, val imageLinks: ImageLinks, val authors: List<String>, val publishedDate: String, val categories: List<String>)
class ImageLinks(val smallThumbnail: String)