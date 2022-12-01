package CapStoneDisign.som

import android.R.id.toggle
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity


class SettingActivity:AppCompatActivity() {

    private val photoZoneSwitch: Switch by lazy{
        findViewById(R.id.photoZoneSwitch)
    }

    private val visitedPlaceSwitch: Switch by lazy{
        findViewById(R.id.visitedPlaceSwitch)
    }
    private val paymentPlaceSwitch: Switch by lazy{
        findViewById(R.id.paymentPlaceSwitch)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.option_layout)

        initSwitch()
    }

    override fun onRestart() {
        super.onRestart()
        initSwitch()
    }

    override fun onResume() {
        super.onResume()
        initSwitch()
    }


    override fun onPause() {
        savePhotoZoneSwitchState()
        saveVisitedPlaceSwitchState()
        savePaymentPlaceSwitchState()
        super.onPause()
    }

    override fun onStop() {
        savePhotoZoneSwitchState()
        saveVisitedPlaceSwitchState()
        savePaymentPlaceSwitchState()
        super.onStop()
    }
    override fun onDestroy() {
        savePhotoZoneSwitchState()
        saveVisitedPlaceSwitchState()
        savePaymentPlaceSwitchState()
        super.onDestroy()
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
        if(paymentPlaceSwitch.isChecked){
            val editor: SharedPreferences.Editor = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE).edit()
            editor.putBoolean("PaymentPlace",true)
            editor.apply()
        }else{
            val editor: SharedPreferences.Editor = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE).edit()
            editor.putBoolean("PaymentPlace",false)
            editor.apply()
        }
    }

    private fun initSwitch(){
        val sharedPref: SharedPreferences = getSharedPreferences("com.Switch.xyz", MODE_PRIVATE)
        photoZoneSwitch.isChecked = sharedPref.getBoolean("PhotoZone", true)
        visitedPlaceSwitch.isChecked = sharedPref.getBoolean("VisitedPlace",true)
        paymentPlaceSwitch.isChecked = sharedPref.getBoolean("PaymentPlace",true)
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