import java.io.InputStream

class PSDCursor {
    private var position = 0
    private var markedPosition = 0

    fun readByte(fileStream: InputStream, byteLength: Int): ByteArray {
        position += byteLength
        return fileStream.readNBytes(byteLength)
    }

    fun markCursor(fileStream: InputStream) {
        markedPosition = position
        fileStream.mark(1000000)
    }

    fun resetCursor(fileStream: InputStream) {
        fileStream.reset()
        position = markedPosition
        markedPosition = 0
    }
}
