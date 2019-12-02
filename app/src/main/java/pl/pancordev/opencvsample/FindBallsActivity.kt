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
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.drawContours
import org.opencv.imgproc.Imgproc.minAreaRect
import java.util.*

class FindBallsActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var managerCallback: BaseLoaderCallback

    private var hColors : Deque<Double> = ArrayDeque(20)
    private var storedTablePoints: Array<Point> = Array(4) { Point() }

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
        hColors.add(hColor)
        val averageHColor = getAverage()

        val thresh = Mat()
        Core.inRange(blurred, Scalar(averageHColor - 10, 0.0, 0.0), Scalar(averageHColor + 10, 255.0, 255.0), thresh)

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

    fun table(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
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
        hColors.add(hColor)
        val averageHColor = getAverage()
        //Log.e("TAGGGGGGGGGGG", averageHColor.toString())

        val thresh = Mat()
        Core.inRange(blurred, Scalar(hColor - 10, 0.0, 0.0), Scalar(hColor + 10, 255.0, 255.0), thresh)

        val cnts: List<MatOfPoint> = mutableListOf()
        val hierarchy = Mat()

        Imgproc.findContours(
            thresh,
            cnts,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        var max = 0.0
        var maxIndex = 0
        for (i in cnts.indices) {
            val area = Imgproc.contourArea(cnts[i])
            if (area > max) {
                max = area
                maxIndex = i
            }
        }

        if (cnts.isNotEmpty()) {
            val points = cnts[maxIndex].toArray()
            Log.e("COOOOOOOOO", points.size.toString())
            //if (points.size == 4) {
                storedTablePoints = points
          //  }

            val epsilon = 0.1 * Imgproc.arcLength(MatOfPoint2f(*storedTablePoints), true)
            val rectPoints = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*storedTablePoints), rectPoints, epsilon, true)

            val pts = rectPoints.toArray()
            pts.forEach { point ->
                run {
                    point.x *= ratio
                    point.y *= ratio
                }
            }

            val recPoints = MatOfPoint(*pts)
            Imgproc.drawContours(
                tableMat, listOf(recPoints), 0, Scalar(
                    255.0,
                    0.0, 0.0
                ), 10
            )
            return tableMat
        }
        return inputFrame.rgba()
    }

    private fun getAverage(): Double {
        return if (hColors.size > 0) {
            var sum = 0.0
            for (hColor in hColors) {
                sum += hColor
            }
            sum / hColors.size
        } else {
            0.0
        }
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
