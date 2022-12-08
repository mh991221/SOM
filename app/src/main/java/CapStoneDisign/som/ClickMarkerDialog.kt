package CapStoneDisign.som

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.Button
import android.widget.EditText

class ClickMarkerDialog(context: Context) {

    private val dlg = Dialog(context)

    private lateinit var clickMarkerDialogEditText: EditText
    private lateinit var clickMarkerDialogCancelButton: Button
    private lateinit var clickMarkerDialogAdmitButton: Button


    fun start(lat:Double, long:Double){
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(R.layout.click_marker_dialog)
        dlg.setCancelable(false)

        clickMarkerDialogEditText = dlg.findViewById(R.id.clickMarkerDialogEditText)
        clickMarkerDialogCancelButton = dlg.findViewById(R.id.clickMarkerDialogCancelButton)
        clickMarkerDialogAdmitButton = dlg.findViewById(R.id.clickMarkerDialogAdmitButton)

        clickMarkerDialogAdmitButton.setOnClickListener {
            //todo clickMarkerDialogEditText.text 를 마커에 넣어주기
            // 일단 나는 이게 intent 가 아니라서 putExtra 가 안되니까 그냥 아예 호출할때 좌표를 입력해주게 했음
            // 좌표만 있으면 해당 마커에 접근하기는 쉬울거 같아서 이렇게 했는데
            // 더 쉬운 방법 있으면 바꿔도 됨
        }
        clickMarkerDialogCancelButton.setOnClickListener {
            //todo 이거는 메모를 안한건데 이러면 그냥 " "이거 넣어서 아무것도 안나오게 해주세요
            // 아니면 뭐 '입력한 메모가 없습니다' 이런거 넣어도 되고
        }
        dlg.show()
    }

}