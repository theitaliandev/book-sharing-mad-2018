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
import kotlinx.android.synthetic.main.activity_add_book.*
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import okhttp3.*
import java.io.IOException

class AddBookActivity : AppCompatActivity() {

    var barcodeNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)
        if(!TextUtils.isEmpty(intent.getStringExtra(EXTRA_BARCODE))){
            barcodeNumber = intent.getStringExtra(EXTRA_BARCODE)
            barcodeText.text = barcodeNumber
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
                    val bookItem = bookInfo.items.get(0)
                    val bookTitle = bookItem.volumeInfo.title
                    runOnUiThread {
                        if(!TextUtils.isEmpty(bookTitle)){
                            bookTitleText.text = bookTitle
                        }
                    }
                }
                override fun onFailure(call: Call?, e: IOException?) {
                }

            })
        }
    }

}

class Items(val items: List<BookItems>)
class BookItems(val volumeInfo: Book)
class Book(val title: String)