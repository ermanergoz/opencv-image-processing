package com.erman.imageprocessing

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class StarterApplication : Application() {

    var isOpenCvLoaded = false
        private set

    override fun onCreate() {
        super.onCreate()
        // The OpenCV native libraries ship inside the APK, so no OpenCV Manager is needed
        isOpenCvLoaded = OpenCVLoader.initDebug()
        if (isOpenCvLoaded) {
            Log.d(TAG, "OpenCV ${OpenCVLoader.OPENCV_VERSION} loaded from the bundled .so libs.")
        } else {
            Log.e(TAG, "OpenCV native libraries failed to load.")
        }
    }

    companion object {
        private const val TAG = "StarterApplication"
    }
}
