package CapStoneDisign.som

import CapStoneDisign.som.Model.GroupModel
import CapStoneDisign.som.Model.UserModel
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import java.util.concurrent.TimeUnit

class DiaryShowDialog : AppCompatActivity() {

    private lateinit var myImageEditButtonInDiary: Button
    private lateinit var myTextEditButtonInDiary: Button
    private lateinit var myDiaryTextView: TextView
    private lateinit var partnerDiaryTextView: TextView
    private lateinit var myImageViewInDiary: ImageView
    private lateinit var partnerImageViewInDiary: ImageView
    private lateinit var myDiaryEditTextView: EditText
    private lateinit var myTextEditCompleteButtonInDiary: Button
    private lateinit var tagView: TextView
    private lateinit var photoWatchTextView: TextView
    private lateinit var placeWatchTextView: TextView
    private lateinit var clickMarkerWatchTextView: TextView

    lateinit var storage: FirebaseStorage


    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private val user = auth.currentUser!!.uid
    private val userDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DBKey.DB_USERS)
    }

    private val groupDB: DatabaseReference by lazy {
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

    private fun initData() {
        initPhoto()
        initText()
    }

    private fun initPhoto() {
        var partnerId: String? = null
        val curruser = userDB.child(user)
        curruser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModel = snapshot.getValue<UserModel>()

                val currentGroup = groupDB.child(userModel?.groupID!!) // groupId에 접근
                currentGroup.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        groupModel = snapshot.getValue<GroupModel>()

                        Log.d("checkiing partner", "${groupModel?.secondUserID}")

                        if (user.compareTo(groupModel?.firstUserID!!) == 0) {     // 내 id와 대조하여 상대의 id 찾아내기
                            partnerId = groupModel?.secondUserID.toString()
                            Log.d("checkiing partner", "${partnerId}")

                        } else {
                            partnerId = groupModel?.firstUserID.toString()

                        }

                        Log.d("checkiing partner", "${partnerId}")
                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.reference

                        var day = intent.getStringExtra("day")
                        var tmpLat = intent.getDoubleExtra("Lat", 0.0)
                        var tmpLong = intent.getDoubleExtra("Long", 0.0)
                        var docName = "$day:$tmpLat:$tmpLong"

                        var fileName = "${user}/${docName}"
                        var fileNameForPartner = "${partnerId!!}/${docName}"

                        storageRef.child("image")
                            .child(fileName).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(myImageViewInDiary)
                        }.addOnFailureListener { //이미지 로드 실패시
                        }

                        storageRef.child("image")
                            .child(fileNameForPartner!!).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
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

    private fun initText() {
        var partnerId: String? = null
        val curruser = userDB.child(user)

        curruser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModel = snapshot.getValue<UserModel>()

                val currentGroup = groupDB.child(userModel?.groupID!!) // groupId에 접근
                currentGroup.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        groupModel = snapshot.getValue<GroupModel>()

                        if (user.compareTo(groupModel?.firstUserID!!) == 0) {     // 내 id와 대조하여 상대의 id 찾아내기
                            partnerId = groupModel?.secondUserID.toString()
                            Log.d("checkiing partner", "${partnerId}")

                        } else {
                            partnerId = groupModel?.firstUserID.toString()
                        }

                        //todo 내 아이디와 partner ID는 찾아 놨으니까
                        //여기서 marker 로 들어가면(마커는 Main 에서 전달해준 marker 이름임) 해당 텍스트가 있을거임
                        //저장하는 로직도 밑에 todo로 달아놨어여 DB 접근만 해주면 됨
                        // 그걸 아래에 text 에 연결해주면 됨
                        // 혹시나 해서 하는 말인데 marker가 여러번 선언되어있긴 한데 전역으로 빼면 오류나니까 빼지 마세여

//                        myDiaryTextView.text =
//                        partnerDiaryTextView.text =

                        // 마커의 날짜
                        var day = intent.getStringExtra("day")
                        // 마커의 위치 (마커 구조를 바꿔서, 날짜와 위치로 마커를 특정해야 할 것 같음.
                        var tmpLat = intent.getDoubleExtra("Lat", 0.0)
                        var tmpLong = intent.getDoubleExtra("Long", 0.0)
                        var docName = "$tmpLat:$tmpLong"

                        db.collection(userModel?.groupID.toString())
                            .document(day.toString())
                            .collection("marker")
                            .document(docName)
                            .get()
                            .addOnSuccessListener { document ->
                                // DB에서 myDiaryTextView 받아오기
                                if (document.get(user) != null) {
                                    myDiaryTextView.text = document.get(user).toString()
                                } else {
                                    Log.d("mylog", "$user 에서 myDiaryTextView 못받아왔어용")
                                }
                                // DB에서 partnerDiaryTextView 받아오기
                                if (document.get(partnerId.toString()) != null) {
                                    partnerDiaryTextView.text =
                                        document.get(partnerId.toString()).toString()
                                } else {
                                    Log.d("mylog", "$partnerId 에서 partnerDiaryTextView 못받아왔어용")
                                }
                            }


                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun initLayout() {
        myTextEditCompleteButtonInDiary = findViewById(R.id.myTextEditCompleteButtonInDiary)
        myTextEditButtonInDiary = findViewById(R.id.myTextEditButtonInDiary)

        myDiaryEditTextView = findViewById(R.id.myDiaryEditTextView)

        myDiaryTextView = findViewById(R.id.myDiaryTextView)
        partnerDiaryTextView = findViewById(R.id.partnerDiaryTextView)

        myImageViewInDiary = findViewById(R.id.myImageViewInDiary)
        partnerImageViewInDiary = findViewById(R.id.partnerImageViewInDiary)

        myImageEditButtonInDiary = findViewById(R.id.myImageEditButtonInDiary)
        val tag = intent.getStringExtra("tag")

        tagView = findViewById(R.id.tagView)
        photoWatchTextView = findViewById(R.id.photoWatchTextView)
        placeWatchTextView = findViewById(R.id.placeWatchTextView)

        clickMarkerWatchTextView = findViewById(R.id.clickMarkerWatchTextView)

        when (tag) {
            "photo" -> {
                tagView.text = "포토존"
                photoWatchTextView.isVisible = true
            }
            "visited" -> {
                tagView.text = "머무른 장소"
                placeWatchTextView.isVisible = true
                var t = intent.getIntExtra("time", 0)
                var time = t.toLong()
                val day = TimeUnit.SECONDS.toDays(time).toInt()
                val hours = TimeUnit.SECONDS.toHours(time) - day * 24
                val minute = TimeUnit.SECONDS.toMinutes(time) - TimeUnit.SECONDS.toHours(time) * 60
                val second =
                    TimeUnit.SECONDS.toSeconds(time) - TimeUnit.SECONDS.toMinutes(time) * 60

                var text1 = ""
                var text2 = ""
                var text3 = ""
                if (hours > 0) {
                    text1 = "${hours}시간 "
                }
                if (minute > 0) {
                    text2 = "${minute}분 "
                }
                if (second > 0) {
                    text3 = "${second}초 "
                }
                placeWatchTextView.text = text1 + text2 + text3 + "동안 머물렀습니다."
            }
            "payment" -> {
                tagView.text = "결제 마커"
            }
            "clicked" -> {
                tagView.text = "클릭 마커"
                clickMarkerWatchTextView.isVisible = true

                //todo 저장한 메모를 clickMarkerWatchTextView.text 에 넣어주기
                clickMarkerWatchTextView.text = intent.getStringExtra("dialog")
            }
        }


        val isEditMode = intent.getBooleanExtra("mode", false)

        if (isEditMode) {
            myImageEditButtonInDiary.isVisible = true
            myTextEditButtonInDiary.isVisible = true
        } else {
            myImageEditButtonInDiary.isVisible = false
            myTextEditButtonInDiary.isVisible = false
        }
    }

    private fun initButtonListener() {

        var day = intent.getStringExtra("day")
        var tmpLat = intent.getDoubleExtra("Lat", 0.0)
        var tmpLong = intent.getDoubleExtra("Long", 0.0)
        var docName = "$day:$tmpLat:$tmpLong"
        var groupID = intent.getStringExtra("groupID")

        var fileName = "${groupID}:photo/$day:$tmpLat:$tmpLong"

        photoWatchTextView.setOnClickListener {
            val intent = Intent(this, ViewPagerActivity::class.java)
            intent.putExtra("fileName", fileName)
            startActivity(intent)
        }

        myImageViewInDiary.setOnClickListener {
            Log.d("MyImageView","clicked")
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference

            var day = intent.getStringExtra("day")
            var tmpLat = intent.getDoubleExtra("Lat", 0.0)
            var tmpLong = intent.getDoubleExtra("Long", 0.0)
            var docName = "$day:$tmpLat:$tmpLong"

            var fileName = "${user}/${docName}"

            storageRef.child("image")
                .child(fileName).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
                    val intent = Intent(this@DiaryShowDialog, MarkerImageActivity::class.java)
                    intent.putExtra("imageUri",uri.toString())
                    startActivity(intent)


//                Glide.with(applicationContext)
//                    .load(uri)
//                    .into(uploadedImageView)
            }.addOnFailureListener { //이미지 로드 실패시
            }
        }

        partnerImageViewInDiary.setOnClickListener {
            var partnerId: String? = null
            val curruser = userDB.child(user)
            curruser.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userModel = snapshot.getValue<UserModel>()

                    val currentGroup = groupDB.child(userModel?.groupID!!) // groupId에 접근
                    currentGroup.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            groupModel = snapshot.getValue<GroupModel>()

                            Log.d("checkiing partner", "${groupModel?.secondUserID}")

                            if (user.compareTo(groupModel?.firstUserID!!) == 0) {     // 내 id와 대조하여 상대의 id 찾아내기
                                partnerId = groupModel?.secondUserID.toString()
                                Log.d("checkiing partner", "${partnerId}")

                            } else {
                                partnerId = groupModel?.firstUserID.toString()
                            }

                            val storage = FirebaseStorage.getInstance()
                            val storageRef = storage.reference

                            var day = intent.getStringExtra("day")
                            var tmpLat = intent.getDoubleExtra("Lat", 0.0)
                            var tmpLong = intent.getDoubleExtra("Long", 0.0)
                            var docName = "$day:$tmpLat:$tmpLong"

                            var fileNameForPartner = "${partnerId!!}/${docName}"

                            storageRef.child("image")
                                .child(fileNameForPartner!!).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
                                    val intent = Intent(this@DiaryShowDialog, MarkerImageActivity::class.java)
                                    intent.putExtra("imageUri",uri.toString())
                                    startActivity(intent)
                                }.addOnFailureListener { //이미지 로드 실패시
                                }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        }

        myImageEditButtonInDiary.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    navigatePhotos()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }
                else -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        1000
                    )
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

            Toast.makeText(this, "내용을 수정할 수 있습니다!", Toast.LENGTH_SHORT).show()
        }

        myTextEditCompleteButtonInDiary.setOnClickListener {
            val text: String = myDiaryEditTextView.text.toString()
            myDiaryTextView.text = text
            myDiaryTextView.isVisible = true
            myDiaryEditTextView.isVisible = false

            myTextEditButtonInDiary.isVisible = true
            myTextEditCompleteButtonInDiary.isVisible = false

            //todo text 저장은 어차피 내 것만 수정할 수 있게 해놔서
            //DB 폴더가 내 ID -> 오늘 날짜 -> marker 순서가 되게 해서 저장해주면 됨
            //오늘 날짜 안에 그날의 마커 이름들 저장하는 방식
            // 해서 내가 맨 위에 있는 text 변수에 String 은 다 넣어 놨음
            // DB 접근해서 할당만 해주세요

            // 마커 정보 찾아가기 위한 정보들
            var day = intent.getStringExtra("day")
            var tmpLat = intent.getDoubleExtra("Lat", 0.0)
            var tmpLong = intent.getDoubleExtra("Long", 0.0)
            var docName = "$tmpLat:$tmpLong"


            // 파이어스토어에 전달해 줄 해시맵 생성
            // wrote는 텍스트가 입력된 적 있는 애만 가지는 필드임.
            var wrote = 1
            val toWrite = hashMapOf(
                "$user" to text,
                "wrote" to wrote
            )
            // 파이어스토어에 작성하는 부분
            val curruser = userDB.child(user)
            curruser.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userModel = snapshot.getValue<UserModel>()

                    //컬렉션: 내 ID, 다큐먼트: 날짜와 시간과 마커 번호, 내용: 텍스트
                    Log.d("Mylog", "그룹 ID: ${userModel?.groupID}")
                    Log.d("Mylog", "날짜: $day")
                    Log.d("Mylog", "마커위치: $docName")
                    Log.d("Mylog", "쓸 텍스트: $text")
                    db.collection(userModel?.groupID.toString())
                        .document(day.toString())
                        .collection("marker")
                        .document(docName)
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

            Toast.makeText(this, "내용이 수정되었습니다!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun navigatePhotos() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        storage = FirebaseStorage.getInstance()
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            1000 -> {
                val selectedImageUri: Uri? = data?.data

                if (selectedImageUri != null) {

                    Log.d("checkingPhoto", "${selectedImageUri}")
                    selectedImageUri.let {

                        Glide.with(this)
                            .load(selectedImageUri)
                            .fitCenter()
                            .apply(RequestOptions().override(500, 500))
                            .into(myImageViewInDiary)
                    }


                    var day = intent.getStringExtra("day")
                    var tmpLat = intent.getDoubleExtra("Lat", 0.0)
                    var tmpLong = intent.getDoubleExtra("Long", 0.0)
                    var docName = "$day:$tmpLat:$tmpLong"

                    var fileName = "${user}/${docName}"
                    Log.d("markerFileName", docName)
                    storage.getReference().child("image").child(fileName).delete()
                    storage.getReference().child("image").child(fileName)
                        .putFile(selectedImageUri)

                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }

            }
            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다")
            .setMessage("앱에서 사진을 불러오기 위해 권한이 필요합니다")
            .setPositiveButton("동의하기") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()
    }

}
