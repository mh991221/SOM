package CapStoneDisign.som

import CapStoneDisign.som.DBKey.Companion.DB_GROUPS
import CapStoneDisign.som.DBKey.Companion.DB_USERS
import CapStoneDisign.som.Model.GroupModel
import CapStoneDisign.som.Model.UserModel
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*


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

    private val accountInfoImageView :ImageView by lazy{
        findViewById(R.id.accountInfoImageView)
    }
    private val accountInfoImageViewPartner :ImageView by lazy{
        findViewById(R.id.accountInfoImageViewPartner)
    }

    var userModel: UserModel? = null
    var userModel2: UserModel?=null
    var groupModel: GroupModel? = null

    var count: Int = 0
    lateinit var storage: FirebaseStorage

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



        initGroup()
        initDate()
        initPhoto()
    }

    private fun initPhoto(){

        editPhotoButton.setOnClickListener {
            when{
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED ->{
                    navigatePhotos()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) ->{
                    showPermissionContextPopup()
                }
                else ->{
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
                }
            }
        }


        var partnerId: String?=null
        val curruser = userDB.child(user)
        curruser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModel = snapshot.getValue<UserModel>()

                val currentGroup = groupDB.child(userModel?.groupID!!) // groupId에 접근
                currentGroup.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        groupModel = snapshot.getValue<GroupModel>()

                        Log.d("checkiing partner","${groupModel?.secondUserID}")

                        if(user.compareTo(groupModel?.firstUserID!!) == 0){     // 내 id와 대조하여 상대의 id 찾아내기
                            partnerId = groupModel?.secondUserID.toString()
                            Log.d("checkiing partner","${partnerId}")

                        }else{
                            partnerId = groupModel?.firstUserID.toString()

                        }

                        Log.d("checkiing partner","${partnerId}")
                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.reference

                        storageRef.child("image").child(user).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(accountInfoImageView)
                        }.addOnFailureListener { //이미지 로드 실패시
                        }

                        storageRef.child("image").child(partnerId!!).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(accountInfoImageViewPartner)
                        }.addOnFailureListener { //이미지 로드 실패시
                        }

                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })






    }

    private fun navigatePhotos(){
        val intent  = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent,2000)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        storage= FirebaseStorage.getInstance()
        if(resultCode != Activity.RESULT_OK){
            return
        }
        when(requestCode){
            2000 ->{
                val selectedImageUri : Uri? = data?.data

                if(selectedImageUri != null){

                    Log.d("checkingPhoto","${selectedImageUri}")
                    selectedImageUri.let{

                        Glide.with(this)
                            .load(selectedImageUri)
                            .fitCenter()
                            .apply(RequestOptions().override(500,500))
                            .into(accountInfoImageView)
                    }

                    var fileName = user
                    storage.getReference().child("image").child(fileName).delete()
                    storage.getReference().child("image").child(fileName)
                        .putFile(selectedImageUri)



                }else{
                    Toast.makeText(this,"사진을 가져오지 못했습니다.",Toast.LENGTH_SHORT).show()
                }

            }
            else ->{
                Toast.makeText(this,"사진을 가져오지 못했습니다.",Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun showPermissionContextPopup(){
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다")
            .setMessage("앱에서 사진을 불러오기 위해 권한이 필요합니다")
            .setPositiveButton("동의하기") {_, _->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            }
            .setNegativeButton("취소하기") {_, _-> }
            .create()
            .show()
    }

    private fun initGroup(){
        val nameTextView = findViewById<TextView>(R.id.nameTextView)
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val phoneNumberTextView = findViewById<TextView>(R.id.phoneNumberTextView)
        val nameTextViewPartner = findViewById<TextView>(R.id.nameTextViewPartner)

        var partnerId: String?=null
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


                        partnerId = if(user.compareTo(groupModel?.firstUserID!!) == 0){     // 내 id와 대조하여 상대의 id 찾아내기
                            groupModel?.secondUserID.toString()
                        }else{
                            groupModel?.firstUserID.toString()
                        }

                        val partnerUser = userDB.child(partnerId!!)                       // 파트너의 id에 접근하여 이름 알아내기
                        partnerUser.addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                userModel2 = snapshot.getValue<UserModel>()

                                nameTextViewPartner.text = userModel2?.name
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


        var currentGroup = Firebase.database.reference

        var startDate:Double?=null




        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,0)
            set(Calendar.MINUTE,0)
            set(Calendar.SECOND,0)
            set(Calendar.MILLISECOND,0)
        }.timeInMillis

        val curruser = userDB.child(user)
        var groupID: String? = null
        curruser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModel = snapshot.getValue<UserModel>()

                groupID = userModel?.groupID
                currentGroup = groupDB.child(userModel?.groupID!!) // groupId에 접근
                currentGroup.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        groupModel = snapshot.getValue<GroupModel>()

                        Log.d("checking a data", "${groupModel?.firstUserID}")
                        startDate = groupModel?.startDate

                        val year = groupModel?.year
                        val month = groupModel?.month
                        val dayOfMonth = groupModel?.dayOfMonth


                        if(startDate != 0.0){
                            val fewDaysInMillis = today - startDate!!
                            val fewDays = (fewDaysInMillis/(24*60*60*1000) + 1).toInt()

                            dateTextView.text = fewDays.toString()
                        }

                        if(year!=null && month!=null && dayOfMonth!=null){
                            startDateTextView.text = "${year}.${month+1}.${dayOfMonth}~"
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })




        changeStartDateButton.setOnClickListener {

            val cal = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener{_, year, month, dayOfMonth->
                startDateTextView.text = "${year}.${month+1}.${dayOfMonth}~"
                count = 0

                curruser.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userModel = snapshot.getValue<UserModel>()

                        groupID = userModel?.groupID
                        currentGroup = groupDB.child(userModel?.groupID!!) // groupId에 접근
                        currentGroup.addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                groupModel = snapshot.getValue<GroupModel>()

                                Log.d("checking", "${groupID}")
                                if(count == 0){
                                    groupDB.child(groupID!!).child("year").setValue(year)
                                    groupDB.child(groupID!!).child("month").setValue(month)
                                    groupDB.child(groupID!!).child("dayOfMonth").setValue(dayOfMonth)
                                    count = 1
                                }

                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })



                val startDay = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH,month)
                    set(Calendar.DAY_OF_MONTH,dayOfMonth)
                    set(Calendar.HOUR_OF_DAY,0)
                    set(Calendar.MINUTE,0)
                    set(Calendar.SECOND,0)
                    set(Calendar.MILLISECOND,0)
                }.timeInMillis

                val fewDaysInMillis = today - startDay
                val fewDays = fewDaysInMillis/(24*60*60*1000) + 1
                groupDB.child(groupID!!).child("startDate").setValue(startDay)
                dateTextView.text = fewDays.toString()
            }
            DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()

        }

    }



}