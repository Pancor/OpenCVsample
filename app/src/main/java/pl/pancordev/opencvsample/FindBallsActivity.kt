package pl.pancordev.opencvsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.act_load_video.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.core.CvType.CV_8UC1
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.drawContours
import org.opencv.imgproc.Imgproc.minAreaRect
import pl.pancordev.opencvsample.tools.TableManager
import pl.pancordev.opencvsample.tools.TableManagerImpl
import java.util.*

class FindBallsActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var managerCallback: BaseLoaderCallback

    private lateinit var tableManager: TableManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_find_balls)
        tableManager = TableManagerImpl()
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
        return table(inputFrame)
//        return findBalls(inputFrame)
    }

    private fun findBalls(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
//        val gray = inputFrame.gray()
//        Imgproc.blur(gray, gray, Size(9.0, 9.0))

        val tableMat = inputFrame.rgba()

        val resized = Mat()
        Imgproc.resize(tableMat, resized, Size(tableMat.size().width * 0.1, tableMat.size().height * 0.1))
        val ratio = tableMat.size().width / resized.size().width

        val hsv = Mat()
        Imgproc.cvtColor(resized, hsv, Imgproc.COLOR_BGR2HSV)
        val blurred = Mat()
        Imgproc.GaussianBlur(hsv, blurred, Size(5.0, 5.0), 0.0)

        val circle = Rect(blurred.width() / 2, blurred.height() / 2, blurred.width() / 4,blurred.height() / 4)
        val roi = Mat(blurred, circle)

        val hist = Mat()
        Imgproc.calcHist(
            listOf(roi),
            MatOfInt(0),
            Mat(),
            hist,
            MatOfInt(180),
            MatOfFloat(0f, 180f)
        )

        val minMaxLocResult = Core.minMaxLoc(hist)
        val hColor = minMaxLocResult.maxLoc.y

        val thresh = Mat()
        Core.inRange(blurred, Scalar(hColor - 10, 0.0, 0.0), Scalar(hColor + 10, 255.0, 255.0), thresh)

        //find circles
        val circles = Mat()
        Log.e("ERROR", "started")
        Imgproc.HoughCircles(thresh, circles, Imgproc.HOUGH_GRADIENT, 2.0,
            10.0, 100.0, 50.0,
            70, 80)
        Log.e("ERROR", "finished")

        //draw circles
        val size = if (circles.cols() > 17) { 17 } else { circles.cols() }
        for (x in 0 until size) { //16 because there is only 16 balls
            Log.e("ERROR", "$x")
            val c = circles.get(0, x)
            val center = Point(Math.round(c[0]*ratio).toDouble(), Math.round(c[1]*ratio).toDouble())
            val radius = Math.round(c[2]*ratio).toInt()
            Imgproc.circle(tableMat, center, radius, Scalar(255.0, 0.0, 255.0),
                3, 8, 0)
        }
        return tableMat
    }

    private fun table(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val tableMat = inputFrame.rgba()
        val pts = tableManager.getTableCountersFromImage(tableMat)
        val recPoints = MatOfPoint(*pts)
        val scalar = Scalar(255.0, 0.0, 0.0)

        val mask = Mat.zeros(tableMat.size(), CvType.CV_8UC1)
        Imgproc.fillPoly(mask, listOf(recPoints), Scalar(255.0, 255.0, 255.0))

        val croped = Mat(tableMat.size(), CV_8UC1)
        tableMat.copyTo(croped, mask)

        //Imgproc.drawContours(tableMat, listOf(recPoints), 0, scalar, 10)
        return croped
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
