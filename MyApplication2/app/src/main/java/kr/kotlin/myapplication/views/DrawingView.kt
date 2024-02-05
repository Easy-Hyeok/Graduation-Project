package kr.kotlin.myapplication.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import kr.kotlin.myapplication.EditPhotoActivity
import kr.kotlin.myapplication.R

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs){
    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? =null
    private var mCanvasPaint: Paint? = null
    var mBrushSize: Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private val mPaths = ArrayList<CustomPath>()
    private var isErasing = false
    private var isDrawing = true
    private var brushSizeSeekBar: SeekBar? = null
    fun setIsErasing(){
        isDrawing = false
        isErasing = true
    }
    fun setIsDrawing(){
        isDrawing = true
        isErasing = false
    }
    fun clickOtherButton(){
        isDrawing = false
        isErasing = false
    }
    init {
        setUpDrawing()
    }

    private fun setUpDrawing(){
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize, false)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 20.toFloat()
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)
        for (path in mPaths) {
            if (path.isErasing) {
                Log.d("어디보자", "ㅁ실행")
                // brushThickness가 0이면 지우개로 간주하고 Xfermode를 PorterDuff.Mode.CLEAR로 설정
                mDrawPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                mDrawPaint!!.strokeWidth = path.brushThickness
            } else {
                mDrawPaint!!.strokeWidth = path.brushThickness
                mDrawPaint!!.color = path.color
                mDrawPaint?.xfermode = null
            }
            canvas.drawPath(path, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                onTouchDown(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_MOVE ->{
                if(touchX != null && touchY != null){
                    if(isErasing){
                        mDrawPath!!.isErasing = true
                    }
                    mDrawPath!!.lineTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_UP -> {
                mDrawPath = CustomPath(color, mBrushSize, false)
            }
            else -> return false
        }
        invalidate()
        return true
    }
    private fun eraseTouchedPoint(touchX: Float, touchY: Float) {
        mDrawPath?.lineTo(touchX, touchY) // 지우개 터치 지점을 그리기 위한 작은 선
    }
    private fun drawTouchedPoint(touchX: Float, touchY: Float) {

        mPaths.add(mDrawPath!!)
        mDrawPath!!.color = color
        mDrawPath!!.brushThickness = mBrushSize

        //reset함수 지워보기
        mDrawPath!!.reset()
        mDrawPath!!.moveTo(touchX, touchY)
    }

    internal fun onDelete(){
        mPaths.removeAt(mPaths.size-1)
        invalidate()
    }
    fun setSizeForBrush(newSize: Float){
        mBrushSize = newSize
        mDrawPaint!!.strokeWidth = mBrushSize
    }
    fun setColor(newColor: Int){
        color = newColor
        mDrawPaint!!.color= newColor
    }
    fun onTouchDown(x: Float, y: Float){
        drawTouchedPoint(x, y)
        if(brushSizeSeekBar == null){
            brushSizeSeekBar = (parent as RelativeLayout).findViewById<SeekBar>(R.id.brush_size_seek_bar)
        }
        if(brushSizeSeekBar!!.visibility == VISIBLE){
            brushSizeSeekBar!!.visibility = GONE
        }
    }
    internal inner class CustomPath(var color: Int, var brushThickness: Float, var isErasing: Boolean): Path()
}