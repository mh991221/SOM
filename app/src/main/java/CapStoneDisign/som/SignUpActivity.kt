package CapStoneDisign.som

import CapStoneDisign.som.DBKey.Companion.DB_USERS
import CapStoneDisign.som.Model.UserModel
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignUpActivity:AppCompatActivity() {

    private val signUpNameEditText: EditText by lazy{
        findViewById(R.id.signUpNameEditText)
    }

    private val signUpEmailEditText: EditText by lazy{
        findViewById(R.id.signUpEmailEditText)
    }

    private val signUpPasswordEditText: EditText by lazy{
        findViewById(R.id.signUpPasswordEditText)
    }

    private val signUpPhoneNumberEditText: EditText by lazy{
        findViewById(R.id.signUpPhoneNumberEditText)
    }

    private val signUpButton: Button by lazy{
        findViewById(R.id.signUpButton)
    }

    private val backButton: Button by lazy{
        findViewById(R.id.backButton)
    }

    private val auth: FirebaseAuth by lazy{
        Firebase.auth
    }

    private val userDB: DatabaseReference by lazy{
        Firebase.database.reference.child(DB_USERS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_layout)

        backButton.setOnClickListener {
            finish()
        }

        signUpButton.setOnClickListener {
            val name = signUpNameEditText.text?.toString().orEmpty()
            val email = signUpEmailEditText.text?.toString().orEmpty()
            val password = signUpPasswordEditText.text?.toString().orEmpty()
            val phoneNumber = signUpPhoneNumberEditText.text?.toString().orEmpty()

            if(emptyCheck(name, email, password, phoneNumber)){
                val model = UserModel(name, email, password, phoneNumber, null)

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener{ task ->
                        if(task.isSuccessful){
                            userDB.child(auth.currentUser?.uid.orEmpty()).setValue(model)
                            Toast.makeText(this,"회원가입에 성공했습니다. 로그인 버튼을 통해 로그인해주세요", Toast.LENGTH_LONG).show()
                            finish()
                        }else{
                            Toast.makeText(this,"이미 가입한 이메일이거나 없는 이메일입니다. 다시 한번 확인해주세요",Toast.LENGTH_LONG).show()
                        }
                    }
            }

        }
    }

    private fun emptyCheck(name: String?, email: String?, password: String?, phoneNumber: String?) : Boolean{
        if(name.isNullOrEmpty()){
            Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }else if(email.isNullOrEmpty()){
            Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }else if(password.isNullOrEmpty()){
            Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }else if(phoneNumber.isNullOrEmpty()){
            Toast.makeText(this, "전화번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

}