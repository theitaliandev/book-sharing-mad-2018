package com.example.giuseppedigiorno.booksharing_mad.Controller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.giuseppedigiorno.booksharing_mad.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth: FirebaseAuth ->
            user = firebaseAuth.currentUser
            if(user!=null){
                startActivity(Intent(this, ShowProfileActivity::class.java))
            }
        }
    }

    fun loginButtonPressed(view: View) {
        var email = loginEmailEditText.text.toString().trim()
        var password = loginPasswordEditText.text.toString().trim()
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            loginUser(email, password)
        }else{
            Toast.makeText(this, getString(R.string.insert_email_and_password), Toast.LENGTH_LONG).show()
        }
    }

    fun registerHerePressed(view: View) {
        val registerActivity = Intent(this, RegisterActivity::class.java)
        startActivity(registerActivity)
    }

    private fun loginUser(email: String, password: String) {
        mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        var showProfileActivity = Intent(this, ShowProfileActivity::class.java)
                        startActivity(showProfileActivity)
                    }else{
                        Toast.makeText(this, getString(R.string.correct_email_and_password), Toast.LENGTH_LONG).show()
                    }
                }
    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    override fun onStop() {
        super.onStop()
        if(mAuthListener!=null){
            mAuth!!.removeAuthStateListener(mAuthListener!!)
        }
    }
}
