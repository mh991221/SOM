package CapStoneDisign.som

import CapStoneDisign.som.Model.UserModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class LoginActivity:AppCompatActivity() {

    private val userDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DBKey.DB_USERS)
    }

    private val loginEmailEditText: EditText by lazy{
        findViewById(R.id.loginEmailEditText)
    }

    private val passwordEditText: EditText by lazy{
        findViewById(R.id.loginPasswordEditText)
    }

    private val loginButton: Button by lazy{
        findViewById(R.id.loginButton)
    }

    private val signUpButton: Button by lazy{
        findViewById(R.id.signUpButtonInLogin)
    }

    private val auth: FirebaseAuth by lazy{
        Firebase.auth
    }

    private var count: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        initLoginButton()
        initSignUpButton()
    }

    private fun initLoginButton(){

        loginEmailEditText.addTextChangedListener {
            val enable = loginEmailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()
            loginButton.isEnabled = enable
        }
        passwordEditText.addTextChangedListener {
            val enable = loginEmailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()
            loginButton.isEnabled = enable
        }

        loginButton.setOnClickListener {
            val email = loginEmailEditText.text.toString()
            val password = passwordEditText.text.toString()

                auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener{ task->
                        if(task.isSuccessful){
                            successLogin()
                            Log.d("LoginActivity","????????? ??????")
                            checkGroup()
                        }else{
                            Log.d("LoginActivity","????????? ??????")
                            Toast.makeText(this,"???????????? ??????????????????. ??????????????? ??????????????? ??????????????????",Toast.LENGTH_LONG).show()
                        }
                    }

            val currentGroup = userDB.child(getCurrentUserID())
            currentGroup.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val userModel = snapshot.getValue<UserModel>()
                    Log.d("tlqkf","${userModel?.email}, ${userModel?.groupID}")
                    if (userModel?.groupID != null ) {
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun initSignUpButton(){
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun successLogin(){
        if(auth.currentUser == null){
            Toast.makeText(this,"???????????? ??????????????????.",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }


    private fun checkGroup() {
        val currentGroup = userDB.child(getCurrentUserID())
        currentGroup.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val userModel = snapshot.getValue<UserModel>()
                Log.d("tlqkf","${userModel?.email}, ${userModel?.groupID}")
                if (userModel?.groupID == null && count) {
                    val dlg = GroupDialog(this@LoginActivity)
                    Log.d("tlqkf","${userModel?.email}, ${userModel?.groupID}")
                    dlg.start()
                    count = false
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getCurrentUserID(): String {
        return auth.currentUser?.uid.orEmpty()
    }

}
