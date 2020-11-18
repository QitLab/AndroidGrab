package com.qit.androidgrab

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.qit.base.GrabController
import com.qit.india.GrabManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), GrabController.GrabListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        grab.setOnClickListener {
            GrabManager(this, this).getExif()
        }
    }


    override fun onReceive(dataType: Int, value: Any) {
        Log.i("grab-", value.toString())
    }
}