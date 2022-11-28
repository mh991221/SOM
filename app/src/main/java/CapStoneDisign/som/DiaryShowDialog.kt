package CapStoneDisign.som

import CapStoneDisign.som.Model.GroupModel
import CapStoneDisign.som.Model.UserModel
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate

class DiaryShowDialog:AppCompatActivity() {

    private lateinit var myImageEditButtonInDiary: Button
    private lateinit var myTextEditButtonInDiary: Button
    private lateinit var myDiaryTextView: TextView
    private lateinit var partnerDiaryTextView: TextView
    private lateinit var myImageViewInDiary: ImageView
    private lateinit var partnerImageViewInDiary: ImageView
    private lateinit var myDiaryEditTextView: EditText
    private lateinit var myTextEditCompleteButtonInDiary:Button
    lateinit var storage: FirebaseStorage

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private val user = auth.currentUser!!.uid
    private val userDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DBKey.DB_USERS)
    }

    private val groupDB: DatabaseReference by lazy{
        Firebase.database.reference.child(DBKey.DB_GROUPS)
    }

    var userModel: UserModel? = null
    var groupModel: GroupModel? = null

    // 파이어스토어db
    val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.marker_dialog_layout)


        initLayout()
        initButtonListener()
        initData()

    }

    private fun initData(){
        initPhoto()
        initText()
    }

    private fun initPhoto(){
        var partnerId: String?=null
        val curruser = userDB.child(user)
        curruser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModel = snapshot.getValue<UserModel>()

                val currentGroup = groupDB.child(userModel?.groupID!!) // groupId에 접근
                currentGroup.addValueEventListener(object: ValueEventListener {
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

                        var marker = intent.getStringExtra("marker")
                        var fileName = "${user}/${marker}"
                        var fileNameForPartner = "${partnerId!!}/${marker}"

                        storageRef.child("image").child(fileName).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(myImageViewInDiary)
                        }.addOnFailureListener { //이미지 로드 실패시
                        }

                        storageRef.child("image").child(fileNameForPartner!!).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(partnerImageViewInDiary)
                        }.addOnFailureListener { //이미지 로드 실패시
                        }

                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun initText(){
        var partnerId: String?=null
        val curruser = userDB.child(user)

        curruser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModel = snapshot.getValue<UserModel>()

                val currentGroup = groupDB.child(userModel?.groupID!!) // groupId에 접근
                currentGroup.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        groupModel = snapshot.getValue<GroupModel>()

                        if(user.compareTo(groupModel?.firstUserID!!) == 0){     // 내 id와 대조하여 상대의 id 찾아내기
                            partnerId = groupModel?.secondUserID.toString()
                            Log.d("checkiing partner","${partnerId}")

                        }else{
                            partnerId = groupModel?.firstUserID.toString()
                        }

                        var marker = intent.getStringExtra("marker")
                        //todo 내 아이디와 partner ID는 찾아 놨으니까
                        //여기서 marker 로 들어가면(마커는 Main 에서 전달해준 marker 이름임) 해당 텍스트가 있을거임
                        //저장하는 로직도 밑에 todo로 달아놨어여 DB 접근만 해주면 됨
                        // 그걸 아래에 text 에 연결해주면 됨
                        // 혹시나 해서 하는 말인데 marker가 여러번 선언되어있긴 한데 전역으로 빼면 오류나니까 빼지 마세여

//                        myDiaryTextView.text =
//                        partnerDiaryTextView.text =

                        // DB에서 myDiaryTextView 받아오기
                        db.collection(user).document(marker.toString()).get()
                            .addOnSuccessListener{ document ->
                                if (document.get("text") != null) {
                                    myDiaryTextView.text = document.get("text").toString()
                                }
                                else {
                                    Log.d("mylog","$user 에서 myDiaryTextView 못받아왔어용")
                                }
                            }

                        // DB에서 partnerDiaryTextView 받아오기
                        // 파트너 없으면 안 받아옵니다
                        if (partnerId != null) {
                            db.collection(partnerId!!).document(marker.toString()).get()
                                .addOnSuccessListener{ document ->
                                    if (document.get("text") != null) {
                                        partnerDiaryTextView.text = document.get("text").toString()
                                    }
                                    else {
                                        Log.d("mylog","$partnerId 에서 partnerDiaryTextView 못받아왔어용")
                                    }
                                }
                        }

                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun initLayout(){
        myTextEditCompleteButtonInDiary = findViewById(R.id.myTextEditCompleteButtonInDiary)
        myTextEditButtonInDiary = findViewById(R.id.myTextEditButtonInDiary)

        myDiaryEditTextView = findViewById(R.id.myDiaryEditTextView)

        myDiaryTextView = findViewById(R.id.myDiaryTextView)
        partnerDiaryTextView = findViewById(R.id.partnerDiaryTextView)

        myImageViewInDiary = findViewById(R.id.myImageViewInDiary)
        partnerImageViewInDiary = findViewById(R.id.partnerImageViewInDiary)

        myImageEditButtonInDiary = findViewById(R.id.myImageEditButtonInDiary)
    }

    private fun initButtonListener(){
        myImageEditButtonInDiary.setOnClickListener {
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

        myTextEditButtonInDiary.setOnClickListener {
            val text: String = myDiaryTextView.text.toString()
            myDiaryEditTextView.setText(text)
            myDiaryTextView.isVisible = false
            myDiaryEditTextView.isVisible = true

            myTextEditButtonInDiary.isVisible = false
            myTextEditCompleteButtonInDiary.isVisible = true

            Toast.makeText(this,"내용을 수정할 수 있습니다!",Toast.LENGTH_SHORT).show()
        }

        myTextEditCompleteButtonInDiary.setOnClickListener {
            val text: String = myDiaryEditTextView.text.toString()
            myDiaryTextView.text = text
            myDiaryTextView.isVisible = true
            myDiaryEditTextView.isVisible = false

            myTextEditButtonInDiary.isVisible = true
            myTextEditCompleteButtonInDiary.isVisible = false

            var marker = intent.getStringExtra("marker")
            //todo text 저장은 어차피 내 것만 수정할 수 있게 해놔서
            //DB 폴더가 내 ID -> 오늘 날짜 -> marker 순서가 되게 해서 저장해주면 됨
            //오늘 날짜 안에 그날의 마커 이름들 저장하는 방식
            // 해서 내가 맨 위에 있는 text 변수에 String 은 다 넣어 놨음
            // DB 접근해서 할당만 해주세요

            // 파이어스토어에 전달해 줄 해시맵 생성
            val toWrite = hashMapOf(
                "text" to text
            )
            // 파이어스토어에 작성하는 부분
            val curruser = userDB.child(user)
            curruser.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //컬렉션: 내 ID, 다큐먼트: 날짜와 시간과 마커 번호, 내용: 텍스트
                    Log.d("Mylog", "내 ID: $user")
                    Log.d("Mylog", "날짜와 마커번호: ${marker.toString()}")
                    Log.d("Mylog", "쓸 텍스트: $text")
                    db.collection(user)
                        .document(marker.toString())
                        .set(toWrite, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("Mylog", "마커에 텍스트 저장 완료!")
                        }
                        .addOnFailureListener { e ->
                            Log.w(
                                "MyLog",
                                "마커에 텍스트 저장 실패!",
                                e
                            )
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Mylog", "텍스트 입력 때 뭔가 잘못됨!")
                }
            })

            Toast.makeText(this,"내용이 수정되었습니다!",Toast.LENGTH_SHORT).show()
        }

    }

    private fun navigatePhotos(){
        val intent  = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent,1000)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        storage= FirebaseStorage.getInstance()
        if(resultCode != Activity.RESULT_OK){
            return
        }
        when(requestCode){
            1000 ->{
                val selectedImageUri : Uri? = data?.data

                if(selectedImageUri != null){

                    Log.d("checkingPhoto","${selectedImageUri}")
                    selectedImageUri.let{

                        Glide.with(this)
                            .load(selectedImageUri)
                            .fitCenter()
                            .apply(RequestOptions().override(500,500))
                            .into(myImageViewInDiary)
                    }


                    var marker = intent.getStringExtra("marker")
                    var fileName = "${user}/${marker}"

                    Log.d("markerFileName",fileName)
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

}