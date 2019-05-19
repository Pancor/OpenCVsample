package pl.pancordev.opencvsample

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Could not initialize")
        } else {
            Log.d("OpenCV", "Initialized")
        }
    }
}