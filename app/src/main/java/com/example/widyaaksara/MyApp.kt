package com.example.widyaaksara

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Failed to load OpenCV!")
        } else {
            Log.d("OpenCV", "OpenCV loaded successfully")
        }
    }
}
