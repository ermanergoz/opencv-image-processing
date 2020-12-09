package com.erman.imageprocessing

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.erman.imageprocessing.pipeline.FrameProcessor
import org.opencv.android.Utils
import org.opencv.core.Mat

object SamplePhoto {

    fun load(resources: Resources, drawableId: Int = R.drawable.cisim): Bitmap {
        val options = BitmapFactory.Options().apply { inScaled = false }
        return BitmapFactory.decodeResource(resources, drawableId, options)
    }

    fun processed(resources: Resources, processor: FrameProcessor): Bitmap {
        val rgba = Mat()
        try {
            val photo = load(resources)
            Utils.bitmapToMat(photo, rgba)
            photo.recycle()
            processor.process(rgba)
            val result = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(rgba, result)
            return result
        } finally {
            rgba.release()
        }
    }
}
