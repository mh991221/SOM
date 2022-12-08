package CapStoneDisign.som

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class MarkerImageActivity:AppCompatActivity() {


    private val uploadedImageView: ImageView by lazy{
        findViewById(R.id.uploadedImageView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_image_layout)

        imageLoad()
    }

    private fun imageLoad(){
        val extraString = intent.getStringExtra("imageUri")
        val uri:Uri = Uri.parse(extraString)
        Log.d("MyImageView","$uri")
        Glide.with(applicationContext)
                    .load(uri)
                    .into(uploadedImageView)
    }
}