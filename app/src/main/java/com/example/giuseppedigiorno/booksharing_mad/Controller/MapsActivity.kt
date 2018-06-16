package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.giuseppedigiorno.booksharing_mad.Model.MapData
import com.example.giuseppedigiorno.booksharing_mad.R
import com.example.giuseppedigiorno.booksharing_mad.Utilities.EXTRA_MAP

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    lateinit var mapData: MutableList<MapData>
    private var mCurrentUser: FirebaseUser? = null
    private var mDatabase: DatabaseReference? = null
    private var currentUserId: String? = null
    private var currentUserName: String? = null
    var latitude: Double? = null
    var longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapData = intent.getParcelableArrayListExtra(EXTRA_MAP)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        loadUserPosition()
        loadFoundBook()
    }

    private fun loadUserPosition() {
        mCurrentUser = FirebaseAuth.getInstance().currentUser
        currentUserId = mCurrentUser!!.uid
        mDatabase = FirebaseDatabase.getInstance().reference
                .child("users").child(currentUserId)
        mDatabase!!.addValueEventListener( object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot?) {
                latitude = snap!!.child("latitude").value.toString().toDouble()
                longitude = snap.child("longitude").value.toString().toDouble()
                currentUserName = snap.child("name").value.toString()
                val userPosition = LatLng(latitude!!, longitude!!)
                mMap.addMarker(MarkerOptions().position(userPosition).title(getString(R.string.am_here)))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userPosition))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12f))
            }
            override fun onCancelled(p0: DatabaseError?) {
            }

        })
    }

    private fun loadFoundBook() {
        for(item in mapData) {
            mDatabase = FirebaseDatabase.getInstance().reference
                    .child("users").child(item.userId)
            mDatabase!!.addValueEventListener( object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot?) {
                    latitude = snap!!.child("latitude").value.toString().toDouble()
                    longitude = snap!!.child("longitude").value.toString().toDouble()
                    var name = snap!!.child("name").value.toString()
                    val bookFoundPosition = LatLng(latitude!!, longitude!!)
                    val marker = mMap.addMarker(MarkerOptions().position(bookFoundPosition)
                            .title(name)
                            .snippet(item.bookTitle))
                    marker.tag = item.userId
                    mMap.setOnInfoWindowClickListener { marker ->
                        if(marker.title.equals(getString(R.string.am_here))) {
                            return@setOnInfoWindowClickListener
                        }else{
                            val mapDetailActivity = Intent(this@MapsActivity, MapDetailActivity::class.java)
                            mapDetailActivity.putExtra("bookTitle", marker.snippet)
                            mapDetailActivity.putExtra("userName", marker.title)
                            mapDetailActivity.putExtra("userId", marker.tag.toString())
                            mapDetailActivity.putExtra("currentUserId", currentUserId)
                            mapDetailActivity.putExtra("currentUserName", currentUserName)
                            startActivity(mapDetailActivity)
                            return@setOnInfoWindowClickListener
                        }
                    }
                }
                override fun onCancelled(p0: DatabaseError?) {
                }

            })
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        var searchBookActivity = Intent(this, SearchBookActivity::class.java)
        startActivity(searchBookActivity)
    }

}
