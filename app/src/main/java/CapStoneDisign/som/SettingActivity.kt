package CapStoneDisign.som

import android.os.Bundle
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
    }

    fun getPhotoZoneIsChecked(): Boolean{
        return photoZoneSwitch.isChecked
    }

    fun getVisitedPlaceIsChecked(): Boolean{
        return visitedPlaceSwitch.isChecked
    }

    fun getPaymentPlaceIsChecked(): Boolean{
        return paymentPlaceSwitch.isChecked
    }

}