package pl.pancordev.opencvsample.tools

import org.opencv.core.*
import org.opencv.imgproc.Imgproc


class TablePositionServiceImpl : TablePositionService {

    private var ratio = 0.0

    override fun getPreparedImageForCalculations(table: Mat): Mat {
        val resizedTable = getResizedTable(table)
        return convertToHSVWithGaussianBlur(resizedTable)
    }

    private fun getResizedTable(table: Mat): Mat {
        val resizedTable = Mat()
        val newSize = Size(table.size().width * 0.1, table.size().height * 0.1)
        Imgproc.resize(table, resizedTable, newSize)
        ratio = table.size().width / resizedTable.size().width //TODO: store resizing ratio for future calculations of drawing proper scale of contours
        return resizedTable
    }

    private fun convertToHSVWithGaussianBlur(table: Mat): Mat {
        val hsvTable = Mat()
        Imgproc.cvtColor(table, hsvTable, Imgproc.COLOR_BGR2HSV)
        val blurredTable = Mat()
        Imgproc.GaussianBlur(hsvTable, blurredTable, Size(5.0, 5.0), 0.0)
        return blurredTable
    }

    override fun getTableColor(table: Mat): Double {
        val circle = Rect(table.width() / 2, table.height() / 2, table.width() / 4,table.height() / 4)
        val roi = Mat(table, circle)

        val hist = Mat()
        Imgproc.calcHist(listOf(roi), MatOfInt(0), Mat(), hist, MatOfInt(180), MatOfFloat(0f, 180f))

        val minMaxLocResult = Core.minMaxLoc(hist)
        return minMaxLocResult.maxLoc.y
    }

    override fun getTableContours(table: Mat, tableColor: Double): Array<Point> {
        val thresh = Mat()
        Core.inRange(table, Scalar(tableColor - 10, 0.0, 0.0), Scalar(tableColor + 10, 255.0, 255.0), thresh)

        val allContours: List<MatOfPoint> = mutableListOf()
        val hierarchy = Mat()
        Imgproc.findContours(thresh, allContours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var max = 0.0
        var maxIndex = 0
        for (i in allContours.indices) {
            val area = Imgproc.contourArea(allContours[i])
            if (area > max){
                max = area
                maxIndex = i
            }
        }
        val rectPoints = MatOfPoint2f()
        if (allContours.isNotEmpty()) {
            val points = allContours[maxIndex].toArray()

            val epsilon = 0.03 * Imgproc.arcLength(MatOfPoint2f(*points), true)
            Imgproc.approxPolyDP(MatOfPoint2f(*points), rectPoints, epsilon, true)

            val pts = rectPoints.toArray()
            pts.forEach { point ->
                run {
                    point.x *= ratio
                    point.y *= ratio
                }
            }
            return pts
        }
        return emptyArray()
    }
}