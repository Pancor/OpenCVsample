package pl.pancordev.opencvsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.act_find_blue.*
import org.opencv.android.*
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.*


class FindBlueActivity : AppCompatActivity(),
    CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var managerCallback: BaseLoaderCallback
    private lateinit var rgbaF: Mat
    private lateinit var rgbaT: Mat

    var points : Deque<Point> = ArrayDeque(50)
    var lastX = -1.0
    var lastY = -1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_find_blue)

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
        val rotatedFrame = rotateCamera(inputFrame)
        val hsv = convertToHSVList(rotatedFrame)

        val min = Scalar(100.0, 150.0, 0.0)
        val max = Scalar(140.0, 255.0, 255.0)

        Core.inRange(hsv, min, max, hsv)

        Imgproc.blur(hsv, hsv, Size(3.0, 3.0))
        Imgproc.erode(hsv, hsv, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(5.0, 5.0)))
        Imgproc.dilate(hsv, hsv, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(5.0, 5.0)))

        val moments = Imgproc.moments(hsv)
        val m01 = moments.m01
        val m10 = moments.m10
        val area = moments.m00

        if (area > 20000) {
            val x = m10 / area
            val y = m01 / area
            val r = Math.sqrt(area/Math.PI) / 11

            Imgproc.circle(
                rotatedFrame, Point(x, y), r.toInt(),
                Scalar(0.0, 255.0, 255.0), 2
            )

            if (points.size > 2) {

                for ((index, point) in points.withIndex()) {
                    if (index > 1) {
                        val thickness = (Math.sqrt((50/(index + 1).toDouble())) * 2.5).toInt()
                        Imgproc.line(
                            rotatedFrame, points.elementAt(index - 1),
                            point, Scalar(0.0, 0.0, 255.0), thickness
                        )
                    }
                }
            }

            points.addFirst(Point(x, y))
            if (points.size > 50) points.removeLast()

            lastY = y
            lastX = x
        }


        return rotatedFrame
    }

    private fun rotateCamera(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val rgba = inputFrame.rgba()
        Core.transpose(rgba, rgbaT)
        Imgproc.resize(rgbaT, rgbaF, rgbaF.size(), 0.0, 0.0, 0)
        Core.flip(rgbaF, rgba, 1)
        return rgba
    }

    private fun convertToHSVList(frame: Mat): Mat {
        val hsv = Mat()
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HSV)
        //val hsvList = mutableListOf<Mat>()
        //Core.split(hsv, hsvList)
        return hsv
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
