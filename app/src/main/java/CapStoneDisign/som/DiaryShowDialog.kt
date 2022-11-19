package CapStoneDisign.som

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.*
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage

class DiaryShowDialog(context: Context):AppCompatActivity() {

    private val dlg = Dialog(context)

    private lateinit var myImageEditButtonInDiary: Button
    private lateinit var myTextEditButtonInDiary: Button
    private lateinit var myDiaryTextView: TextView
    private lateinit var partnerDiaryTextView: TextView

    fun start(){
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(R.layout.marker_dialog_layout)

        myDiaryTextView = dlg.findViewById(R.id.myDiaryTextView)
        partnerDiaryTextView = dlg.findViewById(R.id.partnerDiaryTextView)

        myImageEditButtonInDiary = dlg.findViewById(R.id.myImageEditButtonInDiary)
        myImageEditButtonInDiary.setOnClickListener {
            when{
                ContextCompat.checkSelfPermission(
                    dlg.context,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED ->{
                    navigatePhotos()
                }
                shouldShowRequestPermissionRationale(MainActivity(),android.Manifest.permission.READ_EXTERNAL_STORAGE) ->{
                    showPermissionContextPopup()
                }
                else ->{
                    requestPermissions(MainActivity(),arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
                }
            }

            //todo 사진 값을 지정된 마커 DB로 넘겨주기
        }
        myTextEditButtonInDiary = dlg.findViewById(R.id.myTextEditButtonInDiary)
        myTextEditButtonInDiary.setOnClickListener {
            //todo editText 창 띄우기 <- dialog 로 하면 엄청 쉬워보이긴 함
            //todo text 값을 지정된 마커 DB로 넘겨주기
        }
        dlg.show()
    }
    private fun showPermissionContextPopup(){
        AlertDialog.Builder(dlg.context)
            .setTitle("권한이 필요합니다")
            .setMessage("앱에서 사진을 불러오기 위해 권한이 필요합니다")
            .setPositiveButton("동의하기") {_, _->
                requestPermissions(MainActivity(),arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            }
            .setNegativeButton("취소하기") {_, _-> }
            .create()
            .show()
    }

    private fun navigatePhotos(){
        val intent  = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        dlg.context.startActivity(intent)
    }

}