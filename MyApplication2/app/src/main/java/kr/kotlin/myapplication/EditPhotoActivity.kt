package kr.kotlin.myapplication

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import kr.kotlin.myapplication.databinding.ActivityEditPhotoBinding
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.MultipartBody
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.RequestBody
//import okhttp3.RequestBody.Companion.asRequestBody
//import okhttp3.Response
//import okio.ByteString.Companion.decodeBase64
//import java.io.File


class EditPhotoActivity : AppCompatActivity() {
    var binding: ActivityEditPhotoBinding? = null
    var isBrushSizeSeekBarVisible = false
    var brushSizeStorage: Float? = 20.toFloat()
    val TAG = "EditPhotoActivity TAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPhotoBinding.inflate(layoutInflater)
        binding?.let {
            setContentView(it.root)
            val colorButton = it.buttonContainer
                ?.findViewById<ConstraintLayout>(R.id.select_color_button)
                ?.findViewById<ImageButton>(R.id.color_button)

            val uri = intent.data
            val prompt = intent.getStringExtra("prompt")
            if(prompt != null && prompt != "" && uri != null){
//                startChangeBackground(uri, prompt)
            }
            val colorContainer = it.colorContainer
            for (childView in colorContainer?.root?.children!!) {
                childView.setOnClickListener{cv ->
                    val colorContainerChildDrawable = (cv as ImageButton).background
                    var color: Int? = null
                    if(colorContainerChildDrawable is ColorDrawable){
                        color = colorContainerChildDrawable.color
                    } else {
                        color = Color.parseColor("#000000")
                    }
                    it.editImage.setColor(color)
                    colorButton?.setBackgroundColor(color)
                    colorContainer.root.visibility = View.GONE
                }
            }

            colorButton
                ?.setOnClickListener{
                    val root = colorContainer.root
                    val isVisible = root.visibility
                    if(isVisible == View.VISIBLE){
                        root.visibility = View.GONE
                    } else {
                        root.visibility = View.VISIBLE
                    }
                    setBrushSizeSeekBarUnvisible()
                }
            it.backgroundImage.setImageURI(intent.data)
            val editImage = it.editImage
            editImage.setOnClickListener{
                Log.d(TAG, "editImage 눌림")
                setBrushSizeSeekBarUnvisible()
            }
            it.eraserImageButton.setOnClickListener{
                editImage.setIsErasing()
                setBrushSizeSeekBarUnvisible()
            }
            it.brushImageButton.setOnClickListener{
                editImage.setIsDrawing()
                setBrushSizeSeekBarVisibility()
            }
            it.previous.setOnClickListener{
                editImage.onDelete()
                setBrushSizeSeekBarUnvisible()
            }
            if (Build.VERSION.SDK_INT >= 26) {
                // API 26 이상에서 호출되는 코드
                it.brushSizeSeekBar.min = 10
            }
            it.brushSizeSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    editImage.setSizeForBrush(progress.toFloat())
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // 슬라이딩을 시작할 때 호출되는 메서드
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // 슬라이딩을 멈춘 후 호출되는 메서드
                }
            })
        }
    }

    fun setBrushSizeSeekBarUnvisible(){
        if(isBrushSizeSeekBarVisible){
            binding?.brushSizeSeekBar?.visibility = View.GONE
            isBrushSizeSeekBarVisible = false

//            val editImage = binding?.editImage
//            editImage?.setSizeForBrush(
//                if (brushSizeStorage != null)
//                    brushSizeStorage!!
//                else
//                    20.toFloat()
//            )
        }
    }
    fun setBrushSizeSeekBarVisibility(){
        val editImage = binding?.editImage
        if(isBrushSizeSeekBarVisible){
            binding?.brushSizeSeekBar?.visibility = View.GONE
            isBrushSizeSeekBarVisible = false
//            editImage?.setSizeForBrush(
//                if (brushSizeStorage != null)
//                    brushSizeStorage!!
//                else
//                    20.toFloat()
//            )
        } else {
            binding?.brushSizeSeekBar?.visibility = View.VISIBLE
            isBrushSizeSeekBarVisible = true
            binding?.colorContainer?.root?.visibility = View.GONE
//            brushSizeStorage = editImage?.mBrushSize
//            editImage?.setSizeForBrush(0.toFloat())
        }
    }
    fun getFilePathFromUri(contentResolver: ContentResolver, uri: Uri): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex: Int = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = it.getString(columnIndex)
            }
            it.close()
        }
        return filePath
    }

//    fun startChangeBackground(uri: Uri, prompt: String){
//        Thread{
//            val contentResolver: ContentResolver = getContentResolver()
//            val uriPath = getFilePathFromUri(contentResolver, uri)
//            val lastIndex = uriPath?.lastIndexOf(".")
//            val extension = uriPath?.substring(lastIndex!! + 1)
//            val file = File(uriPath)
//            val fileRequestBody = file.asRequestBody("image/${extension}".toMediaTypeOrNull())
//
//            val requestBody: RequestBody = MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("prompt", prompt)
//                .addFormDataPart("imageFile", uriPath, fileRequestBody)
//                .build()
//
//            val request: Request = Request.Builder()
//                .header("x-api-key", getString(R.string.photoroom_api_key))
//                .url("https://beta-sdk.photoroom.com/v1/instant-backgrounds")
//                .post(requestBody)
//                .build()
//
//            val response: Response = OkHttpClient().newCall(request).execute()
//            val inputStream = response.body.byteStream()
//            val bitmap=BitmapFactory.decodeStream(inputStream)
//            binding?.backgroundImage?.setImageBitmap(bitmap)
//        }.start()
//    }
}