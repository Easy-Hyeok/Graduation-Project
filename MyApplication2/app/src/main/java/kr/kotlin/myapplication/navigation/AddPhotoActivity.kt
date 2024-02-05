package kr.kotlin.myapplication.navigation

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kr.kotlin.myapplication.EditPhotoActivity
import kr.kotlin.myapplication.R
import kr.kotlin.myapplication.databinding.ActivityAddPhotoBinding
import kr.kotlin.myapplication.databinding.FragmentAlarmBinding
import kr.kotlin.myapplication.navigation.model.ContentDTO
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    //    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    private lateinit var binding: ActivityAddPhotoBinding
    private lateinit var cbActivityResultLauncher: ActivityResultLauncher<Intent>
    private val TAG = "ADDPHOTOACTIVITY TAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate 호출")
        // 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 앨범 열기
        var photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
        photoPickerIntent.type = "image/*"
        cbActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // 1> it 사용 예제 (it : ActivityResult!)
            if(it.resultCode== RESULT_OK){
                //사진을 선택했을 때 이미지 경로가 이쪽으로 넘어옴
                photoUri = it.data?.data
                Log.d(TAG, "RESULT_OK URI PATH${photoUri?.path}")
                binding.addPhotoImage.setImageURI(photoUri)
            } else {
                //사진을 선택하지 않고 취소를 했을 때
//                finish()
                Log.d(TAG, "NOT RESULT OK")
            }
        }
        Log.d(TAG, "onCreate 호출")
        cbActivityResultLauncher.launch(photoPickerIntent)
//        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)

        Log.d(TAG, "onCreate 호출")
//        binding.addPhotoButton.setOnClickListener { contentUpload() }
        // 업로드 버튼에 contentUpload 연결
        binding.toolbarNextButton.setOnClickListener{
            moveToNext()
        }
    }

    private fun moveToNext(){
        val prompt = binding.editPrompt.text.toString()
        if(prompt != ""){
            val intent = Intent(this, EditPhotoActivity::class.java)
            intent.setData(photoUri)
            intent.putExtra("prompt", binding.editPrompt.text.toString())
            startActivity(intent)
        } else {
            toastMessage("프롬프트를 입력하세요")
        }
    }
    private fun toastMessage(message: String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }
    fun contentUpload(){
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        // 이미지 이름을 현재시간으로 정해줘서 중복 방지

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        // 이미지 업로드
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {

            // 이미지 주소 받아오기
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO()

                // 이미지 주소 넣어주기
                contentDTO.imageUrl = uri.toString()

                // 유저 uid 넣어주기
                contentDTO.uid = auth?.currentUser?.uid

                // 유저 아이디 넣어주기
                contentDTO.userId = auth?.currentUser?.email

                // 설명 넣어주기
//                contentDTO.explain = binding.addPhotoEdit.text.toString()

                // 타임스태프 넣어주기
                contentDTO.timestamp = System.currentTimeMillis()

                // 값 넘겨주기
                firestore?.collection("images")?.document()?.set(contentDTO)?.addOnSuccessListener {
                    // 업로드 성공 시 수행할 작업
                    toastMessage("파이어스토어에 올림")
                }?.addOnFailureListener {
                    // 업로드 실패 시 수행할 작업
                    toastMessage("${it.message}")
                }

                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}
