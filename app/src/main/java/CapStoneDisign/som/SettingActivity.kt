package CapStoneDisign.som

import android.R.id.toggle
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class SettingActivity:AppCompatActivity() {

    private val photoZoneSwitch: Switch by lazy{
        findViewById(R.id.photoZoneSwitch)
    }

    private val visitedPlaceSwitch: Switch by lazy{
        findViewById(R.id.visitedPlaceSwitch)
    }
//    private val paymentPlaceSwitch: Switch by lazy{
//        findViewById(R.id.paymentPlaceSwitch)
//    }

    private val photoZoneEditText: EditText by lazy{
        findViewById(R.id.photoZoneEditText)
    }

    private val visitedPlaceEditText: EditText by lazy{
        findViewById(R.id.visitedPlaceEditText)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.option_layout)

        initSwitch()
    }


    override fun onPause() {
        savePhotoZoneSwitchState()
        saveVisitedPlaceSwitchState()
        savePaymentPlaceSwitchState()
        savePhotoZoneShot()
        saveVisitedPlaceMinute()
        super.onPause()
    }

    override fun onStop() {
        savePhotoZoneSwitchState()
        saveVisitedPlaceSwitchState()
        savePaymentPlaceSwitchState()
        savePhotoZoneShot()
        saveVisitedPlaceMinute()
        super.onStop()
    }
    override fun onDestroy() {
        savePhotoZoneSwitchState()
        saveVisitedPlaceSwitchState()
        savePaymentPlaceSwitchState()
        savePhotoZoneShot()
        saveVisitedPlaceMinute()
        super.onDestroy()
    }


    private fun savePhotoZoneShot(){
        if(photoZoneEditText.text.isEmpty()){
            val editor: SharedPreferences.Editor = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE).edit()
            editor.putInt("PhotoZoneShot",5)
            editor.apply()
        }else{
            val editor: SharedPreferences.Editor = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE).edit()
            val inputInt :Int = Integer.parseInt(photoZoneEditText.text.toString())
            editor.putInt("PhotoZoneShot",inputInt)
            editor.apply()
            Toast.makeText(this,"$inputInt 장으로 설정 되었습니다.",Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveVisitedPlaceMinute(){
        if(visitedPlaceEditText.text.isEmpty()){
            val editor: SharedPreferences.Editor = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE).edit()
            editor.putInt("placeMinute",5)
            editor.apply()
        }else{
            val editor: SharedPreferences.Editor = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE).edit()
            val inputInt :Int = Integer.parseInt(visitedPlaceEditText.text.toString())
            editor.putInt("placeMinute",inputInt)
            editor.apply()
            Toast.makeText(this,"$inputInt 분으로 설정 되었습니다.",Toast.LENGTH_SHORT).show()
        }
    }


    @SuppressLint("CommitPrefEdits")
    private fun savePhotoZoneSwitchState(){
        if(photoZoneSwitch.isChecked){
            val editor: SharedPreferences.Editor = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE).edit()
            editor.putBoolean("PhotoZone",true)
            editor.apply()
        }else{
            val editor: SharedPreferences.Editor = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE).edit()
            editor.putBoolean("PhotoZone",false)
            editor.apply()
        }
    }
    private fun saveVisitedPlaceSwitchState(){
        if(visitedPlaceSwitch.isChecked){
            val editor: SharedPreferences.Editor = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE).edit()
            editor.putBoolean("VisitedPlace",true)
            editor.apply()
        }else{
            val editor: SharedPreferences.Editor = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE).edit()
            editor.putBoolean("VisitedPlace",false)
            editor.apply()
        }
    }
    private fun savePaymentPlaceSwitchState(){
//        if(paymentPlaceSwitch.isChecked){
//            val editor: SharedPreferences.Editor = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE).edit()
//            editor.putBoolean("PaymentPlace",true)
//            editor.apply()
//        }else{
//            val editor: SharedPreferences.Editor = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE).edit()
//            editor.putBoolean("PaymentPlace",false)
//            editor.apply()
//        }
    }

    private fun initSwitch(){
        val sharedPref: SharedPreferences = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE)
        photoZoneSwitch.isChecked = sharedPref.getBoolean("PhotoZone", true)
        visitedPlaceSwitch.isChecked = sharedPref.getBoolean("VisitedPlace",true)
//        paymentPlaceSwitch.isChecked = sharedPref.getBoolean("PaymentPlace",true)

    }

    fun getPhotoZoneIsChecked(): Boolean{
        val sharedPref: SharedPreferences = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE)
        return sharedPref.getBoolean("PhotoZone",true)
    }

    fun getVisitedPlaceIsChecked(): Boolean{
        val sharedPref: SharedPreferences = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE)
        return sharedPref.getBoolean("VisitedPlace",true)
    }

    fun getPaymentPlaceIsChecked(): Boolean {
        val sharedPref: SharedPreferences = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE)
        return sharedPref.getBoolean("PaymentPlace", true)
    }

}