import java.io.InputStream

// TODO Make parser parallelized
class PSDParser(private val fileStream: InputStream) {
    private lateinit var cursor: PSDCursor
    private val PSD_HEADER_OFFSET = 0x00
    private val PSD_COLOR_MODE_OFFSET = 26

    init {
        cursor = PSDCursor()
    }

    fun ByteArray.fromPascalString(): String {
        return this.map { it.toInt().toChar() }.joinToString(separator = "")
    }

    fun ByteArray.toInt(): Int {
        return this.map { it.toInt() }.foldIndexed(0) { index: Int, acc: Int, value: Int ->
            (value and 0xFF shl (8 * (this.size - 1 - index))) or acc
        }
    }

    suspend fun parseHeader(): PSDFileHeader {
        cursor.apply {
            val signature = readByte(fileStream, 4)
            if (String(signature) != "8BPS") {
                throw Error("Invalid PSD File")
            }

            val version = readByte(fileStream, 2)
            if (version[1].toInt() != 1) {
                throw Error("Invalid PSD Version")
            }

            // reserved
            readByte(fileStream, 6)

            // channel
            val channel = readByte(fileStream, 2).toInt()

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

    private suspend fun parseImageResourceBlock(): PSDImageResource? {
        cursor.markCursor(fileStream)
        val signature = cursor.readByte(fileStream, 4)
        if (String(signature) != "8BIM") {
            cursor.resetCursor(fileStream)
            return null
        }

        val identifier = cursor.readByte(fileStream, 2).toInt()

        val resourceNameSize = cursor.readByte(fileStream, 1).toInt()
        // not empty resource name
        // pascal string format -> https://stackoverflow.com/questions/28519732/what-is-a-pascal-style-string
        val pascalStrings = if (resourceNameSize > 0) {
            cursor.readByte(fileStream, resourceNameSize).fromPascalString()
        } else {
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
        val layerBlockList = (1..layerCount).map {
            parseLayerBlock()
        }

        // Channel Image data
        for (i in 1..layerCount) {
            for (j in 1 .. layerBlockList[i].channelCount) {
                val channelImageCompression = cursor.readByte(fileStream, 2).toInt()
                parseChannelImageData(channelImageCompression, layerBlockList[i])
            }
        }
    }

    private fun parseChannelImageData(compressionCode: Int, layerBlock: PSDLayerBlock) {
        when (compressionCode) {
            0 -> {
                var size = (layerBlock.bottom - layerBlock.top) * (layerBlock.right - layerBlock.left)
                if (size % 2 == 1)
                    size += 1
                cursor.readByte(fileStream, size)
            }
            1 -> {
                var start = 0
                do {
                    cursor.markCursor(fileStream)
                    val read = cursor.readByte(fileStream, 2).toInt() // read As size * channelCount(usually 4, because of rgba)
                    start += 2
                } while (read != 1 && read != 0 && read != 2 && read != 3)
                cursor.resetCursor(fileStream)
                print(start)
            }
            2 -> {
            }
            3 -> {
            }
        }
    }

    private suspend fun parseLayerBlock(): PSDLayerBlock {
        cursor.apply {
            val top = readByte(fileStream, 4).toInt()
            val left = readByte(fileStream, 4).toInt()
            val bottom = readByte(fileStream, 4).toInt()
            val right = readByte(fileStream, 4).toInt()

            val channelsCount = readByte(fileStream, 2).toInt()
            for (channel in 1..channelsCount) {
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
            val layerFlag = readByte(fileStream, 1).toInt()

            // filler
            readByte(fileStream, 1)

            // extra data field
            readByte(fileStream, 4)

            // layer mask, layer blending
            parseLayerMask()
            parseLayerBlending()

            // layer name (Pascal String)
            val layerName = parsePascalName(paddingBy = 4)
            print(layerName)

            // additionalLayerInfo

            while (parseAdditionalLayer()) {
                println('?')
            }

            return PSDLayerBlock(top, left, bottom, right, channelsCount, opacity, false, layerFlag)
        }
    }

    fun parseAdditionalLayer(): Boolean {
        cursor.apply {
            markCursor(fileStream)
            val signature = String(readByte(fileStream, 4))
            if (signature != "8BIM" && signature != "8B64") {
                resetCursor(fileStream)
                return false
            }
            val additionalLayerKey = String(readByte(fileStream, 4))
            val keyLength = readByte(fileStream, 4).toInt()

            if (keyLength > 0) {
                readByte(fileStream, keyLength)
                return true
            }

            when (additionalLayerKey) {
                "luni" -> {
                    parseUnicodeString()
                }
                "lrFX" -> {
                }
                "tySh" -> {
                }
                "lyid" -> {
                }
                "lfx2" -> {
                }
                "Patt", "Pat2", "Pat3" -> {
                }
                "Anno" -> {
                }
                "clbl" -> {
                }
                "infx" -> {
                }
                "knko" -> {
                }
                "lspf" -> {
                }
                "lclr" -> {
                }
                "fxrp" -> {
                }
                "grdm" -> {
                }
                "lsct" -> {
                }
                "brst" -> {
                }
                "SoCo" -> {
                }
                "PtFl" -> {
                }
                "GdFl" -> {
                }
                "vmsk", "vsms" -> {
                }
                "TySh" -> {
                }
                "ffxi" -> {
                }
                "lnsr" -> {
                }
                "shpa" -> {
                }
                "shmd" -> {
                }
                "lyvr" -> {
                }
                "tsly" -> {
                }
                "lmgm" -> {
                }
                "vmgm" -> {
                }
                "brit" -> {
                }
                "mixr" -> {
                }
                "clrL" -> {
                }
                "plLd" -> {
                }
                "lnkD", "lnk2", "lnk3" -> {
                }
                "phfl" -> {
                }
                "blwh" -> {
                }
                "CgEd" -> {
                }
                "Txt2" -> {
                }
                "vibA" -> {
                }
                "pths" -> {
                }
                "anFX" -> {
                }
                "FMsk" -> {
                }
                "SoLd" -> {
                }
                "vstk" -> {
                }
                "vscg" -> {
                }
                "sn2P" -> {
                }
                "vogk" -> {
                }
                "PxSc" -> {
                }
                "cinf" -> {
                }
                "PxSD" -> {
                }
                "artb", "artd", "abdd" -> {
                }
                "SoLE" -> {
                }
                "Mtrn", "Mt16", "Mt32" -> {
                }
                "LMsk" -> {
                }
                "expA" -> {
                }
                "FXid", "FEid" -> {
                }
            }
        }
        return true
    }

    private fun parseUnicodeString(): String? {
        cursor.apply {
        }
        return ""
    }

    private fun parsePascalName(paddingBy: Int? = null): String? {
        cursor.apply {
            val layerNameSize = readByte(fileStream, 1).toInt()
            val pascalStrings = if (layerNameSize > 0) {
                readByte(fileStream, layerNameSize).fromPascalString()
            } else {
                readByte(fileStream, 1)
                null
            }
            if (paddingBy != null) {
                val padding = (1 + layerNameSize) % paddingBy
                if (padding > 0) {
                    readByte(fileStream, paddingBy - padding)
                }
            }
            return pascalStrings
        }
    }

    private fun parseLayerMask() {
        cursor.apply {
            val layerMaskSize = readByte(fileStream, 4).toInt()
            if (layerMaskSize == 0) return

            val top = readByte(fileStream, 4)
            val left = readByte(fileStream, 4)
            val right = readByte(fileStream, 4)
            val bottom = readByte(fileStream, 4)

            val defaultColor = readByte(fileStream, 1)

            // bit 0 = position relative to layer
            // bit 1 = layer mask disabled
            // bit 2 = invert layer mask when blending (Obsolete)
            // bit 3 = indicates that the user mask actually came from rendering other data
            // bit 4 = indicates that the user and/or vector masks have parameters applied to them
            val flags = readByte(fileStream, 1).toInt()

            // mask parameters
            if (flags == 4) {
                // bit 0 = user mask density, 1 byte
                // bit 1 = user mask feather, 8 byte, double
                // bit 2 = vector mask density, 1 byte
                // bit 3 = vector mask feather, 8 bytes, double
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
            val blendingLength = readByte(fileStream, 4).toInt()

            val greyBlendSource = readByte(fileStream, 4)
            val greyBlendDestination = readByte(fileStream, 4)

            (1..(blendingLength - 8) / 8).forEach {
                val channelSourceRange = readByte(fileStream, 4)
                val channelDestinationRange = readByte(fileStream, 4)
            }
        }
    }
}
