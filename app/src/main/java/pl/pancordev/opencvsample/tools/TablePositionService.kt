package pl.pancordev.opencvsample.tools

import org.opencv.core.Mat
import org.opencv.core.Point

interface TablePositionService {

    fun getPreparedImageForCalculations(table: Mat): Mat

    fun getTableColor(table: Mat): Double

    fun getTableContours(table: Mat, tableColor: Double): Array<Point>
}