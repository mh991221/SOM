package CapStoneDisign.som


// import android.os.Build.VERSION_CODES.R
import CapStoneDisign.som.Model.UserModel
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.MultipartPathOverlay
import com.naver.maps.map.overlay.MultipartPathOverlay.ColorPart
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.LocationButtonView
import java.time.LocalDate
import kotlin.math.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener {

    //Firestore 레퍼런스. 이거 써서 Firestore에 값 올리고 내린다.
    val db = Firebase.firestore

    //현재 데이트 중인 경로 좌표들의 리스트
    var routes = mutableListOf<MutableList<LatLng>>()

    // DB에 넣어서 쓰려는 좌표값들 담아둘 더블리스트.
    var routesd = mutableListOf<Double>()

    //DB에서 받아온 경로의 좌표들 담아둘 리스트
    var memory = mutableListOf<MutableList<LatLng>>()
    // 경로 그릴때 필요한 색
    var memcolor = mutableListOf<ColorPart>()

    // DB에서 불러온 경로를 담아둘 버퍼. 여기 담다가 0, 0 나오면 끊고 path에 넣는 식으로 써먹을 예정.
    var memBuf = mutableListOf<LatLng>()

    //현재 기록중인지 체크용
    var checkWritingOrNot = 0

    //현재 사용자 정보 받아오기 용
    var userModel: UserModel? = null

    // 경로 그리기 위해 필요한 애. 얘가 담고 있는 정보로 경로를 그리게 된다.
    var path = MultipartPathOverlay()

    // 마커의 좌표를 담아둘 더블 리스트
    var markerPoints = mutableListOf<Double>()
    // 마커를 담아둘 리스트
    var markers = mutableListOf<Marker>()

    var context: Context? = null

    private val editMarkerButton: Button by lazy{
        findViewById(R.id.editMarkerButton)
    }

    private val watchMarkerButton: Button by lazy{
        findViewById(R.id.watchMarkerButton)
    }

    private val photoZoneToggleButton: ToggleButton by lazy{
        findViewById(R.id.photoZoneToggleButton)
    }

    private val placeToggleButton: ToggleButton by lazy{
        findViewById(R.id.placeToggleButton)
    }

    private val paymentToggleButton: ToggleButton by lazy{
        findViewById(R.id.paymentToggleButton)
    }

    private val userDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DBKey.DB_USERS)
    }

    private val mapView: MapView by lazy {
        findViewById(R.id.mapView)
    }

    private val mainDrawerLayout: DrawerLayout by lazy {
        findViewById(R.id.mainDrawerLayout)
    }

    private val toolbarLayout: androidx.appcompat.widget.Toolbar by lazy {
        findViewById(R.id.main_layout_toolbar)
    }

    private val navigationView: NavigationView by lazy {
        findViewById(R.id.navigationView)
    }

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private val currentLocationButton: LocationButtonView by lazy {
        findViewById(R.id.currentLocationButton)
    }

    /*
    private val dateButton: Button by lazy {
        findViewById(R.id.dateButton)
    }
     */

    // 캘린더뷰 (날짜 선택해서 날짜값 받아오기 용
    private val calendar: CalendarView by lazy {
        findViewById(R.id.calendarView)
    }

    private lateinit var naverMap: NaverMap

    private var isTracking: Int = 0
    private var groupID: String?= null
    private lateinit var day: String

    private var isEditMode: Boolean = false

    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_drawer_layout)
        context = this

        if (auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(this)

        calendar.setOnDateChangeListener { calender, year, month, dayOfMonth ->
            // 아니 ㅅㅂ 왜 반환하는 달 값은 +1을 해줘야 하는 건대 ㅅㅂ ㅋㅋㅋㅋㅋㅋㅋㅋㅋ

            // 저장할 땐 01로 저장하는데, 달력 클릭하면 day가 0이 아니라 1임.
            // 그거 먼저 처리 좀 해줄게
            var newday = ""
            if (dayOfMonth / 10 < 1) {
                newday = "0$dayOfMonth"
            }
            else {
                newday = "$dayOfMonth"
            }

            day= "${year}-${month+1}-${newday}"
            Log.d("mylog", day)
            getMemory(day)
        }


        initToolBar()
        initClickListener()

        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun initClickListener(){
        editMarkerButton.setOnClickListener {
            //todo 편집모드

            isEditMode = true
            editMarkerButton.isVisible = false
            watchMarkerButton.isVisible = true
        }

        watchMarkerButton.setOnClickListener {
            //todo 보기모드

            isEditMode = false
            editMarkerButton.isVisible = true
            watchMarkerButton.isVisible = false
        }

        photoZoneToggleButton.setOnClickListener {
            if(photoZoneToggleButton.isChecked) {
                //todo 포토존 마커 보이기
            }else{
                //todo 포토존 마커 없애기
            }
        }

        placeToggleButton.setOnClickListener {
            if(placeToggleButton.isChecked){
                //todo 장소 마커 보이기
            }else{
                //todo 장소 마커 없애기
            }
        }

        paymentToggleButton.setOnClickListener {
            if(paymentToggleButton.isChecked){
                //todo 결제 마커 보이기
            }else{
                //todo 결제 마커 없애기
            }
        }
    }

    private fun initToolBar() {
        setSupportActionBar(toolbarLayout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val currUser = userDB.child(auth.currentUser!!.uid)
        currUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModel = snapshot.getValue<UserModel>()
                groupID = userModel?.groupID

                Log.d("checking", "${groupID}")
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                486486 -> {
                    Log.d("QRCODEREQUESTED1", "$groupID")

                    val contents = data!!.getStringExtra("groupID")
                    Log.d("QRCODEREQUESTED1", "$contents")
                    if(data!!.getStringExtra("groupID").toString().compareTo(groupID!!) == 0){
                        isTracking = 1
                        Log.d("QRCODEREQUESTED1", "$isTracking")
                    }
                }
            }
        }
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0

        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false

        currentLocationButton.map = naverMap

        locationSource = FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        setUpdateLocationListener()
    }

    // 일단 일관성을 위해 카메라 부분에 달려있는 거랑 동일하게 구현하겠음
    private var standardLatitude = 0.0
    private var standardLongitude = 0.0
    var count = 0
    // 현재 위치는 뭐 따로 만들어줄 필요는 없을 거 같은데, 이게 보기 편하니까 쓰도록 하겠음
    private var presentLatitude = 0.0
    private var presentLongitude = 0.0
    //
    private val numberToCalculateDistance = 6372.8 * 1000

    @SuppressLint("MissingPermission")
    fun setUpdateLocationListener() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY //높은 정확도
            interval = 1000 //1초에 한번씩 GPS 요청
        }

        // 이 부분에서 사용자의 현재 위치가 갱신되니, 여기에 머문 자리 마커 생성 기능을 추가하겠다.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for ((i, location) in locationResult.locations.withIndex()) {
                    Log.d("location1: ", "${location.latitude}, ${location.longitude}")
                    if (checkWritingOrNot == 1) {

                        // 사용자의 현재 위치를 경로에 추가
                        routes[routes.lastIndex].add(LatLng(location.latitude, location.longitude))

                        // 경로 길이가 3 이상이어야 지도에 경로를 그릴 수 있으니, 경로 길이가 3 이상인지 체크
                        // 3 이상이면 경로 그려줌
                        if(routes[routes.lastIndex].size>=3){
                            path.coordParts = routes
                            path.map = naverMap
                        }
                        routesd.add(location.latitude)
                        routesd.add(location.longitude)

                        // 머문자리 마커 생성 기능
                        if (count == 0) {
                            standardLatitude = location.latitude
                            standardLongitude = location.longitude
                            count++
                        }
                        else {
                            presentLatitude = location.latitude
                            presentLongitude = location.longitude

                            val dLat = Math.toRadians(presentLatitude - standardLatitude)
                            val dLon = Math.toRadians(presentLongitude - standardLongitude)

                            val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(Math.toRadians(presentLatitude)) * cos(Math.toRadians(standardLatitude))
                            val c = 2 * asin(sqrt(a))
                            val distance =  (numberToCalculateDistance * c).toInt()

                            if(distance <= 100){
                                count++
                            }else{
                                count = 1
                                standardLatitude = presentLatitude
                                standardLongitude = presentLongitude
                            }
                        }

                        // 현재는 1초에 한 번씩 gps 받아오니까,
                        // 일단 실험용으로 10초 동안 머물면 마커 생성되게 해봤음.
                        // count가 1 올라갈 때마다 1초 지나는 거
                        if (count > 10) {
                            // 생성될 마커의 위치값 받아온다.
                            var tmp = routes[routes.lastIndex][routes[routes.lastIndex].lastIndex]
                            var tmpLat = tmp.latitude
                            var tmpLong = tmp.longitude

                            // 위치 값 토대로, 방문으로 만들어진 마커에 대한 정보를 꾸린다.
                            var tag = "visited"
                            val marker = hashMapOf(
                                "tag" to tag,
                                "Latitude" to tmpLat,
                                "Longitude" to tmpLong,
                            )
                            // 다큐먼트를 특정하기 위해, 다큐먼트의 이름을 좌표값을 이용해 만들어낸다.
                            var docName = "$tmpLat:$tmpLong"

                            // 현재 사용자가 누구인지 확인
                            val curruser = userDB.child(auth.currentUser!!.uid)
                            // 현재 사용자의 정보 기반으로 DB에 값 저장
                            curruser.addValueEventListener(object : ValueEventListener {
                                // ?? 이건 왜 필요하다냐?
                                @RequiresApi(Build.VERSION_CODES.O)
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    userModel = snapshot.getValue<UserModel>()

                                    //컬렉션: 그룹ID, 다큐먼트: 날짜와 시간, 내용: 경로들
                                    db.collection(userModel?.groupID.toString())
                                        .document(LocalDate.now().toString())
                                        .collection("marker")
                                        .document(docName)
                                        .set(marker, SetOptions.merge())
                                        .addOnSuccessListener {
                                            Log.d("Mylog", "방문 마커 저장 완료!")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(
                                                "MyLog",
                                                "방문 마커 저장 실패함!",
                                                e
                                            )
                                        }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })

                            // 카운트 값 다시 초기화
                            count = 0
                        }
                        Log.d("Mylog", "현재 count 값: $count")
                    }
                    setLastLocation(location)
