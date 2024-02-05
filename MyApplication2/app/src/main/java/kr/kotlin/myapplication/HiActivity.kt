package kr.kotlin.myapplication

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat

class HiActivity : AppCompatActivity() {
    var passedValue: Int? = null
    var intent2: Intent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hi)

        intent2 = getIntent()
        passedValue =  intent2?.getIntExtra("number", 1)?:1

        val textView = findViewById<TextView>(R.id.textView)
        textView?.text = "${passedValue}/9"
        textView?.setTextColor(Color.parseColor("#aaaaaa"))
        textView?.typeface=Typeface.MONOSPACE
        textView?.background=ContextCompat.getDrawable(this, R.drawable.ic_launcher_background)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.progress = passedValue!!
    }
    fun onNext(view: View){
        val intent = Intent(this, HiActivity::class.java)
        intent.putExtra("number", passedValue!! +1)
        startActivity(intent)
    }
}