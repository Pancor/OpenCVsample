package pl.pancordev.opencvsample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.act_load_image.imageView
import kotlinx.android.synthetic.main.act_load_image.imageView2
import kotlinx.android.synthetic.main.act_load_image.imageView3
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.CvType.*
import org.opencv.imgproc.Imgproc

class LoadImageActivity : AppCompatActivity() {

    private val KEY = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_load_image)

        //threshing(null)
        findBalls_BACK_UP(null)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/"
        startActivityForResult(intent, KEY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == KEY && resultCode == Activity.RESULT_OK) {
            if (data == null) return
            val stream = applicationContext.contentResolver.openInputStream(data.data)
            val bitmap = BitmapFactory.decodeStream(stream)
            threshing(bitmap)
        }
    }

    private fun findBalls(bit: Mat, tableCnts: Array<Point>): Mat {
        val tableMat = bit

        setOriginalImage(tableMat)

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
        setComparinImage(thresh)

        tableCnts.forEach { point ->
            run {
                point.x /= ratio
                point.y /= ratio
            }
        }

        val mask = Mat(thresh.height(), thresh.width(), CV_8U)
        Imgproc.drawContours(mask, listOf(MatOfPoint(*tableCnts)), -1, Scalar(255.0, 255.0, 255.0), Core.FILLED)
        val croped = Mat(thresh.height(), thresh.width(), CV_8UC3)
        croped.setTo(Scalar(0.0, 0.0, 0.0))
        thresh.copyTo(croped, mask)

        val inversed = Mat()
        Core.bitwise_not(croped, inversed)

        val cnts: List<MatOfPoint> = mutableListOf()
        val hierarchy = Mat()
        Imgproc.findContours(inversed, cnts, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        cnts.forEach { cnt ->
            val points = cnt.toArray()
            val epsilon = 0.03 * Imgproc.arcLength(MatOfPoint2f(*points), true)
            val circlePoints = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*points), circlePoints, epsilon, true)

            val cicPoints = MatOfPoint(*circlePoints.toArray())
            Imgproc.drawContours(
                croped, listOf(cicPoints), 0, Scalar(
                    255.0,
                    0.0, 0.0
                ), 15
            )
        }

//        val bilateraled = Mat()
//        Imgproc.cvtColor(croped, croped, Imgproc.COLOR_BGRA2BGR)
//        Imgproc.bilateralFilter(croped, bilateraled, 5, 175.0, 175.0)
//
//        val canny = Mat()
//        Imgproc.Canny(bilateraled, canny, 75.0, 200.0)

        setImageResult(croped)
        return tableMat
    }

    private fun threshing(bit: Bitmap?) {
        val tableBitmap = bit ?: BitmapFactory.decodeResource(applicationContext.resources, R.drawable.table)
        val tableMat = Mat()
        Utils.bitmapToMat(tableBitmap, tableMat)


        val resized = Mat()
        Imgproc.resize(
            tableMat,
            resized,
            Size(tableMat.size().width * 0.1, tableMat.size().height * 0.1)
        )
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
            if (area > max){
                max = area
                maxIndex = i
            }
        }

        if (cnts.isNotEmpty()) {
            val points = cnts[maxIndex].toArray()

            val epsilon = 0.03 * Imgproc.arcLength(MatOfPoint2f(*points), true)
            val rectPoints = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*points), rectPoints, epsilon, true)

            val pts = rectPoints.toArray()
            pts.forEach { point ->
                run {
                    point.x *= ratio
                    point.y *= ratio
                }
            }

            val recPoints = MatOfPoint(*pts)
//            Imgproc.drawContours(
//                tableMat, listOf(recPoints), 0, Scalar(
//                    255.0,
//                    0.0, 0.0
//                ), 15
//            )
            findBalls(tableMat, pts)
        }
    }

    private fun findBalls_BACK_UP(bit: Bitmap?): Mat {

        val tableBitmap = bit ?: BitmapFactory.decodeResource(applicationContext.resources, R.drawable.table)
        val tableMat = Mat()
        Utils.bitmapToMat(tableBitmap, tableMat)
        setOriginalImage(tableMat)

        val resized = Mat()
        Imgproc.resize(tableMat, resized, Size(tableMat.size().width * 0.1, tableMat.size().height * 0.1))
        val ratio = tableMat.size().width / resized.size().width

        val gray = Mat()
        Imgproc.cvtColor(tableMat, gray, Imgproc.COLOR_BGR2GRAY)
        val blurred = Mat()
        Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)
        //     Imgproc.blur(gray, gray, Size(5.0, 5.0))

//
//        val hsv = Mat()
//        Imgproc.cvtColor(resized, hsv, Imgproc.COLOR_BGR2HSV)
//        val blurred = Mat()
//        Imgproc.GaussianBlur(hsv, blurred, Size(5.0, 5.0), 0.0)
//
//        val circle = Rect(blurred.width() / 2, blurred.height() / 2, blurred.width() / 4,blurred.height() / 4)
//        val roi = Mat(blurred, circle)
//
//        val hist = Mat()
//        Imgproc.calcHist(
//            listOf(roi),
//            MatOfInt(0),
//            Mat(),
//            hist,
//            MatOfInt(180),
//            MatOfFloat(0f, 180f)
//        )
//
//        val minMaxLocResult = Core.minMaxLoc(hist)
//        val hColor = minMaxLocResult.maxLoc.y
//        val thresh = Mat()
//        Core.inRange(blurred, Scalar(hColor - 10, 0.0, 0.0), Scalar(hColor + 10, 255.0, 255.0), thresh)
        setComparinImage(gray)

        //find circles
        val circles = Mat()
        Log.e("ERROR", "started")
        Imgproc.HoughCircles(blurred, circles, Imgproc.HOUGH_GRADIENT, 1.0,
            45.0, 250.0, 10.0, 25, 40)
        Log.e("ERROR", "finished")

        Log.e("TAAAAAAAK", "size: ${circles.cols()}")
        //draw circles
        val size = if (circles.cols() > 15) { 15 } else { circles.cols() }// it sjhytd be 17
        for (x in 0 until size) { //16 because there is only 16 balls
            val c = circles.get(0, x)
            val center = Point(Math.round(c[0]).toDouble(), Math.round(c[1]).toDouble())
            val radius = Math.round(c[2]).toInt()
            Imgproc.circle(tableMat, center, radius, Scalar(0.0, 255.0, 0.0),
                10, 8, 0)
        }

        setImageResult(tableMat)
        return tableMat
    }

    private fun setImageResult(mat: Mat) {
        val bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bmp)
        Glide.with(this)
            .load(bmp)
            .into(imageView)
    }

    private fun setComparinImage(mat: Mat) {
        val bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bmp)
        Glide.with(this)
            .load(bmp)
            .into(imageView2)
    }

    private fun setOriginalImage(mat: Mat) {
        val bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bmp)
        Glide.with(this)
            .load(bmp)
            .into(imageView3)
    }
}
