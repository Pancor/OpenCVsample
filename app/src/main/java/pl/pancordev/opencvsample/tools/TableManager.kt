package pl.pancordev.opencvsample.tools

import org.opencv.core.Mat
import org.opencv.core.Point


interface TableManager {

    fun getTableCountersFromImage(table: Mat): Array<Point>
}