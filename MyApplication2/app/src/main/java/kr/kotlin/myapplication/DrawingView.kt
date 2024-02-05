package kr.kotlin.myapplication

import android.R.attr.path
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView


class DrawingView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs){
    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? =null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private val mPaths = ArrayList<CustomPath>()
    var isErasing = false
    private val imageResourceId : Int? = null
    init {
        setUpDrawing()
    }

    private fun setUpDrawing(){
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 20.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    // Change Canvas to Canvas? if fails
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)
        for (path in mPaths){
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)

        }
        if(!mDrawPath!!.isEmpty){
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                if(isErasing){
                    eraseTouchedPoint(touchX!!, touchY!!)
                } else {
                    drawTouchedPoint(touchX!!, touchY!!)
                }
            }
            MotionEvent.ACTION_MOVE ->{
                if(touchX != null && touchY != null){
                    mDrawPath!!.lineTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_UP -> {
                mDrawPath = CustomPath(color, mBrushSize)
                Log.d("himaru", "hi")
            }
            else -> return false
        }
        invalidate()
        return true
    }


    private fun eraseTouchedPoint(touchX: Float, touchY: Float){
        mPaths.add(mDrawPath!!)
        mDrawPath!!.color = Color.argb(0,0,0,0)
        mDrawPath!!.brushThickness = mBrushSize

        //reset함수 지워보기
        mDrawPath!!.reset()
        mDrawPath!!.moveTo(touchX, touchY)
        Log.d("himaru", "DOwn")

        Log.d("himaru", mPaths.size.toString())
    }
    private fun drawTouchedPoint(touchX: Float, touchY: Float) {

        mPaths.add(mDrawPath!!)
        mDrawPath!!.color = color
        mDrawPath!!.brushThickness = mBrushSize

        //reset함수 지워보기
        mDrawPath!!.reset()
        mDrawPath!!.moveTo(touchX, touchY)
        Log.d("himaru", "DOwn")

        Log.d("himaru", mPaths.size.toString())
    }
    internal fun onDelete(){
        mPaths.removeAt(mPaths.size-1)
        invalidate()
    }
    fun setSizeForBrush(newSize: Float){
        mBrushSize = newSize
        mDrawPaint!!.strokeWidth = mBrushSize
    }
    fun setColor(newColor: String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color=color
    }
    internal inner class CustomPath(var color: Int, var brushThickness: Float): Path()
}