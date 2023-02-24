import java.io.InputStream

// TODO Make parser parallelized
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

    fun ByteArray.toInt(): Int {
        return this.map { it.toInt() }.foldIndexed(0) { index: Int, acc: Int, value: Int ->
            (value and 0xFF shl (8 * (this.size - 1 - index))) or acc
        }
    }

    suspend fun parseHeader(): PSDFileHeader {
        cursor.apply {
            val signature = readByte(fileStream, 4)
            if (String(signature) != "8BPS")
                throw Error("Invalid PSD File")

            val version = readByte(fileStream, 2)
            if (version[1].toInt() != 1)
                throw Error("Invalid PSD Version")

            // reserved
            readByte(fileStream, 6)

            // channel
            readByte(fileStream, 2)

            // height
            val height = readByte(fileStream, 4).toInt()
//        println("Height: ${height}")

            // width
            val width = readByte(fileStream, 4).toInt()
//        println("Width: ${width}")

            // depth
            val depth = readByte(fileStream, 2)

            // color mode
            val colorMode = readByte(fileStream, 2).toInt()
            return PSDFileHeader(String(signature), height, width, colorMode)
        }
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

    suspend fun parseResourceMaskAndLayer() {
        val maskAndLayerLength = cursor.readByte(fileStream, 4).toInt()

        // Layer Info Parsing
        val layerInfoLength = cursor.readByte(fileStream, 4).toInt()

        val layerCount = cursor.readByte(fileStream, 2).toInt()

        // Layer Record Parsing
        for (i in 1..layerCount) {
            parseLayerBlock()
        }

        // Global layer mask info
    }

    private suspend fun parseLayerBlock() {
        cursor.apply {
            val top = readByte(fileStream, 4).toInt()
            val left = readByte(fileStream, 4).toInt()
            val bottom = readByte(fileStream, 4).toInt()
            val right = readByte(fileStream, 4).toInt()

            val channelsCount = readByte(fileStream, 2).toInt()
            for (channel in 1 .. channelsCount) {
                readByte(fileStream, 2)
                readByte(fileStream, 4)
            }

            // Blend mode signature
            if (String(readByte(fileStream, 4)) != "8BIM") {
                throw Error("Blend mode invalid")
            }

            // Blend mode key
            val blendModeKey = String(readByte(fileStream, 4))

            // opacity
            val opacity = readByte(fileStream, 1).toInt()

            // clipping
            val clipping = readByte(fileStream, 1).toInt()

            // flag
            val layerFlag = readByte(fileStream, 1)

            // filler
            readByte(fileStream, 1)

            // extra data field
            readByte(fileStream, 4)

            // layer mask, layer blending
            parseLayerMask()
            parseLayerBlending()

            // layer name (Pascal String)


        }
    }

    private fun parsePascalName() {
        cursor.apply {
            val layerNameSize = readByte(fileStream, 1).toInt()
            val pascalStrings = if (layerNameSize > 0) {
                readByte(fileStream, layerNameSize).fromPascalString()
            } else {
                readByte(fileStream, 1)
                null
            }
        }
    }

    private fun parseLayerMask() {
        cursor.apply {
            val layerMaskSize = readByte(fileStream, 4).toInt()
            if (layerMaskSize == 0) return

            val top = readByte(fileStream, 4)
            val left = readByte(fileStream,4)
            val right = readByte(fileStream, 4)
            val bottom = readByte(fileStream, 4)

            val defaultColor = readByte(fileStream, 1)

            //bit 0 = position relative to layer
            //bit 1 = layer mask disabled
            //bit 2 = invert layer mask when blending (Obsolete)
            //bit 3 = indicates that the user mask actually came from rendering other data
            //bit 4 = indicates that the user and/or vector masks have parameters applied to them
            val flags = readByte(fileStream, 1).toInt()

            // mask parameters
            if (flags == 4) {
                //bit 0 = user mask density, 1 byte
                //bit 1 = user mask feather, 8 byte, double
                //bit 2 = vector mask density, 1 byte
                //bit 3 = vector mask feather, 8 bytes, double
                val maskParameter = readByte(fileStream, 1).toInt()
                when (maskParameter) {
                    0 -> {
                        readByte(fileStream, 1)
                    }
                    1 -> {
                        readByte(fileStream, 8)
                    }
                    2 -> {
                        readByte(fileStream, 1)
                    }
                    3 -> {
                        readByte(fileStream, 8)
                    }
                }
            }

            // padding
            if (layerMaskSize == 20) {
                cursor.readByte(fileStream, 2)
                return
            }


        }
    }

    fun parseLayerBlending() {
        cursor.apply {

        }
    }

}
