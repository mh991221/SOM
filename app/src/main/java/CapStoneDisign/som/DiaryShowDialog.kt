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

    // ??????????????????db
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

                val currentGroup = groupDB.child(userModel?.groupID!!) // groupId??? ??????
                currentGroup.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        groupModel = snapshot.getValue<GroupModel>()

                        Log.d("checkiing partner", "${groupModel?.secondUserID}")

                        if (user.compareTo(groupModel?.firstUserID!!) == 0) {     // ??? id??? ???????????? ????????? id ????????????
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
                            .child(fileName).downloadUrl.addOnSuccessListener { uri -> //????????? ?????? ?????????
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(myImageViewInDiary)
                        }.addOnFailureListener { //????????? ?????? ?????????
                        }

                        storageRef.child("image")
                            .child(fileNameForPartner!!).downloadUrl.addOnSuccessListener { uri -> //????????? ?????? ?????????
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(partnerImageViewInDiary)
                        }.addOnFailureListener { //????????? ?????? ?????????
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

                val currentGroup = groupDB.child(userModel?.groupID!!) // groupId??? ??????
                currentGroup.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        groupModel = snapshot.getValue<GroupModel>()

                        if (user.compareTo(groupModel?.firstUserID!!) == 0) {     // ??? id??? ???????????? ????????? id ????????????
                            partnerId = groupModel?.secondUserID.toString()
                            Log.d("checkiing partner", "${partnerId}")

                        } else {
                            partnerId = groupModel?.firstUserID.toString()
                        }

                        //todo ??? ???????????? partner ID??? ?????? ????????????
                        //????????? marker ??? ????????????(????????? Main ?????? ???????????? marker ?????????) ?????? ???????????? ????????????
                        //???????????? ????????? ?????? todo??? ??????????????? DB ????????? ????????? ???
                        // ?????? ????????? text ??? ??????????????? ???
                        // ????????? ?????? ?????? ????????? marker??? ????????? ?????????????????? ?????? ???????????? ?????? ??????????????? ?????? ?????????

//                        myDiaryTextView.text =
//                        partnerDiaryTextView.text =

                        // ????????? ??????
                        var day = intent.getStringExtra("day")
                        // ????????? ?????? (?????? ????????? ?????????, ????????? ????????? ????????? ???????????? ??? ??? ??????.
                        var tmpLat = intent.getDoubleExtra("Lat", 0.0)
                        var tmpLong = intent.getDoubleExtra("Long", 0.0)
                        var docName = "$tmpLat:$tmpLong"

                        db.collection(userModel?.groupID.toString())
                            .document(day.toString())
                            .collection("marker")
                            .document(docName)
                            .get()
                            .addOnSuccessListener { document ->
                                // DB?????? myDiaryTextView ????????????
                                if (document.get(user) != null) {
                                    myDiaryTextView.text = document.get(user).toString()
                                } else {
                                    Log.d("mylog", "$user ?????? myDiaryTextView ??????????????????")
                                }
                                // DB?????? partnerDiaryTextView ????????????
                                if (document.get(partnerId.toString()) != null) {
                                    partnerDiaryTextView.text =
                                        document.get(partnerId.toString()).toString()
                                } else {
                                    Log.d("mylog", "$partnerId ?????? partnerDiaryTextView ??????????????????")
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
                tagView.text = "?????????"
                photoWatchTextView.isVisible = true
            }
            "visited" -> {
                tagView.text = "????????? ??????"
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
                    text1 = "${hours}?????? "
                }
                if (minute > 0) {
                    text2 = "${minute}??? "
                }
                if (second > 0) {
                    text3 = "${second}??? "
                }
                placeWatchTextView.text = text1 + text2 + text3 + "?????? ??????????????????."
            }
            "payment" -> {
                tagView.text = "?????? ??????"
            }
            "clicked" -> {
                tagView.text = "?????? ??????"
                clickMarkerWatchTextView.isVisible = true

                //todo ????????? ????????? clickMarkerWatchTextView.text ??? ????????????
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
                .child(fileName).downloadUrl.addOnSuccessListener { uri -> //????????? ?????? ?????????
                    val intent = Intent(this@DiaryShowDialog, MarkerImageActivity::class.java)
                    intent.putExtra("imageUri",uri.toString())
                    startActivity(intent)


//                Glide.with(applicationContext)
//                    .load(uri)
//                    .into(uploadedImageView)
            }.addOnFailureListener { //????????? ?????? ?????????
            }
        }

        partnerImageViewInDiary.setOnClickListener {
            var partnerId: String? = null
            val curruser = userDB.child(user)
            curruser.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userModel = snapshot.getValue<UserModel>()

                    val currentGroup = groupDB.child(userModel?.groupID!!) // groupId??? ??????
                    currentGroup.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            groupModel = snapshot.getValue<GroupModel>()

                            Log.d("checkiing partner", "${groupModel?.secondUserID}")

                            if (user.compareTo(groupModel?.firstUserID!!) == 0) {     // ??? id??? ???????????? ????????? id ????????????
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
                                .child(fileNameForPartner!!).downloadUrl.addOnSuccessListener { uri -> //????????? ?????? ?????????
                                    val intent = Intent(this@DiaryShowDialog, MarkerImageActivity::class.java)
                                    intent.putExtra("imageUri",uri.toString())
                                    startActivity(intent)
                                }.addOnFailureListener { //????????? ?????? ?????????
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

            Toast.makeText(this, "????????? ????????? ??? ????????????!", Toast.LENGTH_SHORT).show()
        }

        myTextEditCompleteButtonInDiary.setOnClickListener {
            val text: String = myDiaryEditTextView.text.toString()
            myDiaryTextView.text = text
            myDiaryTextView.isVisible = true
            myDiaryEditTextView.isVisible = false

            myTextEditButtonInDiary.isVisible = true
            myTextEditCompleteButtonInDiary.isVisible = false

            //todo text ????????? ????????? ??? ?????? ????????? ??? ?????? ?????????
            //DB ????????? ??? ID -> ?????? ?????? -> marker ????????? ?????? ?????? ??????????????? ???
            //?????? ?????? ?????? ????????? ?????? ????????? ???????????? ??????
            // ?????? ?????? ??? ?????? ?????? text ????????? String ??? ??? ?????? ??????
            // DB ???????????? ????????? ????????????

            // ?????? ?????? ???????????? ?????? ?????????
            var day = intent.getStringExtra("day")
            var tmpLat = intent.getDoubleExtra("Lat", 0.0)
            var tmpLong = intent.getDoubleExtra("Long", 0.0)
            var docName = "$tmpLat:$tmpLong"


            // ????????????????????? ????????? ??? ????????? ??????
            // wrote??? ???????????? ????????? ??? ?????? ?????? ????????? ?????????.
            var wrote = 1
            val toWrite = hashMapOf(
                "$user" to text,
                "wrote" to wrote
            )
            // ????????????????????? ???????????? ??????
            val curruser = userDB.child(user)
            curruser.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userModel = snapshot.getValue<UserModel>()

                    //?????????: ??? ID, ????????????: ????????? ????????? ?????? ??????, ??????: ?????????
                    Log.d("Mylog", "?????? ID: ${userModel?.groupID}")
                    Log.d("Mylog", "??????: $day")
                    Log.d("Mylog", "????????????: $docName")
                    Log.d("Mylog", "??? ?????????: $text")
                    db.collection(userModel?.groupID.toString())
                        .document(day.toString())
                        .collection("marker")
                        .document(docName)
                        .set(toWrite, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("Mylog", "????????? ????????? ?????? ??????!")
                        }
                        .addOnFailureListener { e ->
                            Log.w(
                                "MyLog",
                                "????????? ????????? ?????? ??????!",
                                e
                            )
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Mylog", "????????? ?????? ??? ?????? ?????????!")
                }
            })

            Toast.makeText(this, "????????? ?????????????????????!", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show()
                }

            }
            else -> {
                Toast.makeText(this, "????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("????????? ???????????????")
            .setMessage("????????? ????????? ???????????? ?????? ????????? ???????????????")
            .setPositiveButton("????????????") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            }
            .setNegativeButton("????????????") { _, _ -> }
            .create()
            .show()
    }

}
