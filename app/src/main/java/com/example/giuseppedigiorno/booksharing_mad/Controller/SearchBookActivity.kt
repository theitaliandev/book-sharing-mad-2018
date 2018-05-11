package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.giuseppedigiorno.booksharing_mad.Model.MapData
import com.example.giuseppedigiorno.booksharing_mad.Model.SearchBookItem
import com.example.giuseppedigiorno.booksharing_mad.R
import com.example.giuseppedigiorno.booksharing_mad.Utilities.EXTRA_MAP
import com.example.giuseppedigiorno.booksharing_mad.ViewHolder.SearchBookHolder
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_search_book.*

class SearchBookActivity : AppCompatActivity() {

    lateinit var mRecyclerView: RecyclerView
    lateinit var mDatabase: DatabaseReference
    lateinit var mUserdatabase: DatabaseReference
    lateinit var mGeoDatabase: DatabaseReference
    lateinit var mBookDatabase: DatabaseReference
    lateinit var geoFire: GeoFire
    private var geoQuery: GeoQuery? = null
    private var mCurrentUser: FirebaseUser? = null
    private var userId: String? = null
    lateinit var query: Query
    private var searchTerm: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var geoLocation: GeoLocation? = null
    var userIdNearby = mutableListOf<String>()
    var mapData = mutableListOf<MapData>()
    var mapDataItem = MapData("", "")
    var complete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_book)
        mCurrentUser = FirebaseAuth.getInstance().currentUser
        userId = mCurrentUser!!.uid
        mBookDatabase = FirebaseDatabase.getInstance().reference
                .child("books")
        mUserdatabase = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(userId)
        mUserdatabase.addValueEventListener( object : ValueEventListener{
            override fun onDataChange(snap: DataSnapshot?) {
                latitude = snap!!.child("latitude").value.toString().toDouble()
                longitude = snap!!.child("longitude").value.toString().toDouble()
                if(latitude != 0.0 && longitude != 0.0){
                    geoLocation = GeoLocation(latitude!!, longitude!!)
                }
            }

            override fun onCancelled(p0: DatabaseError?) {
            }
        })
        mDatabase = FirebaseDatabase.getInstance().reference
                .child("searchBooks")
        mGeoDatabase = FirebaseDatabase.getInstance().reference
                .child("geofire")
        geoFire = GeoFire(mGeoDatabase)
        mRecyclerView = findViewById(R.id.searchListRecyclerView)
        var layoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.setHasFixedSize(true)

    }

    fun searchBookButtonPressed(view: View) {
        searchTerm = searchEditText.text.toString().capitalize()
        if(!TextUtils.isEmpty(searchTerm)) {
            hideKeyboard()
            searchBook(searchTerm!!)
        }else{
            Toast.makeText(this, "Insert the title of the book you are looking for", Toast.LENGTH_LONG).show()
        }
    }


  private fun searchBook(term: String){
            query = mDatabase
                    .orderByChild("title").startAt(term).endAt(term + "\uf88f")

                val options = FirebaseRecyclerOptions.Builder<SearchBookItem>()
                        .setIndexedQuery(query, mDatabase, SearchBookItem::class.java)
                        .setLifecycleOwner(this)
                        .build()

                val adapter = object : FirebaseRecyclerAdapter<SearchBookItem, SearchBookHolder>(options) {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchBookHolder {
                        return SearchBookHolder(LayoutInflater.from(parent.context)
                                .inflate(R.layout.search_book_item, parent, false))
                    }

                    override fun onBindViewHolder(holder: SearchBookHolder, position: Int, model: SearchBookItem) {
                        var bookFoundTitle = getRef(position).key
                        mapData = mutableListOf()
                        var index: Int = 0
                        complete = false
                        mapDataItem= MapData("", "")
                        holder.bindSearchBook(model)
                        holder.customView.setOnClickListener {
                            userIdNearby = mutableListOf()
                            if(geoLocation != null){
                                geoQuery = geoFire.queryAtLocation(geoLocation, 10.0)
                                geoQuery!!.addGeoQueryEventListener( object : GeoQueryEventListener {
                                    override fun onGeoQueryReady() {
                                        if(userIdNearby.isNotEmpty() && !complete){
                                            println(userIdNearby)
                                            println(complete)
                                            for(userId in userIdNearby) {
                                                println(userId)
                                                mBookDatabase.child(userId).child(bookFoundTitle)
                                                        .addValueEventListener( object : ValueEventListener {
                                                            override fun onDataChange(snap: DataSnapshot?) {
                                                                if(snap!!.childrenCount > 1 && index != snap!!.childrenCount.toInt()) {
                                                                    mapDataItem = MapData(userId, snap.child("bookTitle").value.toString())
                                                                    mapData.add(index, mapDataItem)
                                                                    index ++
                                                                    }else if(mapData.isNotEmpty()) {
                                                                        var mapActivity = Intent(this@SearchBookActivity, MapsActivity::class.java)
                                                                        mapActivity.putExtra(EXTRA_MAP, ArrayList(mapData))
                                                                        startActivity(mapActivity)
                                                                    }else{
                                                                        complete = true
                                                                        if (mapData.isEmpty()){
                                                                            Toast.makeText(this@SearchBookActivity, "The book you are looking for it's not near to you", Toast.LENGTH_SHORT).show()
                                                                            complete = false
                                                                        }
                                                                    }

                                                                }
                                                            override fun onCancelled(p0: DatabaseError?) {
                                                            }

                                                        })
                                            }

                                            }

                                    }

                                    override fun onKeyEntered(key: String?, location: GeoLocation?) {
                                        if(!TextUtils.equals(key, userId)){
                                            userIdNearby.add(key!!)
                                        }
                                    }

                                    override fun onKeyMoved(key: String?, location: GeoLocation?) {
                                    }

                                    override fun onKeyExited(key: String?) {
                                    }

                                    override fun onGeoQueryError(error: DatabaseError?) {
                                    }

                                })
                            }
                        }
                    }

                }
            mRecyclerView.adapter = adapter

            }


    fun backButtonPressed(view: View) {
        var showProfileActivity = Intent(this, ShowProfileActivity::class.java)
        startActivity(showProfileActivity)
    }

    protected fun hideKeyboard() {
        val view = this.currentFocus
        if(android.os.Build.VERSION.SDK_INT >= 26) {
            val imm: InputMethodManager = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            view?.post({
                imm.hideSoftInputFromWindow(this.currentFocus.windowToken, 0)
                imm.hideSoftInputFromInputMethod(this.currentFocus.windowToken, 0)
            })
        } else {
            if (view != null) {
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                imm.hideSoftInputFromInputMethod(view.windowToken, 0)
            }
        }
    }
}
