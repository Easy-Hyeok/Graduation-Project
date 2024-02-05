package kr.kotlin.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kr.kotlin.myapplication.databinding.ActivityMainBinding
import kr.kotlin.myapplication.navigation.AddPhotoActivity
import kr.kotlin.myapplication.navigation.AlarmViewFragment
import kr.kotlin.myapplication.navigation.DetailViewFragment
import kr.kotlin.myapplication.navigation.GridFragment
import kr.kotlin.myapplication.navigation.UserFragment

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var detailViewFragment = DetailViewFragment()
    private val TAG = "MainActivity TAG"
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        setToolbarDefault()

        when(item.itemId){
            R.id.home ->{
                supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()
                return true
            }
            R.id.search ->{
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,gridFragment).commit()
                return true
            }
            R.id.add_photo ->{
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    ){
                    // 권한 체크해서 권한이 있을 때
                    Log.d(TAG, "addPhoto 권한 있음")
                    startActivity(Intent(this, AddPhotoActivity::class.java))

                }
                Log.d(TAG, "addPhoto 호출")
                return true
            }
            R.id.favorite_alarm ->{
                var alarmFragment = AlarmViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,alarmFragment).commit()
                return true
            }
            R.id.account ->{
                var userFragment = UserFragment()
                var bundle = Bundle()
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                Log.d(TAG, "Main Activity uid :${uid}")
                bundle.putString("destinationUid",uid)
                userFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.main_content,userFragment).commit()
                return true
            }
        }
        return false
    }
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNavigation?.setOnNavigationItemSelectedListener(this)
        supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()

        ActivityCompat.requestPermissions(this, arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_MEDIA_IMAGES
        ),1)
//        registerPushToken()
    }

    // 툴바 기본 이미지 설정
    fun setToolbarDefault(){
        binding.toolbarBackButton.visibility = View.GONE
        binding.topImage.visibility = View.VISIBLE
    }

    // 푸시 토큰 생성
//    fun registerPushToken(){
//        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
//            task ->
//            val token = task.result?.token
//            val uid = FirebaseAuth.getInstance().currentUser?.uid
//            val map = mutableMapOf<String,Any>()
//            map["pushToken"] = token!!
//
//            FirebaseFirestore.getInstance().collection("pushtokens").document(uid!!).set(map)
//        }
//    }
//

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        // 프로필 사진 이미지 파이어베이스에 올리기
//        if(requestCode == UserFragment.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
//            var imageUri = data?.data
//            var uid = FirebaseAuth.getInstance().currentUser?.uid
//            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
//            storageRef.putFile(imageUri!!).continueWithTask { task : Task<UploadTask.TaskSnapshot> ->
//                return@continueWithTask storageRef.downloadUrl
//            }.addOnSuccessListener { uri ->
//                var map = HashMap<String,Any>()
//                map["image"] = uri.toString()
//                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
//            }
//        }
//    }
}