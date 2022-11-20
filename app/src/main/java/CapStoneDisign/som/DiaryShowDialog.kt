package CapStoneDisign.som

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
import com.google.firebase.storage.FirebaseStorage

class DiaryShowDialog:AppCompatActivity() {

    private lateinit var myImageEditButtonInDiary: Button
    private lateinit var myTextEditButtonInDiary: Button
    private lateinit var myDiaryTextView: TextView
    private lateinit var partnerDiaryTextView: TextView
    private lateinit var myImageViewInDiary: ImageView
    private lateinit var partnerImageViewInDiary: ImageView
    private lateinit var myDiaryEditTextView: EditText
    private lateinit var myTextEditCompleteButtonInDiary:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.marker_dialog_layout)


        initLayout()
        initButtonListener()


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