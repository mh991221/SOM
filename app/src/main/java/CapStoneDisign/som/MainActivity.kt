package CapStoneDisign.som


// import android.os.Build.VERSION_CODES.R
import CapStoneDisign.som.Model.UserModel
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
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
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.LocationButtonView
import java.time.LocalDate


class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener {

    //Firestore 레퍼런스. 이거 써서 Firestore에 값 올리고 내린다.
    val db = Firebase.firestore

    //현재 데이트 중인 경로 좌표들의 리스트
    var routes = mutableListOf<LatLng>()

    // DB에 넣어서 쓰려는 좌표값들 담아둘 더블리스트.
    var routes_d = mutableListOf<Double>()

    //DB에서 받아온 경로의 좌표들 담아둘 리스트
    var memory = mutableListOf<LatLng>()

    //현재 기록중인지 체크용
    var checkWritingOrNot = 0

    //현재 사용자 정보 받아오기 용
    var userModel: UserModel? = null

    // 경로 그리기 위해 필요한 애. 얘가 담고 있는 정보로 경로를 그리게 된다.
    var path = PathOverlay()

    var context: Context? = null

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

    private val dateButton: Button by lazy {
        findViewById(R.id.dateButton)
    }

    private lateinit var naverMap: NaverMap

    private var isTracking: Int = 0
    private var groupID: String?= null

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


        initToolBar()
        navigationView.setNavigationItemSelectedListener(this)
    }


    private fun initToolBar() {
        setSupportActionBar(toolbarLayout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        dateButton.setOnClickListener {
            val dlg = DateQRDialog(this)
            dlg.start()
            dlg.setOnOKClickedListener{ content ->
                if(content.compareTo("intent") == 0){
                    val intent = Intent(this, QrCodeActivity::class.java)
                    startActivityForResult(intent,486486)
                }
            }
        }

        val currUser = userDB.child(auth.currentUser!!.uid)
        currUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModel = snapshot.getValue<UserModel>()
                groupID = userModel?.groupID
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

    @SuppressLint("MissingPermission")
    fun setUpdateLocationListener() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY //높은 정확도
            interval = 1000 //1초에 한번씩 GPS 요청
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for ((i, location) in locationResult.locations.withIndex()) {
                    Log.d("location1: ", "${location.latitude}, ${location.longitude}")
                    if (checkWritingOrNot == 1) {
                        routes.add(LatLng(location.latitude, location.longitude))
                        if(routes.size>=3){
                            path.coords = routes
                            path.map = naverMap
                        }
                        routes_d.add(location.latitude)
                        routes_d.add(location.longitude)
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
            R.id.diaryIcon -> Toast.makeText(this, "diary Clicked", Toast.LENGTH_SHORT).show()
            R.id.settingIcon -> Toast.makeText(this, "account Clicked", Toast.LENGTH_SHORT).show()
            R.id.startIcon -> {
                checkWritingOrNot = 1
            }
            R.id.endIcon -> {
                checkWritingOrNot = 0
                routes.add(LatLng(0.0, 0.0))
                routes_d.add(0.0)
                routes_d.add(0.0)

                // 참 미스테리하죠잉 왜 처음 누를 때는 get("routes")가 널이면서 두번째부터는 제대로 받아오는 것일까잉
                // 이것도 위치정보가 처음에 바로 안 뜨는 거랑 좀 비슷한 상황일까???
                db.collection(userModel?.groupID.toString()).document(LocalDate.now().toString())
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            Log.d(
                                "MyTAG",
                                "DocumentSnapshot data: ${document.id} ${document.get("route")}"
                            )
                        } else {
                            Log.d("MyTAG", "No such document")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("MyTAG", "get failed with ", exception)
                    }
                val course = hashMapOf(
                    "route" to routes_d
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
            R.id.memIcon -> {
                // 그래도 없으면 허전하니까 달아는 뒀음. 이 getMemory()에 매개변수로
                // 불러오고 싶은 날짜 값을 스트링으로 넣어주면 됨.
                // 날짜 값 형태는 2022-11-15 같은 형태인 건 아시져?!?!
                getMemory(LocalDate.now().toString())
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
                if (document != null) {
                    var coord = document.get("route") as MutableList<Double>
                    Log.d(
                        "MyTAG",
                        "DocumentSnapshot data: ${document.id} ${document.get("route")}"
                    )
                    if (coord.size >= 8) {
                        memory.clear()
                        for (i: Int in 0 until coord.size step(2)) {
                            memory.add(LatLng(coord[i], coord[i+1]))
                        }
                        path.coords = memory
                        path.map = naverMap
                    }

                } else {
                    Log.d("MyTAG", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("MyTAG", "get failed with ", exception)
            }
    }


}