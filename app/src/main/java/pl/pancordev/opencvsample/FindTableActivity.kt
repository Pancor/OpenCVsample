package pl.pancordev.opencvsample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.act_find_table.*
import kotlinx.android.synthetic.main.act_find_table.imageView
import kotlinx.android.synthetic.main.act_load_image.*
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.*


class FindTableActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_find_table)

        thresholding()
    }

    //TODO: not finished
    private fun findingDominantColor() {
        val tableBitmap = BitmapFactory.decodeResource(applicationContext.resources,
            R.drawable.table)
        val tableMat = Mat()
        Utils.bitmapToMat(tableBitmap, tableMat)

        val resized = Mat()
        resize(tableMat, resized, Size(300.0, 150.0))

        val hsv = Mat()
        cvtColor(resized, hsv, COLOR_BGR2HSV)

        val hist = Mat()

        calcHist(listOf(hsv), MatOfInt(0), Mat(), hist, MatOfInt(255), MatOfFloat(0f, 255f))
    }

    private fun thresholding() {
        //converting bitmap to Mat
        val tableBitmap = BitmapFactory.decodeResource(applicationContext.resources,
            R.drawable.table)
        val tableMat = Mat()
        Utils.bitmapToMat(tableBitmap, tableMat)

        setOriginalImage(tableMat)

        val resized = Mat()
        resize(tableMat, resized, Size(300.0, 150.0))
        val ratio = tableMat.size().width / resized.size().width

        val gray = Mat()
        cvtColor(resized, gray, COLOR_BGR2GRAY)
        val blurred = Mat()
        GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)
        val thresh = Mat()
//        adaptiveThreshold(blurred, thresh, 255.0, ADAPTIVE_THRESH_GAUSSIAN_C,
//            THRESH_BINARY, 7, 2.0)
        threshold(blurred, thresh, 60.0, 255.0, THRESH_BINARY)

        setComparinImage(thresh)

        val cnts: List<MatOfPoint> = mutableListOf()
        val hierarchy = Mat()
        findContours(thresh, cnts, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE)



//        for (c in cnts) {
//            drawContours(resized, listOf(c), 0, Scalar(0.0,
//                255.0, 0.0), 3)
//        }

        var max = 0.0
        var maxIndex = 0
        for (i in cnts.indices) {
            val area = contourArea(cnts[i])
            if (area > max){
                max = area
                maxIndex = i
            }
        }

        val rec = boundingRect(cnts[maxIndex])

        //TODO: minAreaRect

        val points = cnts[maxIndex].toArray()
        val rotatedRect = minAreaRect(MatOfPoint2f(*points))

        val rectPoints = Array(4) { Point() }
        rotatedRect.points(rectPoints)
        drawContours(resized, listOf(MatOfPoint(*rectPoints)), 0, Scalar(255.0,
            0.0, 0.0), Core.FILLED)

        //rectangle(resized, rec.br(), rec.tl(),Scalar(255.0, 0.0, 0.0), Core.FILLED)

//        drawContours(resized, listOf(cnts[maxIndex]), 0, Scalar(255.0,
//            0.0, 0.0), 3)

        setImageResult(resized)

//        val points = MatOfPoint2f(tableMat)
//        val peri = Imgproc.arcLength(points, true)
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
