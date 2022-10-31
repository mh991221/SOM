package CapStoneDisign.som

import CapStoneDisign.som.Model.UserModel
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.LocationButtonView


class MainActivity : AppCompatActivity(), OnMapReadyCallback,NavigationView.OnNavigationItemSelectedListener {

    private val userDB: DatabaseReference by lazy{
        Firebase.database.reference.child(DBKey.DB_USERS)
    }

    private val mapView: MapView by lazy{
        findViewById(R.id.mapView)
    }

    private val mainDrawerLayout: DrawerLayout by lazy {
        findViewById(R.id.mainDrawerLayout)
    }

    private val toolbarLayout: androidx.appcompat.widget.Toolbar by lazy{
        findViewById(R.id.main_layout_toolbar)
    }

    private val navigationView: NavigationView by lazy {
        findViewById(R.id.navigationView)
    }

    private val auth: FirebaseAuth by lazy{
        Firebase.auth
    }

    private val currentLocationButton: LocationButtonView by lazy{
        findViewById(R.id.currentLocationButton)
    }

    private lateinit var naverMap: NaverMap

    var updateMap = HashMap<String, Any>()

    private var count: Boolean = true

    private lateinit var locationSource: FusedLocationSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_drawer_layout)

        if(auth.currentUser == null){
            val intent= Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(this)

        checkGroup()
        initToolBar()
        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun initToolBar(){
        setSupportActionBar(toolbarLayout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0

//        val cameraUpdate = CameraUpdate.scrollTo(LatLng(350.0, 127.027512))
//
//        naverMap.moveCamera(cameraUpdate)

        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false

        currentLocationButton.map = naverMap

        locationSource = FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource


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

        naverMap.locationTrackingMode = LocationTrackingMode.Follow
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean{
        when(item.itemId){
            R.id.accountInfoIcon -> Toast.makeText(this,"account Clicked", Toast.LENGTH_SHORT).show()
            R.id.diaryIcon -> Toast.makeText(this,"diary Clicked", Toast.LENGTH_SHORT).show()
            R.id.settingIcon -> Toast.makeText(this,"account Clicked", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onBackPressed() {
        if(mainDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mainDrawerLayout.closeDrawers()
            Toast.makeText(this,"drawer is closed", Toast.LENGTH_SHORT).show()
        }else{
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
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
        mapView.onStop()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    private fun checkGroup(){
        val currentGroup = userDB.child(getCurrentUserID())
        currentGroup.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val userModel = snapshot.getValue<UserModel>()
                if(userModel?.groupID == null && count){
                    val dlg = GroupDialog(this@MainActivity)
                    dlg.start()
                    count = false
                    return
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun getCurrentUserID(): String{
        return auth.currentUser?.uid.orEmpty()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


}