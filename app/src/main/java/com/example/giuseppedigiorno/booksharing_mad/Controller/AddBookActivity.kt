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
import com.example.giuseppedigiorno.booksharing_mad.R
import com.example.giuseppedigiorno.booksharing_mad.Utilities.EXTRA_BARCODE
import com.example.giuseppedigiorno.booksharing_mad.Utilities.URL
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_book.*
import okhttp3.*
import org.w3c.dom.Text
import java.io.IOException

class AddBookActivity : AppCompatActivity() {

    var barcodeNumber: String? = null

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
            Toast.makeText(this, "Camera permission are required to scan the barcode", Toast.LENGTH_LONG).show()
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
                        val bookImageUrl = bookItem.volumeInfo.imageLinks.smallThumbnail
                        val bookTitle = bookItem.volumeInfo.title
                        val bookAuthor = bookItem.volumeInfo.authors[0]
                        val bookDate = bookItem.volumeInfo.publishedDate
                        val bookCategory = bookItem.volumeInfo.categories[0]
                        runOnUiThread {
                            if(!TextUtils.isEmpty(bookImageUrl) && !TextUtils.isEmpty(bookTitle) && !TextUtils.isEmpty(bookAuthor) && !TextUtils.isEmpty(bookDate) && !TextUtils.isEmpty(bookCategory)){
                                bookInfoCardView.visibility = View.VISIBLE
                                Picasso.get()
                                        .load(bookImageUrl)
                                        .into(bookImageView)
                                bookTitleTextView.text = bookTitle
                                writtenByTextView.text = "Written by: " + bookAuthor
                                categoryBooktextView.text = "Category: " + bookCategory
                                bookPublishedTextView.text = "Published in: " + bookDate
                            }
                        }
                    }else{
                        runOnUiThread {
                            Toast.makeText(this@AddBookActivity, "We can't find your book in Google's Book Library, please try again", Toast.LENGTH_LONG).show()
                        }
                    }


                }
                override fun onFailure(call: Call?, e: IOException?) {
                }

            })
        }
    }

    fun addBookButtonPressed(view: View){

    }

}

class Items(val items: List<BookItems>, val totalItems: Int)
class BookItems(val volumeInfo: Book)
class Book(val title: String, val imageLinks: ImageLinks, val authors: List<String>, val publishedDate: String, val categories: List<String>)
class ImageLinks(val smallThumbnail: String)