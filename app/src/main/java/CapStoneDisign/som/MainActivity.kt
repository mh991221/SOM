package CapStoneDisign.som

import CapStoneDisign.som.Model.UserModel
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
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


class MainActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

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

    private lateinit var map: GoogleMap

    var updateMap = HashMap<String, Any>()

    private var count: Boolean = true

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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val myLocation = LatLng(37.654601,127.060530)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(10f))

        mapSetting(map)
    }

    private fun mapSetting(map: GoogleMap){


        map.setMinZoomPreference(6.0f)
        map.setMaxZoomPreference(14.0f)
        map.setPadding(0,0,0,150)

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isRotateGesturesEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true
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

}