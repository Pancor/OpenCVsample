package pl.pancordev.opencvsample.tools

import org.opencv.core.Mat
import org.opencv.core.Point


class TableManagerImpl : TableManager {

    private val tablePositionService: TablePositionService

    init {
        tablePositionService = TablePositionServiceImpl()
    }

    override fun getTableCountersFromImage(table: Mat): Array<Point> {
        val convertedTable = tablePositionService.getPreparedImageForCalculations(table)
        val tableColor = tablePositionService.getTableColor(convertedTable)
        return tablePositionService.getTableContours(convertedTable, tableColor)
    }
}