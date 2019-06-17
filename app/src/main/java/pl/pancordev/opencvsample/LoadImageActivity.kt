package pl.pancordev.opencvsample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.act_load_image.*
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class LoadImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_load_image)

        val mat = getImage()

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
        Imgproc.HoughCircles(matGrey, circles, Imgproc.HOUGH_GRADIENT, 1.0,
            10.0, 100.0, 20.0,
            30, 50)
        Log.e("ERROR", "finished")

        //draw circles
        for (x in 0 until circles.cols()) {
            Log.e("ERROR", "$x")
            val c = circles.get(0, x)
            val center = Point(Math.round(c[0]).toDouble(), Math.round(c[1]).toDouble())
            val radius = Math.round(c[2]).toInt()
            Imgproc.circle(mat, center, radius, Scalar(255.0, 0.0, 255.0),
                3, 8, 0)
        }

        setImageResult(mat)
    }

    private fun getImage() : Mat {
        val result = Mat()
        val img = BitmapFactory.decodeResource(resources, R.drawable.table)
            .copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(img, result)
        return result
    }

    private fun setImageResult(mat: Mat) {
        val bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bmp)
        imageView.setImageBitmap(bmp)
    }
}
