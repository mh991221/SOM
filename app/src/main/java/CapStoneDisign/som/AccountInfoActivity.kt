package CapStoneDisign.som

import CapStoneDisign.som.DBKey.Companion.DB_GROUPS
import CapStoneDisign.som.DBKey.Companion.DB_USERS
import CapStoneDisign.som.Model.GroupModel
import CapStoneDisign.som.Model.UserModel
import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Calendar

class AccountInfoActivity : AppCompatActivity() {

    private val accountBackButton: Button by lazy{
        findViewById(R.id.accountBackButton)
    }

    private val logoutButton: Button by lazy{
        findViewById(R.id.logoutButton)
    }

    private val changeStartDateButton: Button by lazy{
        findViewById(R.id.changeStartDateButton)
    }

    private val editPhotoButton: Button by lazy{
        findViewById(R.id.editPhotoButton)
    }

    private val editPartnerPhotoButton: Button by lazy{
        findViewById(R.id.editPartnerPhotoButton)
    }

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private val user = auth.currentUser!!.uid

    private val userDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_USERS)
    }

    private val groupDB: DatabaseReference by lazy{
        Firebase.database.reference.child(DB_GROUPS)
    }

    var userModel: UserModel? = null
    var groupModel: GroupModel? = null

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

        initPhoto()
        initGroup()
        initDate()
    }

    private fun initPhoto(){

    }

    private fun initGroup(){
        val nameTextView = findViewById<TextView>(R.id.nameTextView)
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val phoneNumberTextView = findViewById<TextView>(R.id.phoneNumberTextView)
        val nameTextViewPartner = findViewById<TextView>(R.id.nameTextViewPartner)

        val curruser = userDB.child(user)
        curruser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModel = snapshot.getValue<UserModel>()

                nameTextView.text = userModel?.name
                emailTextView.text = userModel?.email
                phoneNumberTextView.text = userModel?.phoneNumber

                val currentGroup = groupDB.child(userModel?.groupID!!) // groupId에 접근
                currentGroup.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        groupModel = snapshot.getValue<GroupModel>()
                        var partnerId: String?=null

                        partnerId = if(user.compareTo(groupModel?.firstUserID!!) == 0){     // 내 id와 대조하여 상대의 id 찾아내기
                            groupModel?.secondUserID.toString()
                        }else{
                            groupModel?.firstUserID.toString()
                        }

                        val partnerUser = userDB.child(partnerId)                       // 파트너의 id에 접근하여 이름 알아내기
                        partnerUser.addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                userModel = snapshot.getValue<UserModel>()

                                nameTextViewPartner.text = userModel?.name
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }


    @SuppressLint("SetTextI18n")
    private fun initDate(){
        val dateTextView = findViewById<TextView>(R.id.dateTextView)
        val startDateTextView = findViewById<TextView>(R.id.startDateTextView)

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,0)
            set(Calendar.MINUTE,0)
            set(Calendar.SECOND,0)
            set(Calendar.MILLISECOND,0)
        }.time.time

        changeStartDateButton.setOnClickListener {

            val cal = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener{_, year, month, dayOfMonth->
                startDateTextView.text = "${year}.${month+1}.${dayOfMonth}~"
            }
            DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()

        }

    }


}