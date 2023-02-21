import java.io.InputStream

class PSDParser(private val fileStream: InputStream) {
    private lateinit var cursor: PSDCursor
    private val PSD_HEADER_OFFSET = 0x00
    private val PSD_COLOR_MODE_OFFSET = 26

    init {
        cursor = PSDCursor()
    }

    // TODO: IMPLEMENT
    fun ByteArray.fromPascalString(): String {
        return ""
    }

    // TODO: What if moreThan 8byte
    fun ByteArray.toInt(): Int {
        return this.map { it.toInt() }.foldIndexed(0) { index: Int, acc: Int, value: Int ->
            (value and 0xFF shl (8 * (this.size - 1 - index))) or acc
        }
    }

    suspend fun parseHeader(): PSDFileHeader {
        val signature = cursor.readByte(fileStream, 4)
        if (String(signature) != "8BPS")
            throw Error("Invalid PSD File")

        val version = cursor.readByte(fileStream, 2)
        if (version[1].toInt() != 1)
            throw Error("Invalid PSD Version")

        // reserved
        cursor.readByte(fileStream, 6)

        // channel
        cursor.readByte(fileStream, 2)

        // height
        val height = cursor.readByte(fileStream, 4).toInt()
//        println("Height: ${height}")

        // width
        val width = cursor.readByte(fileStream, 4).toInt()
//        println("Width: ${width}")

        // depth
        val depth = cursor.readByte(fileStream, 2)

        // color mode
        val colorMode = cursor.readByte(fileStream, 2).toInt()
        return PSDFileHeader(String(signature), height, width, colorMode)
    }

    suspend fun parseColorModeData(): PSDColorMode {
        val colorLength = cursor.readByte(fileStream, 4)
        val colorMode = cursor.readByte(fileStream, colorLength.toInt())
        return PSDColorMode(colorMode)
    }

    suspend fun parseImageResource() {
        val imageResourceLength = cursor.readByte(fileStream, 4)
        while (true) {
            val block = parseImageResourceBlock() ?: break
            println(block.resourceId)
        }
    }

    private suspend fun parseImageResourceBlock(): PSDImageResource?  {
        cursor.markCursor(fileStream)
        val signature = cursor.readByte(fileStream, 4)
        if (String(signature) != "8BIM")  {
            cursor.resetCursor(fileStream)
            return null
        }

        val identifier = cursor.readByte(fileStream, 2).toInt()

        val resourceNameSize = cursor.readByte(fileStream, 1).toInt()
        // not empty resource name
        // pascal string format -> https://stackoverflow.com/questions/28519732/what-is-a-pascal-style-string
        val pascalStrings = if (resourceNameSize > 0) {
            cursor.readByte(fileStream, resourceNameSize).fromPascalString()
        }  else {
            cursor.readByte(fileStream, 1)
            null
        }

        val resourceLength = cursor.readByte(fileStream, 4).toInt().let {
            if (it % 2 != 0) it + 1 else it
        }

        val resourceData = cursor.readByte(fileStream, resourceLength)

        return PSDImageResource(identifier, pascalStrings ?: "", resourceData)
    }

    fun parseResourceMaskAndLayer() {
        val maskAndLayerLength = cursor.readByte(fileStream, 4).toInt()
        println("maskAndLayerLength: $maskAndLayerLength")
        // Layer Info Parsing
        val layerInfoLength = cursor.readByte(fileStream, 4).toInt()
        println("layerInfoLength: $layerInfoLength")
        val layerCount = cursor.readByte(fileStream, 2).toInt()
        println("layer: $layerCount")
        // Layer Record Parsing

        // Global layer mask info
    }

}
