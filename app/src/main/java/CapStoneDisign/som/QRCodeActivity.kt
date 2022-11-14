package CapStoneDisign.som

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONException
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class QrCodeActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private val scanButton: Button by lazy{
        findViewById(R.id.scanButton)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_code_layout)

        scanButton.setOnClickListener {
            cameraTask()
        }
    }

    private fun hasCameraAccess(): Boolean {
        return EasyPermissions.hasPermissions(this, android.Manifest.permission.CAMERA)
    }

    private fun cameraTask() {
        if (hasCameraAccess()) {
            var qrScanner = IntentIntegrator(this)
            qrScanner.setPrompt("QR코드를 인증해주세요.") // 원하는 문구 기입
            qrScanner.setCameraId(0)
            qrScanner.setOrientationLocked(false) // 세로,가로 모드를 고정 시켜주는 역할
            qrScanner.setBeepEnabled(false) // QR코드 스캔시 소리 나게 하려면 true 아니면 false로 지정
            qrScanner.initiateScan() //QR코드 스캔의 결과 값은 onActivityResult 함수로 전달
        } else {
            EasyPermissions.requestPermissions(
                this,
                "QR 코드 기능을 사용하기 위해서는 카메라 권한 설정을 허용해 주셔야 합니다.",
                123,
                android.Manifest.permission.CAMERA
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "결과를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    Log.d("QRCODEREQUESTED","$result")
                    val intent = Intent()
                    intent.putExtra("groupID",result.contents)
                    setResult(Activity.RESULT_OK,intent)
                    finish()
//                    cardView1!!.startAnimation(reveal)
//                    cardView2!!.startAnimation(hide)
                } catch (exception: JSONException) {
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }    }

    override fun onRationaleAccepted(requestCode: Int) {}
    override fun onRationaleDenied(requestCode: Int) {}


}
