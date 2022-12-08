package CapStoneDisign.som

import CapStoneDisign.som.DBKey.Companion.DB_GROUPS
import CapStoneDisign.som.DBKey.Companion.DB_USERS
import CapStoneDisign.som.Model.GroupModel
import CapStoneDisign.som.Model.UserModel
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder

class DateQRDialog(context: Context) {

    private val dlg = Dialog(context)
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private lateinit var groupDatabase: DatabaseReference

    private lateinit var listener : MyDialogOKClickedListener
    private lateinit var createQRButton: Button
    private lateinit var scanQRButton: Button
    private lateinit var QRImageView: ImageView
    private val user = auth.currentUser!!.uid
    var userModel: UserModel? = null


    private val userDB: DatabaseReference by lazy{
        Firebase.database.reference.child(DB_USERS)
    }

    fun start(){
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(R.layout.date_qr_layout)
        dlg.setCancelable(true)



        QRImageView = dlg.findViewById(R.id.QRImageView)

        createQRButton = dlg.findViewById(R.id.createQRButton)
        createQRButton.setOnClickListener {
            var groupID: String?=null
            val currUser = userDB.child(user)
            currUser.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userModel = snapshot.getValue<UserModel>()
                    groupID = userModel?.groupID
                    
                    QRImageView.setImageBitmap(createQR(groupID))
                    Log.d("QRGroupID","$groupID")
                    QRImageView.isVisible = true
                }

                override fun onCancelled(error: DatabaseError) {}
            })


        }
        scanQRButton = dlg.findViewById(R.id.scanQRButton)
        scanQRButton.setOnClickListener {
            listener.onOKClicked("intent")
            dlg.dismiss()
        }
        dlg.show()

    }
    fun setOnOKClickedListener(listener: (String) -> Unit) {
        this.listener = object: MyDialogOKClickedListener {
            override fun onOKClicked(content: String) {
                listener(content)
            }
        }
    }


    interface MyDialogOKClickedListener {
        fun onOKClicked(content : String)
    }

   private fun createQR(contents: String?): Bitmap{
       val barcodeEncoder = BarcodeEncoder()
       return barcodeEncoder.encodeBitmap(contents, BarcodeFormat.QR_CODE, 512, 512)
   }

}