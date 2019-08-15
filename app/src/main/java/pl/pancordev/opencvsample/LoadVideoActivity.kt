package pl.pancordev.opencvsample

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.act_load_video.*
import org.opencv.android.*
import org.opencv.core.Mat

class LoadVideoActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var managerCallback: BaseLoaderCallback
    private lateinit var retriever: MediaMetadataRetriever
    private var i = 0L
    private val frTime = 67L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_load_video)

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

        val path = Uri.parse("android.resource://$packageName/raw/game")
        retriever = MediaMetadataRetriever()
        retriever.setDataSource(this, path)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            .toLong()
        //15 frames per second
        val frameTime = 67.toLong()

//        for (i in 0..duration step frameTime) {
//            val frame = retriever.getFrameAtTime(i)
//            onCameraFrame(frame)
//        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {

    }

    override fun onCameraViewStopped() {

    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        i += frTime
        val bitmap = Bitmap.createScaledBitmap(
            retriever.getFrameAtTime(i), 800, 600, false
        )
        Log.e("TAAAG", i.toString())
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        return mat
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
