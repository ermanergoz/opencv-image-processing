// Camera1 is what OpenCV 3.4's JavaCameraView uses
@file:Suppress("DEPRECATION")

package com.erman.imageprocessing

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.erman.imageprocessing.pipeline.FrameProcessor
import com.erman.imageprocessing.pipeline.MeasurementPipeline
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat

class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private enum class Mode { CAMERA, PHOTO }

    private var mode = Mode.PHOTO

    private val liveProcessorHolder = lazy<FrameProcessor> { MeasurementPipeline.live() }
    private val liveProcessor by liveProcessorHolder

    private val processedPhoto: Bitmap by lazy {
        val processor = MeasurementPipeline.still()
        try {
            SamplePhoto.processed(resources, processor)
        } finally {
            processor.release()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!(application as StarterApplication).isOpenCvLoaded) {
            Toast.makeText(this, R.string.opencv_not_loaded, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        camera_view.setMaxFrameSize(MAX_FRAME_WIDTH, MAX_FRAME_HEIGHT)
        camera_view.setCvCameraViewListener(this)
        mode_button.setOnClickListener { show(if (mode == Mode.CAMERA) Mode.PHOTO else Mode.CAMERA) }

        val hasBackCamera = findBackCamera()
        if (!hasBackCamera) mode_button.visibility = View.GONE

        val saved = savedInstanceState?.getString(KEY_MODE)?.let { Mode.valueOf(it) }
        show(if (hasBackCamera) saved ?: Mode.CAMERA else Mode.PHOTO)
    }

    override fun onResume() {
        super.onResume()
        if (mode == Mode.CAMERA && hasCameraPermission()) camera_view.enableView()
    }

    override fun onPause() {
        camera_view.disableView()
        super.onPause()
    }

    override fun onDestroy() {
        camera_view.disableView()
        if (liveProcessorHolder.isInitialized()) liveProcessor.release()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_MODE, mode.name)
    }

    private fun show(newMode: Mode) {
        if (newMode == Mode.CAMERA && !hasCameraPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
            return
        }
        mode = newMode
        when (newMode) {
            Mode.CAMERA -> {
                photo_view.visibility = View.GONE
                camera_view.visibility = View.VISIBLE
                camera_view.enableView()
                mode_button.setImageResource(R.drawable.ic_photo)
                mode_button.contentDescription = getString(R.string.show_sample_photo)
            }
            Mode.PHOTO -> {
                camera_view.disableView()
                camera_view.visibility = View.GONE
                photo_view.setImageBitmap(processedPhoto)
                photo_view.visibility = View.VISIBLE
                mode_button.setImageResource(R.drawable.ic_camera)
                mode_button.contentDescription = getString(R.string.show_live_camera)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_CAMERA || grantResults.isEmpty()) return
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            show(Mode.CAMERA)
        } else {
            Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_LONG).show()
            show(Mode.PHOTO)
        }
    }

    /** JavaCameraView opens the back camera; without one it shows a dialog and quits */
    private fun findBackCamera(): Boolean {
        val info = Camera.CameraInfo()
        return (0 until Camera.getNumberOfCameras()).any {
            Camera.getCameraInfo(it, info)
            info.facing == Camera.CameraInfo.CAMERA_FACING_BACK
        }
    }

    private fun hasCameraPermission() =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onCameraViewStarted(width: Int, height: Int) {}

    override fun onCameraViewStopped() {}

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val rgba = inputFrame.rgba()
        liveProcessor.process(rgba)
        return rgba
    }

    companion object {
        private const val REQUEST_CAMERA = 1
        private const val KEY_MODE = "mode"
        private const val MAX_FRAME_WIDTH = 1280
        private const val MAX_FRAME_HEIGHT = 720
    }
}
