package com.hiroaki404.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hiroaki404.lifecycle.LogLifecycle

@LogLifecycle
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
