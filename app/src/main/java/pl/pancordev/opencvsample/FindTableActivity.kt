package pl.pancordev.opencvsample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.act_find_table.*
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.*


class FindTableActivity : AppCompatActivity() {

    private val KEY = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_find_table)

        thresholding(null)

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
            thresholding(bitmap)
        }
    }

    private fun findingDominantColor(): Double {
        val tableBitmap = BitmapFactory.decodeResource(applicationContext.resources,
            R.drawable.table)
        val tableMat = Mat()
        Utils.bitmapToMat(tableBitmap, tableMat)

        val resized = Mat()
        resize(tableMat, resized, Size(300.0, 150.0))

        val hsv = Mat()
        cvtColor(resized, hsv, COLOR_BGR2HSV)

        val hist = Mat()
        calcHist(listOf(hsv), MatOfInt(0), Mat(), hist, MatOfInt(180), MatOfFloat(0f, 180f))

        val minMaxLocResult = Core.minMaxLoc(hist)
        return minMaxLocResult.maxLoc.y
    }

    private fun thresholding(bit: Bitmap?) {
        //converting bitmap to Mat
        val tableBitmap = bit ?: BitmapFactory.decodeResource(applicationContext.resources, R.drawable.table)
        val tableMat = Mat()
        Utils.bitmapToMat(tableBitmap, tableMat)

        setOriginalImage(tableMat)

        val resized = Mat()
        resize(tableMat, resized, Size(300.0, 150.0))
        val ratio = tableMat.size().width / resized.size().width

        val hsv = Mat()
        cvtColor(resized, hsv, COLOR_BGR2HSV)
        val blurred = Mat()
        GaussianBlur(hsv, blurred, Size(5.0, 5.0), 0.0)
        val thresh = Mat()
//        adaptiveThreshold(blurred, thresh, 255.0, ADAPTIVE_THRESH_GAUSSIAN_C,
//            THRESH_BINARY, 7, 2.0)
//        threshold(blurred, thresh, 169.0, 255.0, THRESH_BINARY)


        val hist = Mat()
        calcHist(listOf(blurred), MatOfInt(0), Mat(), hist, MatOfInt(180), MatOfFloat(0f, 180f))

        val minMaxLocResult = Core.minMaxLoc(hist)
        val hColor = minMaxLocResult.maxLoc.y

        Core.inRange(blurred, Scalar(hColor - 10, 0.0, 0.0), Scalar(hColor + 10, 255.0, 255.0), thresh)
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

        if (cnts.isNotEmpty()) {
            val rec = boundingRect(cnts[maxIndex])

            //TODO: minAreaRect

            val points = cnts[maxIndex].toArray()
            val rotatedRect = minAreaRect(MatOfPoint2f(*points))

            val rectPoints = Array(4) { Point() }
            rotatedRect.points(rectPoints)
            drawContours(
                resized, listOf(MatOfPoint(*rectPoints)), 0, Scalar(
                    255.0,
                    0.0, 0.0
                ), Core.FILLED
            )

            //rectangle(resized, rec.br(), rec.tl(),Scalar(255.0, 0.0, 0.0), Core.FILLED)

//        drawContours(resized, listOf(cnts[maxIndex]), 0, Scalar(255.0,
//            0.0, 0.0), 3)

            setImageResult(resized)
        }

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
