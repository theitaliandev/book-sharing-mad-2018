package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.algolia.search.saas.Client
import com.algolia.search.saas.Index
import com.example.giuseppedigiorno.booksharing_mad.Adapters.SearchBooksRecyclerAdapter
import com.example.giuseppedigiorno.booksharing_mad.Model.MapData
import com.example.giuseppedigiorno.booksharing_mad.Model.SearchBookItem
import com.example.giuseppedigiorno.booksharing_mad.R
import com.example.giuseppedigiorno.booksharing_mad.Utilities.ALGOLIA_API_KEY
import com.example.giuseppedigiorno.booksharing_mad.Utilities.ALGOLIA_APPLICATION_ID
import com.example.giuseppedigiorno.booksharing_mad.Utilities.EXTRA_MAP
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_search_book.*
import org.json.JSONArray
import org.json.JSONObject

class SearchBookActivity : AppCompatActivity() {

    lateinit var mRecyclerView: RecyclerView
    lateinit var mUserdatabase: DatabaseReference
    lateinit var mGeoDatabase: DatabaseReference
    lateinit var mBookDatabase: DatabaseReference
    lateinit var geoFire: GeoFire
    private var geoQuery: GeoQuery? = null
    private var mCurrentUser: FirebaseUser? = null
    private var userId: String? = null
    private var searchTerm: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var geoLocation: GeoLocation? = null
    var userIdNearby = mutableListOf<String>()
    var mapData = mutableListOf<MapData>()
    var mapDataItem = MapData("", "")
    private var index: Index? = null
    private var client: Client? = null
    private var queryAlgolia: com.algolia.search.saas.Query? = null
    private var jsonArray: JSONArray? = null
    private var jsonObjectAlgolia: JSONObject? = null
    private var searchBookList = ArrayList<SearchBookItem>()
    var i = 0
    var searchBookItem = SearchBookItem("", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_book)
        client = Client(ALGOLIA_APPLICATION_ID, ALGOLIA_API_KEY)
        index = client!!.getIndex("books")
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
            searchBookWithAlgolia(searchTerm!!)
        }else{
            Toast.makeText(this, "Insert the title of the book you are looking for", Toast.LENGTH_LONG).show()
        }
    }

    private fun searchBookWithAlgolia(term: String) {
        searchBookList = ArrayList()
        val adapter = SearchBooksRecyclerAdapter(this, searchBookList) { bookFound ->
        }
        mRecyclerView.adapter = adapter
        queryAlgolia = com.algolia.search.saas.Query(term).setAttributesToRetrieve("title", "author").setHitsPerPage(50)
        index!!.searchAsync(queryAlgolia, { jsonObject, algoliaException ->
            if(jsonObject["nbHits"] != 0) {
                jsonArray = jsonObject.getJSONArray("hits")
                for (i in 0..(jsonArray!!.length() - 1)) {
                    jsonObjectAlgolia = jsonArray!!.getJSONObject(i)
                    searchBookItem.title = jsonObjectAlgolia!!.getString("title")
                    searchBookItem.author = jsonObjectAlgolia!!.getString("author")
                    searchBookList.add(SearchBookItem(searchBookItem.title, searchBookItem.author))
                }
                val adapter = SearchBooksRecyclerAdapter(this, searchBookList) { bookFound ->
                    var bookFoundTitle = bookFound.title
                    val re =  Regex("[^A-Za-z0-9]")
                    val prettyfiedTitle = re.replace(bookFoundTitle!!, "")
                    var i = 0
                    mapData = mutableListOf()
                    mapDataItem = MapData("", "")
                    if(geoLocation != null){
                        userIdNearby = mutableListOf()
                        geoQuery = geoFire.queryAtLocation(geoLocation, 10.0)
                        geoQuery!!.addGeoQueryEventListener( object : GeoQueryEventListener {
                            override fun onGeoQueryReady() {
                                if(userIdNearby.isNotEmpty()){
                                    for(userId in userIdNearby){
                                        mBookDatabase.child(userId).child(prettyfiedTitle)
                                                .addValueEventListener( object : ValueEventListener {
                                                    override fun onCancelled(p0: DatabaseError?) {
                                                    }
                                                    override fun onDataChange(snap: DataSnapshot?) {
                                                        i ++
                                                        if(snap!!.childrenCount > 0) {
                                                            mapDataItem = MapData(userId, snap.child("bookTitle").value.toString())
                                                            mapData.add(mapDataItem)
                                                        }else if (mapData.isNotEmpty() && i == userIdNearby.count()){
                                                            var mapActivity = Intent(this@SearchBookActivity, MapsActivity::class.java)
                                                            mapActivity.putExtra(EXTRA_MAP, ArrayList(mapData))
                                                            startActivity(mapActivity)
                                                        } else if (mapData.isEmpty() && i == userIdNearby.count()){
                                                            Toast.makeText(this@SearchBookActivity, "The book you are looking for is not near to you", Toast.LENGTH_SHORT).show()
                                                        }
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
                mRecyclerView.adapter = adapter
            }
        })
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
