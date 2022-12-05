package CapStoneDisign.som

import CapStoneDisign.som.databinding.CameraLayoutBinding
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaActionSound
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.*


class CameraActivity : AppCompatActivity() {

    // DB에 값 올리기 위해 파이어스토어
    val db = Firebase.firestore

    lateinit private var binding: CameraLayoutBinding

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null // 현재 위치를 가져오기 위한 변수
    lateinit var mLastLocation: Location // 위치 값을 가지고 있는 객체
    internal lateinit var mLocationRequest: LocationRequest // 위치 정보 요청의 매개변수를 저장하는
    private val REQUEST_PERMISSION_LOCATION = 10

    private var standardLatitude = 0.0
    private var standardLongitude = 0.0

    private var presentLatitude = 0.0
    private var presentLongitude = 0.0
    private val numberToCalculateDistance = 6372.8 * 1000

    lateinit var storage: FirebaseStorage

    var count = 0
    val stackImage = Stack<Uri>()

    val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    val CAMERA_PERMISSION_REQUEST = 100

    val STORAGE_PERMISSION = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private val user = auth.currentUser!!.uid
    lateinit var fileName: String
    var imageCount = 0
    val STORAGE_PERMISSION_REQUEST = 200
    private var isMarkerCreated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CameraLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        startCamera()
        checkPermission(CAMERA_PERMISSION, CAMERA_PERMISSION_REQUEST)
        checkPermission(STORAGE_PERMISSION, STORAGE_PERMISSION_REQUEST)
        mLocationRequest =  LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
            startLocationUpdates()
            val sound = MediaActionSound()
            sound.play(MediaActionSound.SHUTTER_CLICK)
        }


        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startLocationUpdates() {

        //FusedLocationProviderClient의 인스턴스를 생성.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        // 기기의 위치에 관한 정기 업데이트를 요청하는 메서드 실행
        // 지정한 루퍼 스레드(Looper.myLooper())에서 콜백(mLocationCallback)으로 위치 업데이트를 요청
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )

        mFusedLocationProviderClient!!.lastLocation
            .addOnSuccessListener { location ->
                if(location == null) {
                    Log.e("camera1", "location get fail")
                } else {
                    onLocationChanged(location)

                    Log.d("camera1", "${location.latitude} , ${location.longitude},$count")
                }
            }
            .addOnFailureListener {
                Log.e("camera1", "location error is ${it.message}")
                it.printStackTrace()
            }

    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.d("camera count2","$count")
            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    // 시스템으로 부터 받은 위치정보를 화면에 갱신해주는 메소드
    fun onLocationChanged(location: Location) {
        mLastLocation = location
//        text2.text = "위도 : " + mLastLocation.latitude // 갱신 된 위도
//        text1.text = "경도 : " + mLastLocation.longitude // 갱신 된 경도

        if(count == 0){
            isMarkerCreated = false
            standardLatitude = mLastLocation.latitude
            standardLongitude = mLastLocation.longitude
            count++
        }else {
            presentLatitude = mLastLocation.latitude
            presentLongitude = mLastLocation.longitude

            val dLat = Math.toRadians(presentLatitude - standardLatitude)
            val dLon = Math.toRadians(presentLongitude - standardLongitude)

            val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(Math.toRadians(presentLatitude)) * cos(Math.toRadians(standardLatitude))
            val c = 2 * asin(sqrt(a))
            val distance =  (numberToCalculateDistance * c).toInt()

            if(distance <= 100){
                count++
            }else{
                isMarkerCreated = false
                count = 1
                var topUri = stackImage.pop()
                while(stackImage.isNotEmpty()){
                    stackImage.pop()
                }
                stackImage.push(topUri)
                standardLatitude = mLastLocation.latitude
                standardLongitude = mLastLocation.longitude
            }
        }



        if(count > 5 && !isMarkerCreated){
            /*todo (standardLatitude, standardLongitude)에 photoZone tag 를 달은 마커를 생성
             내 생각엔 마커에 tag를 저장할 필요가 있는데 이건 어떻게 할까 그 diary 저장한 거기에 tag 도 같이 저장할 수 있나
             포토존 태그를 달면 사진 uri도 저장해야해서 marker 이름으로 폴더를 하나 만드는게 좋아보임
             그니까 userID -> 날짜 -> marker이름 -> tag, uri, text 이런식
             현시점에서 바로 보여줄 필요 없음(naverMap 이랑 일일히 연결하기 귀찮을거 같음)
             그냥 DB Marker 에 넣어만 두고 나중에 불러올때만 나타나도록 해주세요 */
            isMarkerCreated = true
            // 메인에서 받아온 오늘의 날짜
            var day = intent.getStringExtra("day")
            var groupID = intent.getStringExtra("groupID")

            val storage = FirebaseStorage.getInstance()

            // 위치 값 토대로, 포토존으로 만들어진 마커에 대한 정보를 꾸린다.
            var tag = "photo"
            var wrote = 0
            val marker = hashMapOf(
                "tag" to tag,
                "Latitude" to standardLatitude,
                "Longitude" to standardLongitude,
                "wrote" to wrote
            )
            // 마커를 특정하기 위해, 위치정보를 토대로 마커의 이름을 만든다.
            var docName = "$standardLatitude:$standardLongitude"

            fileName = "${groupID}:photo/$day:$standardLatitude:$standardLongitude"

            imageCount = 0
            while(stackImage.isNotEmpty()){
                var topImageUri = stackImage.pop()
                Log.d("topImageUri","$topImageUri")
                storage.getReference().child("image").child(fileName).child(imageCount.toString())
                    .putFile(topImageUri)
                imageCount++
            }


            db.collection(groupID.toString())
                .document(day.toString())
                .collection("marker")
                .document(docName)
                .set(marker, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("Mylog", "사진 마커 저장 완료!")
                }
                .addOnFailureListener { e ->
                    Log.w(
                        "MyLog",
                        "사진 마커 저장에 실패함!",
                        e
                    )
                }
        }else if(count > 5 && isMarkerCreated){
            var day = intent.getStringExtra("day")
            var groupID = intent.getStringExtra("groupID")

            val storage = FirebaseStorage.getInstance()

            if(stackImage.isNotEmpty()){
                var topImageUri = stackImage.pop()
                storage.getReference().child("image").child(fileName).child(imageCount.toString())
                    .putFile(topImageUri)
                Log.d("topImageUri2","$topImageUri")
            }

        }

    }


    private fun checkPermission(permissions: Array<String>, permissionRequestNumber: Int) {
        val permissionResult = ContextCompat.checkSelfPermission(this, permissions[0])

        when (permissionResult) {
            PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                // Go Main Function
            }
            PackageManager.PERMISSION_DENIED -> {
                ActivityCompat.requestPermissions(this, permissions, permissionRequestNumber)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
                    // Go Main Function
                } else {
                    Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
                    // Finish() or Show Guidance on the need for permission
                }
            }
            STORAGE_PERMISSION_REQUEST -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
                    // Go Main Function
                } else {
                    Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
                    // Finish() or Show Guidance on the need for permission
                }
            }
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            newJpgFileName()
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {

                    Log.d("CameraX-Debug", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    stackImage.push(savedUri)
                    var topUri = stackImage.pop()
                    stackImage.push(savedUri)
                    Log.d("uriCheck","$topUri")
                }
            })
    }

    // viewFinder 설정 : Preview
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            // ImageCapture
            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )

            } catch (exc: Exception) {
                Log.d("CameraX-Debug", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun newJpgFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir
        else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


}