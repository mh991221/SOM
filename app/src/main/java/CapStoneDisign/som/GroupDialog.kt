package CapStoneDisign.som

import CapStoneDisign.som.DBKey.Companion.DB_GROUPS
import CapStoneDisign.som.DBKey.Companion.DB_USERS
import CapStoneDisign.som.Model.GroupModel
import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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

class GroupDialog(context: Context) {

    private val dlg = Dialog(context)
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private lateinit var groupDatabase: DatabaseReference

    private lateinit var joinGroupButton: Button
    private lateinit var createGroupButton: Button
    private lateinit var alertDialogGroupCode: EditText
    private lateinit var joinButton: Button
    var updateMap = HashMap<String, Any>()

    private val groupDB: DatabaseReference by lazy{
        Firebase.database.reference.child(DB_GROUPS)
    }

    private val userDB: DatabaseReference by lazy{
        Firebase.database.reference.child(DB_USERS)
    }

    var updateUserMap = HashMap<String,Any>()

    fun start(){
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(R.layout.alert_dialog_layout)
        dlg.setCancelable(false)

        joinGroupButton = dlg.findViewById(R.id.joinGroupButton)
        joinGroupButton.setOnClickListener {
            joinGroup()
        }
        createGroupButton = dlg.findViewById(R.id.createGroupButton)
        createGroupButton.setOnClickListener {
            createGroup()
            dlg.dismiss()
        }
        dlg.show()
    }

    private fun joinGroup(){
        alertDialogGroupCode = dlg.findViewById(R.id.alertDialogGroupCode)
        joinButton = dlg.findViewById(R.id.joinButton)
        groupDatabase = Firebase.database.reference.child(DB_GROUPS)

        alertDialogGroupCode.isVisible = true
        Toast.makeText(dlg.context,"그룹 코드를 입력해주세요",Toast.LENGTH_LONG).show()

        joinButton.isVisible = true

        joinButton.setOnClickListener {
            val groupCode = alertDialogGroupCode.text.toString()

            val group = groupDB.child(groupCode)

            val user = userDB.child(getCurrentUserID())
            group.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groupModel = snapshot.getValue<GroupModel>()
                    if(groupModel?.secondUserID.orEmpty() == "null"){
                        updateMap.put("firstUserID",groupModel?.firstUserID.orEmpty())
                        updateMap.put("secondUserID",auth.currentUser?.uid.orEmpty())
                        groupDB.child(groupCode).updateChildren(updateMap)
                            .addOnSuccessListener {
                                Toast.makeText(dlg.context, "그룹에 참가했습니다!", Toast.LENGTH_LONG).show()
                                updateUserMap.put("groupID",groupCode)
                                userDB.child(getCurrentUserID()).updateChildren(updateUserMap)
                            }
                        dlg.dismiss()
                    }else if(groupModel?.secondUserID.orEmpty() != "null"){
                        Toast.makeText(dlg.context,"이미 두명이 있는 그룹입니다!", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(dlg.context,"그룹 코드를 확인해주세요",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

            Toast.makeText(dlg.context,"그룹 코드를 확인해주세요", Toast.LENGTH_LONG).show()

        }


    }

    private fun createGroup(){
        val uid = getCurrentUserID()
        val model = GroupModel(uid,"null")
        groupDB.child(auth.currentUser?.uid.orEmpty()).setValue(model)
        Toast.makeText(dlg.context,"그룹이 생성되었습니다. 다시 로그인해주세요",Toast.LENGTH_LONG).show()
        updateUserMap.put("groupID",uid)
        userDB.child(getCurrentUserID()).updateChildren(updateUserMap)
    }

    private fun getCurrentUserID(): String{
        return auth.currentUser?.uid.orEmpty()
    }

}