//                    val intent = Intent(this@MainActivity, BackgroundLocationUpdateService::class.java)
//                    startService(intent)
                }
            }
        }
        //location 요청 함수 호출 (locationRequest, locationCallback)

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }//좌표계를 주기적으로 갱신

    fun setLastLocation(location: Location) {
        val myLocation = LatLng(location.latitude, location.longitude)


        naverMap.maxZoom = 18.0
        naverMap.minZoom = 5.0

        //marker.map = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.accountInfoIcon -> {
                val intent = Intent(this, AccountInfoActivity::class.java)
                startActivity(intent)
            }
            R.id.settingIcon -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }
            R.id.startIcon -> {
                // 기록을 시작하기에 앞서 먼저 오늘 날짜의 기록을 DB에서 받아온 뒤에 거기에 기록을 덧붙여 나간다.
                // 이렇게 하면 앱을 껐다가 다시 켜서 실행해도 이전 기록이 사라지는 일이 없다!!
                db.collection(userModel?.groupID.toString()).document(LocalDate.now().toString())
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.get("route") != null) {
                            routesd = document.get("route") as MutableList<Double>
                            Log.d(
                                "MyTAG",
                                "DocumentSnapshot data: ${document.id} ${document.get("route")}"
                            )
                            if (routesd.size >= 8) {
                                routes.clear()
                                memBuf.clear()
                                memcolor.clear()
                                for (i: Int in 0 until routesd.size step(2)) {
                                    // 일단 더블형이니까, != 0.0 쓰면 또 부동 소수점 특유의 빡치는 크기비교 나올 거 같아서
                                    // 적당히 우리 나라에 있을 수 없는 좌표인 0.3 정도로 끊어둠. 이정도면 문제 생길 일 없지 않을까?
                                    // 요는, 0.0을 토큰으로 쓰기 위해 이렇게 했다는 것이다.
                                    if (routesd[i] >= 0.3 || routesd[i] <= -0.3) {
                                        // memory.add(LatLng(coord[i], coord[i + 1]))
                                        // 1. 0,0이 아닐 경우, 즉, 그려야 하는 좌표값일 경우
                                        // memory에 아무것도 없으면 리스트 하나 만들어서 넣고,
                                        // 뭐라도 하나 있으면 제일 뒤의 리스트에 값 넣는다.
                                        memBuf.add(LatLng(routesd[i], routesd[i + 1]))
                                    }
                                    else {
                                        // 0,0이 나왔을 경우, 즉, 리스트를 끊어줘야 할 경우
                                        if (memBuf.size >= 3) {
                                            routes.add(mutableListOf<LatLng>())
                                            routes[routes.lastIndex].addAll(memBuf)
                                            memBuf.clear()
                                            memcolor.add(MultipartPathOverlay.ColorPart(Color.RED, Color.WHITE, Color.GRAY, Color.LTGRAY))
                                        }
                                    }
                                }
                                Log.d(
                                    "MyTAG",
                                    "memory data: $routes"
                                )
                                path.coordParts = routes
                                path.colorParts = memcolor
                                Log.d(
                                    "MyTAG",
                                    "찾아보자 오류!"
                                )
                                if (path.coordParts.size > 0) {
                                    path.map = naverMap
                                }
                                Log.d(
                                    "MyTAG",
                                    "찾아보자 오류!!"
                                )
                            }
                        }
                        else {
                            Log.d("MyTAG", "No such document")
                        }

                        // 마커 구조를 다 뜯어 고칠 예정. 이제 기록 중에는 마커 안 띄우고 불러올 때만 마커 띄우게 할 것임.
                        // 현재 그려져있는 마커는 다 지운다 일단.
                        for (i: Int in 0..markers.lastIndex) {
                            markers[i].map = null
                        }
                        markers.clear()
                        /*
                        Log.d("MyTAG", "lastIndex ${markers.lastIndex}")
                        for (i: Int in 0..markers.lastIndex) {
                            markers[i].map = null
                        }
                        markers.clear()
                        if (document.get("marker") != null) {

                            markerPoints = document.get("marker") as MutableList<Double>
                            markers.clear()
                            for (i: Int in 0 until markerPoints.size step(2)) {
                                markers.add(Marker())
                                Log.d("logForMarker","checked")
                                markers[markers.lastIndex].position = LatLng(markerPoints[i], markerPoints[i+1])
                                markers[markers.lastIndex].map = naverMap
                                markers[markers.lastIndex].setOnClickListener{
                                    if(it is Marker){
                                        Toast.makeText(this,"마커가 선택되었습니다", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, DiaryShowDialog::class.java)
                                        // todo 여기 putExtra value 에 날짜 + 마커번호로 된 값을 넘겨주세요
                                        //마커 번호가 어떤건지 몰라서 여기에 남겨요
                                        // 날짜는 내가 day 를 전역변수로 선언해서 넣어놨으니까 뒤에 마커번호만 붙여주세요
                                        // 같은 listener 가 밑에 2개 더 있는데 함수로 따로 빼지 않은 이유는 마커 번호 받아오기가 어려워서임
                                        // storage 에 사진 저장하는 기능은 다 구현했음
                                        // text 만 DB에 넣으면 됨 text 도 이 이름으로 저장하고 싶은데 가능?
                                        // text DB에 넣는거는 DiaryShowDialog 에 todo 로 달아 놓을게

                                        Log.d("mylog","기록 시작에서 실행되는 코드입니당")
                                        Log.d("mylog","마커의 좌표 ${it.position}")
                                        Log.d("mylog","몇 번째로 찍힌 마커인가? ${markerPoints.indexOf(it.position.latitude) / 2}")
                                        Log.d("mylog","넘길 값 한 눈에 보기: "+day+"+${markerPoints.indexOf(it.position.latitude) / 2}")
                                        intent.putExtra("marker",day+"+${markerPoints.indexOf(it.position.latitude) / 2}")
                                        Log.d("checkPutExtra",day+"+${markerPoints.indexOf(it.position.latitude) / 2}")

                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        startActivity(intent)

                                    }
                                    true
                                }
                            }
                        }
                        */
                    }
                    .addOnFailureListener { exception ->
                        Log.d("MyTAG", "get failed with ", exception)
                    }

                // 새로이 적어나갈 경로를 위해 미리 만들어두는 것.
                routes.add(mutableListOf<LatLng>())
                memcolor.add(MultipartPathOverlay.ColorPart(Color.RED, Color.WHITE, Color.GRAY, Color.LTGRAY))
                path.colorParts = memcolor
                checkWritingOrNot = 1
                Log.d("MyTAG", "기록 시작")
            }
            R.id.endIcon -> {
                checkWritingOrNot = 0
                // end 누르면 count 값 다시 0으로 만들어줘야지
                count = 0
                // 이제 루츠에 굳이 0,0 넣을 필요는 없을 듯?
                // routes[routes.lastIndex].add(LatLng(0.0, 0.0))
                routesd.add(0.0)
                routesd.add(0.0)


                val course = hashMapOf(
                    "route" to routesd,
                    "marker" to markerPoints
                )

                val curruser = userDB.child(auth.currentUser!!.uid)
                curruser.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userModel = snapshot.getValue<UserModel>()

                        //컬렉션: 그룹ID, 다큐먼트: 날짜와 시간, 내용: 경로들
                        db.collection(userModel?.groupID.toString())
                            .document(LocalDate.now().toString())
                            .set(course, SetOptions.merge())
                            .addOnSuccessListener {
                                Log.d("Mylog", "DocumentSnapshot successfully written!")
                            }
                            .addOnFailureListener { e ->
                                Log.w(
                                    "MyLog",
                                    "Error writing document",
                                    e
                                )
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            R.id.cameraIcon ->{
                val intent = Intent(this, CameraActivity::class.java)
                // 날짜 정보, 그룹 아이디를 일단 여기서 넘기게 해봤음.
                // 더 나은 방법이 있다 싶으면 그렇게 바꿔도 되고.
                var groupID = "이대로 나오면 뭔가 잘못된 것"

                val curruser = userDB.child(auth.currentUser!!.uid)
                curruser.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userModel = snapshot.getValue<UserModel>()
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
                groupID = userModel?.groupID.toString()
                Log.d("mylog","날짜: ${LocalDate.now()}")
                Log.d("mylog","그룹아이디 $groupID")
                intent.putExtra("day", LocalDate.now().toString())
                intent.putExtra("groupID", groupID)

                startActivity(intent)
            }
            R.id.QRIcon ->{
                val dlg = DateQRDialog(this)
                dlg.start()
                dlg.setOnOKClickedListener { content ->
                    if(content.compareTo("intent") == 0){
                        if(content.compareTo("intent") == 0){
                            val intent = Intent(this, QrCodeActivity::class.java)
                            startActivityForResult(intent,486486)
                        }
                    }

                }
            }
            R.id.markerIcon -> {
                // 마커 구조를 다 뜯어 고칠 예정. 이제 마커를 생성하면, 해당 날짜의 다큐먼트 안의 내부 컬렉션에
                // 그 마커와 관련된 정보를 담고 있는 해시맵을 생성할 것이다.

                /*
                var tmp = routes[routes.lastIndex][routes[routes.lastIndex].lastIndex]
                markerPoints.add(tmp.latitude)
                markerPoints.add(tmp.longitude)
                markers.add(Marker())
                markers[markers.lastIndex].position = LatLng(tmp.latitude, tmp.longitude)
                markers[markers.lastIndex].map = naverMap
                markers[markers.lastIndex].setOnClickListener{
                    if(it is com.naver.maps.map.overlay.Marker){
                        android.widget.Toast.makeText(this,"마커가 선택되었습니다", android.widget.Toast.LENGTH_SHORT).show()
                        val intent = android.content.Intent(
                            this,
                            CapStoneDisign.som.DiaryShowDialog::class.java
                        )
                        // todo 여기 putExtra value 에 날짜 + 마커번호로 된 값을 넘겨주세요
                        Log.d("mylog","마커 생성에서 실행되는 코드입니당")
                        Log.d("mylog","마커의 좌표 ${it.position}")
                        Log.d("mylog","몇 번째로 찍힌 마커인가? ${markerPoints.indexOf(it.position.latitude) / 2}")
                        Log.d("mylog","넘길 값 한 눈에 보기: "+day+"+${markerPoints.indexOf(it.position.latitude) / 2}")
                        intent.putExtra("marker", day+"+${markerPoints.indexOf(it.position.latitude) / 2}")

                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)

                    }
                    true
                }
                */

                // 생성될 마커의 위치값 받아온다.
                var tmp = routes[routes.lastIndex][routes[routes.lastIndex].lastIndex]
                var tmpLat = tmp.latitude
                var tmpLong = tmp.longitude

                // 위치 값 토대로, 클릭으로 만들어진 마커에 대한 정보를 꾸린다.
                var tag = "clicked"
                val marker = hashMapOf(
                    "tag" to tag,
                    "Latitude" to tmpLat,
                    "Longitude" to tmpLong
                )
                // 다큐먼트를 특정하기 위해, 다큐먼트의 이름을 좌표값을 이용해 만들어낸다.
                var docName = "$tmpLat:$tmpLong"

                // 현재 사용자가 누구인지 확인
                val curruser = userDB.child(auth.currentUser!!.uid)
                // 현재 사용자의 정보 기반으로 DB에 값 저장
                curruser.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userModel = snapshot.getValue<UserModel>()

                        //컬렉션: 그룹ID, 다큐먼트: 날짜와 시간, 내용: 경로들
                        db.collection(userModel?.groupID.toString())
                            .document(LocalDate.now().toString())
                            .collection("marker")
                            .document(docName)
                            .set(marker, SetOptions.merge())
                            .addOnSuccessListener {
                                Log.d("Mylog", "클릭 마커 저장 완료!")
                            }
                            .addOnFailureListener { e ->
                                Log.w(
                                    "MyLog",
                                    "클릭 마커 저장 실패함!",
                                    e
                                )
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })

            }
            R.id.QRIcon ->{
                val dlg = DateQRDialog(this)
                dlg.start()
                dlg.setOnOKClickedListener { content ->
                    if(content.compareTo("intent") == 0){
                        if(content.compareTo("intent") == 0){
                            val intent = Intent(this, QrCodeActivity::class.java)
                            startActivityForResult(intent,486486)
                        }
                    }
                }
            }
        }
        return false
    }

    override fun onBackPressed() {
        if (mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mainDrawerLayout.closeDrawers()
            Toast.makeText(this, "drawer is closed", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mainDrawerLayout.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }


    private fun getCurrentUserID(): String {
        return auth.currentUser?.uid.orEmpty()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    // date를 다큐먼트의 키 값으로 갖는 기록 불러옵니다.
    private fun getMemory(date: String) {
        db.collection(userModel?.groupID.toString()).document(date)
            .get()
            .addOnSuccessListener { document ->
                if (document.get("route") != null) {
                    var coord = document.get("route") as MutableList<Double>
                    Log.d(
                        "MyTAG",
                        "DocumentSnapshot data: ${document.id} ${document.get("route")}"
                    )
                    // 길 그리기
                    if (coord.size >= 8) {
                        memory.clear()
                        memBuf.clear()
                        memcolor.clear()
                        for (i: Int in 0 until coord.size step(2)) {
                            // 일단 더블형이니까, != 0.0 쓰면 또 부동 소수점 특유의 빡치는 크기비교 나올 거 같아서
                            // 적당히 우리 나라에 있을 수 없는 좌표인 0.3 정도로 끊어둠. 이정도면 문제 생길 일 없지 않을까?
                            // 요는, 0.0을 토큰으로 쓰기 위해 이렇게 했다는 것이다.
                            if (coord[i] >= 0.3 || coord[i] <= -0.3) {
                                // memory.add(LatLng(coord[i], coord[i + 1]))
                                // 1. 0,0이 아닐 경우, 즉, 그려야 하는 좌표값일 경우
                                // memory에 아무것도 없으면 리스트 하나 만들어서 넣고,
                                // 뭐라도 하나 있으면 제일 뒤의 리스트에 값 넣는다.
                                memBuf.add(LatLng(coord[i], coord[i + 1]))
                            }
                            else {
                                // 0,0이 나왔을 경우, 즉, 리스트를 끊어줘야 할 경우
                                if (memBuf.size >= 3) {
                                    memory.add(mutableListOf<LatLng>())
                                    memory[memory.lastIndex].addAll(memBuf)
                                    memBuf.clear()
                                    memcolor.add(MultipartPathOverlay.ColorPart(Color.RED, Color.WHITE, Color.GRAY, Color.LTGRAY))
                                }
                            }
                        }
                        Log.d(
                            "MyTAG",
                            "memory data: $memory"
                        )
                        path.coordParts = memory
                        path.colorParts = memcolor
                        Log.d(
                            "MyTAG",
                            "찾아보자 오류!"
                        )
                        if (path.coordParts.size > 0) {
                            path.map = naverMap
                        }
                        Log.d(
                            "MyTAG",
                            "찾아보자 오류!!"
                        )
                    }
                }
                else {
                    Log.d("MyTAG", "No such document")
                    path.map = null
                }

                /*
                Log.d("MyTAG", "lastIndex ${markers.lastIndex}")
                for (i: Int in 0..markers.lastIndex) {
                    markers[i].map = null
                }
                markers.clear()
                if (document.get("marker") != null) {
                    Log.d("logForMarker","checked")
                    markerPoints = document.get("marker") as MutableList<Double>
                    markers.clear()
                    for (i: Int in 0 until markerPoints.size step(2)) {
                        markers.add(Marker())
                        markers[markers.lastIndex].position = LatLng(markerPoints[i], markerPoints[i+1])
                        markers[markers.lastIndex].map = naverMap
                        markers[markers.lastIndex].setOnClickListener{
                            if(it is Marker){
                                // it.position을 통해 클릭된 마커의 좌표값을 받아오는 것이 가능하다!!
                                Log.d("mylog","getMemory()에서 실행되는 코드입니당")
                                Log.d("mylog","마커의 좌표 ${it.position}")
                                Log.d("mylog","몇 번째로 찍힌 마커인가? ${markerPoints.indexOf(it.position.latitude) / 2}")
                                Toast.makeText(this,"마커가 선택되었습니다", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, DiaryShowDialog::class.java)
                                // todo 여기 putExtra value 에 날짜 + 마커번호로 된 값을 넘겨주세요
                                Log.d("mylog","넘길 값 한 눈에 보기: "+day+"+${markerPoints.indexOf(it.position.latitude) / 2}")
                                intent.putExtra("marker",day + "+${markerPoints.indexOf(it.position.latitude) / 2}")

                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)

                            }
                            true
                        }
                    }
                }
                */

            }
            .addOnFailureListener { exception ->
                Log.d("MyTAG", "get failed with ", exception)
            }

        // 마커 불러오기
        db.collection(userModel?.groupID.toString())
            .document(date)
            .collection("marker")
            .get()
            .addOnSuccessListener { documents ->

                // 현재 그려진 마커들 일단 정리
                for (i: Int in 0..markers.lastIndex) {
                    markers[i].map = null
                }
                markers.clear()
                // 불러온 애들 좌표값 받아서 지도에 그리기
                for (document in documents) {
                    // 지도에 그릴 마커 하나 추가
                    markers.add(Marker())
                    // 현재 다큐먼트의 좌표값 받아서 지도에 그릴 마커의 좌표값으로 적어주기
                    markers[markers.lastIndex].position = LatLng(document["Latitude"] as Double,
                        document["Longitude"] as Double
                    )
                    // 좌표값 적어준 후 지도에 그려주기
                    markers[markers.lastIndex].map = naverMap
                    // 클릭 리스너 달아주기
                    markers[markers.lastIndex].setOnClickListener {
                        if (it is Marker) {
                            // it.position을 통해 클릭된 마커의 좌표값을 받아오는 것이 가능하다!!
                            Log.d("mylog", "getMemory()에서 실행되는 코드입니당")
                            Log.d("mylog", "마커의 좌표 ${it.position}")
                            Toast.makeText(this, "마커가 선택되었습니다", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, DiaryShowDialog::class.java)
                            // todo 여기 putExtra value 에 날짜 + 마커번호로 된 값을 넘겨주세요
                            // 값 잘 넘기는지 확인용
                            Log.d("mylog"," day: $date")
                            Log.d("mylog","Lat: ${it.position.latitude}")
                            Log.d("mylog", "Long: ${it.position.longitude}")
                            // 날짜 넘겨주기
                            intent.putExtra("day", date)
                            // 위치 넘겨주기
                            intent.putExtra("Lat", it.position.latitude)
                            intent.putExtra("Long", it.position.longitude)
                            intent.putExtra("mode",isEditMode)

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)

                        }
                        true
                    }
                }


                /*
                Log.d("MyTAG", "lastIndex ${markers.lastIndex}")
                for (i: Int in 0..markers.lastIndex) {
                    markers[i].map = null
                }
                markers.clear()
                if (document.get("marker") != null) {
                    Log.d("logForMarker","checked")
                    markerPoints = document.get("marker") as MutableList<Double>
                    markers.clear()
                    for (i: Int in 0 until markerPoints.size step(2)) {
                        markers.add(Marker())
                        markers[markers.lastIndex].position = LatLng(markerPoints[i], markerPoints[i+1])
                        markers[markers.lastIndex].map = naverMap
                        markers[markers.lastIndex].setOnClickListener{
                            if(it is Marker){
                                // it.position을 통해 클릭된 마커의 좌표값을 받아오는 것이 가능하다!!
                                Log.d("mylog","getMemory()에서 실행되는 코드입니당")
                                Log.d("mylog","마커의 좌표 ${it.position}")
                                Log.d("mylog","몇 번째로 찍힌 마커인가? ${markerPoints.indexOf(it.position.latitude) / 2}")
                                Toast.makeText(this,"마커가 선택되었습니다", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, DiaryShowDialog::class.java)
                                // todo 여기 putExtra value 에 날짜 + 마커번호로 된 값을 넘겨주세요
                                Log.d("mylog","넘길 값 한 눈에 보기: "+day+"+${markerPoints.indexOf(it.position.latitude) / 2}")
                                intent.putExtra("marker",day + "+${markerPoints.indexOf(it.position.latitude) / 2}")

                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)

                            }
                            true
                        }
                    }
                }
                */

            }
            .addOnFailureListener { exception ->
                Log.d("MyTAG", "get failed with ", exception)
            }


        Log.d("MyTAG", "여기서 끝나는 건가?!")
    }

}