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
            if(auth.currentUser == null){
                auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this) { task->
                        if(task.isSuccessful){
                            successLogin()
                            Log.d("LoginActivity","로그인 성공")
                            checkGroup()
                            finish()
                        }else{
                            Log.d("LoginActivity","로그인 실패")
                            Toast.makeText(this,"로그인에 실패했습니다. 이메일이나 패스워드를 확인해주세요",Toast.LENGTH_LONG).show()
                        }
                    }
            }else{
                finish()
            }
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
            Toast.makeText(this,"로그인에 실패했습니다.",Toast.LENGTH_SHORT).show()
            return
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
                if (userModel?.groupID == null && count) {
                    val dlg = GroupDialog(this@LoginActivity)
                    dlg.start()
                    count = false
                    return
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getCurrentUserID(): String {
        return auth.currentUser?.uid.orEmpty()
    }

}
