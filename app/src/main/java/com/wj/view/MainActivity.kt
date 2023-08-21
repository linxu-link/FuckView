package com.wj.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.wj.view.scale.ScaleView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val handler = Handler(Looper.getMainLooper())

        for (i in 0..155) {
            handler.postDelayed({
                findViewById<ScaleView>(R.id.scaleView).setCurrentValue(i)
            }, 150 * i.toLong())
        }

    }
}