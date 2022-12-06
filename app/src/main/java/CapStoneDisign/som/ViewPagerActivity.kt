package CapStoneDisign.som

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage


class ViewPagerActivity : AppCompatActivity() {

    lateinit var storage: FirebaseStorage
    private val uriList = arrayListOf<Uri>()
    private val viewpager: ViewPager2 by lazy {
        findViewById(R.id.viewpager)
    }

    private val photoZoneViewPager:ImageView by lazy{
        findViewById(R.id.photoZoneViewPager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.photozone_viewpager)
        getPhotoList()

        Log.d("urichecking", "$uriList")

    }

    private fun getPhotoList() {
        val fileName = intent.getStringExtra("fileName")

        Log.d("fileNameChecking", "$fileName")

        val listRef = FirebaseStorage.getInstance().reference.child("image").child(fileName!!)
        var tmpUrl:Uri = Uri.parse(fileName)
        Log.d("firstTmpUri","$tmpUrl")

        listRef.listAll()
            .addOnSuccessListener { listResult ->
                for (item in listResult.items) {
                    // reference의 item(이미지) url 받아오기
                    item.downloadUrl.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            tmpUrl = task.result
                            Log.d("secondTmpUri","$tmpUrl")
                            Log.d("urichecking2","${task.result}")
                            uriList.add(tmpUrl)
//                            Glide.with(applicationContext)
//                                .load(tmpUrl)
//                                .fitCenter()
//                                .into(photoZoneViewPager)
                        } else {
                            // URL을 가져오지 못하면 토스트 메세지
                            Toast.makeText(this, "Uri를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                        }
                        Log.d("urichecking3", "$uriList")
                        viewpager.adapter = ViewPagerAdapter(this, uriList)
                        viewpager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                    }.addOnFailureListener {
                        // Uh-oh, an error occurred!
                    }
                    Log.d("urichecking2", "$uriList")
                }
                Log.d("urichecking1", "$uriList")

            }
        Log.d("thirdTmpUri","$tmpUrl")

    }

}