package pl.pancordev.opencvsample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.act_load_image.*
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class LoadImageActivity : AppCompatActivity() {

    private val KEY = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_load_image)

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
            findBalls(bitmap)
        }
    }

    private fun findBalls(img: Bitmap) {
        val mat = getImage(img)

        //convert to gray
        val matGrey = Mat()
        Imgproc.cvtColor(mat, matGrey, Imgproc.COLOR_BGR2GRAY)

        //reduce noise
        Imgproc.blur(matGrey, matGrey, Size(9.0, 9.0))

        //find circles
        val circles = Mat()
/*        Imgproc.HoughCircles(matGrey, circles, Imgproc.HOUGH_GRADIENT, 1.0,
             matGrey.rows()/16.toDouble(), 100.0, 30.0,
            1, 30)*/
        Log.e("ERROR", "started")
        Imgproc.HoughCircles(matGrey, circles, Imgproc.HOUGH_GRADIENT, 2.0,
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
            Imgproc.circle(mat, center, radius, Scalar(255.0, 0.0, 255.0),
                3, 8, 0)
        }
        setImageResult(mat)
    }

    private fun getImage(img: Bitmap) : Mat {
        val result = Mat()
        Utils.bitmapToMat(img, result)
        return result
    }

    private fun setImageResult(mat: Mat) {
        val bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bmp)
        Glide.with(this)
            .load(bmp)
            .into(imageView)
    }
}
