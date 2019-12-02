package pl.pancordev.opencvsample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.act_find_table.*
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc.*
import pl.pancordev.opencvsample.tools.TableManager
import pl.pancordev.opencvsample.tools.TableManagerImpl

class FindTableActivity : AppCompatActivity() {

    private val KEY = 100
    private lateinit var tableManager: TableManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_find_table)
        tableManager = TableManagerImpl()

        findTable(null)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/"
        startActivityForResult(intent, KEY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == KEY && resultCode == Activity.RESULT_OK && data != null) {
            val stream = applicationContext.contentResolver.openInputStream(data.data)
            val bitmap = BitmapFactory.decodeStream(stream)
            findTable(bitmap)
        }
    }

    private fun findTable(bit: Bitmap?) {
        val tableBitmap = bit ?: BitmapFactory.decodeResource(applicationContext.resources, R.drawable.table)
        val tableMat = Mat()
        Utils.bitmapToMat(tableBitmap, tableMat)

        setOriginalImage(tableMat)

        val pts = tableManager.getTableCountersFromImage(tableMat)

        val recPoints = MatOfPoint(*pts)
        drawContours(tableMat, listOf(recPoints), 0, Scalar(255.0, 0.0, 0.0), 15)
        setImageResult(tableMat)
    }

    private fun setImageResult(mat: Mat) {
        val bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bmp)
        Glide.with(this)
            .load(bmp)
            .into(imageView)
    }

    private fun setOriginalImage(mat: Mat) {
        val bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bmp)
        Glide.with(this)
            .load(bmp)
            .into(imageView3)
    }

//    private fun threshing(bit: Bitmap?) {
//        val tableBitmap = bit ?: BitmapFactory.decodeResource(applicationContext.resources, R.drawable.table)
//        val tableMat = Mat()
//        Utils.bitmapToMat(tableBitmap, tableMat)
//
//        setOriginalImage(tableMat)
//
//        val resized = Mat()
//        resize(tableMat, resized, Size(tableMat.size().width * 0.1, tableMat.size().height * 0.1))
//        val ratio = tableMat.size().width / resized.size().width
//
//        val hsv = Mat()
//        cvtColor(resized, hsv, COLOR_BGR2HSV)
//        val blurred = Mat()
//        GaussianBlur(hsv, blurred, Size(5.0, 5.0), 0.0)
//
//        val circle = Rect(blurred.width() / 2, blurred.height() / 2, blurred.width() / 4,blurred.height() / 4)
//        val roi = Mat(blurred, circle)
//
//        val hist = Mat()
//        calcHist(listOf(roi), MatOfInt(0), Mat(), hist, MatOfInt(180), MatOfFloat(0f, 180f))
//
//        val minMaxLocResult = Core.minMaxLoc(hist)
//        val hColor = minMaxLocResult.maxLoc.y
//
//        val thresh = Mat()
//        Core.inRange(blurred, Scalar(hColor - 10, 0.0, 0.0), Scalar(hColor + 10, 255.0, 255.0), thresh)
//        setComparinImage(thresh)
//
//        val cnts: List<MatOfPoint> = mutableListOf()
//        val hierarchy = Mat()
//        findContours(thresh, cnts, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE)
//
//        var max = 0.0
//        var maxIndex = 0
//        for (i in cnts.indices) {
//            val area = contourArea(cnts[i])
//            if (area > max){
//                max = area
//                maxIndex = i
//            }
//        }
//
//        if (cnts.isNotEmpty()) {
//            val points = cnts[maxIndex].toArray()
//
//            val epsilon = 0.03 * arcLength(MatOfPoint2f(*points), true)
//            val rectPoints = MatOfPoint2f()
//            approxPolyDP(MatOfPoint2f(*points), rectPoints, epsilon, true)
//
//            val pts = rectPoints.toArray()
//            pts.forEach { point ->
//                run {
//                    point.x *= ratio
//                    point.y *= ratio
//                }
//            }
//
//            val recPoints = MatOfPoint(*pts)
//            drawContours(tableMat, listOf(recPoints), 0, Scalar(255.0,
//                0.0, 0.0), 15)
//            setImageResult(tableMat)
//        }
//    }
//
//    private fun setComparinImage(mat: Mat) {
//        val bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(mat, bmp)
//        Glide.with(this)
//            .load(bmp)
//            .into(imageView2)
//    }
}
