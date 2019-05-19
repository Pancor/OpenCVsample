package pl.pancordev.opencvsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.imgproc.Imgproc


class MainActivity : AppCompatActivity(),
    CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var managerCallback: BaseLoaderCallback
    private lateinit var rgbaF: Mat
    private lateinit var rgbaT: Mat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView.setCvCameraViewListener(this)

        managerCallback = object : BaseLoaderCallback(this) {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    LoaderCallbackInterface.SUCCESS -> {
                        cameraView.enableView()
                    }
                    else -> {
                        super.onManagerConnected(status)
                    }
                }
            }
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Toast.makeText(this, "Camera started", Toast.LENGTH_SHORT).show()
        rgbaF = Mat(height, width, CvType.CV_8UC4)
        rgbaT = Mat(height, width, CvType.CV_8UC4)
    }

    override fun onCameraViewStopped() {
        Toast.makeText(this, "Camera stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val rgba = inputFrame.rgba()
        Core.transpose(rgba, rgbaT)
        Imgproc.resize(rgbaT, rgbaF, rgbaF.size(), 0.0, 0.0, 0)
        Core.flip(rgbaF, rgba, 1)
        return rgba
    }

    override fun onPause() {
        super.onPause()
        cameraView.disableView()
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, managerCallback)
        } else {
            managerCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }
}
