package pl.pancordev.opencvsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.act_load_video.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class FindBallsActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var managerCallback: BaseLoaderCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_find_balls)

        cameraView.setCvCameraViewListener(this)
        managerCallback = object : BaseLoaderCallback(this) {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    LoaderCallbackInterface.SUCCESS -> { cameraView.enableView() }
                    else -> { super.onManagerConnected(status) }
                }
            }
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) { }

    override fun onCameraViewStopped() { }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val result = inputFrame.rgba()
        val gray = inputFrame.gray()

        //reduce noise
        Imgproc.blur(gray, gray, Size(9.0, 9.0))

        //find circles
        val circles = Mat()
        Log.e("ERROR", "started")
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 2.0,
            10.0, 100.0, 50.0,
            70, 80)
        Log.e("ERROR", "finished")

        //draw circles
        val size = if (circles.cols() > 17) { 17 } else { circles.cols() }
        for (x in 0 until size) { //16 because there is only 16 balls
            Log.e("ERROR", "$x")
            val c = circles.get(0, x)
            val center = Point(Math.round(c[0]).toDouble(), Math.round(c[1]).toDouble())
            val radius = Math.round(c[2]).toInt()
            Imgproc.circle(gray, center, radius, Scalar(255.0, 0.0, 255.0),
                3, 8, 0)
        }
        return gray
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

    override fun onDestroy() {
        super.onDestroy()
        cameraView.disableView()
    }
}
