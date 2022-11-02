package CapStoneDisign.som

import CapStoneDisign.som.DBKey.Companion.DB_USERS
import CapStoneDisign.som.Model.UserModel
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import com.google.firebase.ktx.Firebase

class AccountInfoActivity : AppCompatActivity() {

    private val accountBackButton: Button by lazy{
        findViewById(R.id.accountBackButton)
    }

    private val logoutButton: Button by lazy{
        findViewById(R.id.logoutButton)
    }

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private val user = auth.currentUser!!.uid

    private val userDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_USERS)
    }

    var userModel: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_info)

        accountBackButton.setOnClickListener {
            finish()
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val nameTextView = findViewById<TextView>(R.id.nameTextView)
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val phoneNumberTextView = findViewById<TextView>(R.id.phoneNumberTextView)
/*
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userModel = dataSnapshot.getValue<UserModel>()

                nameTextView.text = userModel?.name
                emailTextView.text = userModel?.email
                phoneNumberTextView.text = userModel?.phoneNumber
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        userDB.child(user).addValueEventListener(postListener)*/

        val curruser = userDB.child(user)
        curruser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModel = snapshot.getValue<UserModel>()

                nameTextView.text = userModel?.name
                emailTextView.text = userModel?.email
                phoneNumberTextView.text = userModel?.phoneNumber
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}