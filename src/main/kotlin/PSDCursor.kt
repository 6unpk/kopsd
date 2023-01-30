import java.io.InputStream

class PSDCursor {
    private var position = 0

    fun readByte(fileStream: InputStream, byteLength: Int): ByteArray {
        position += byteLength
        return fileStream.readNBytes(byteLength)
    }

    fun markCurrent(fileStream: InputStream, byteLength: Int) {

    }

    fun resetCursor(fileStream: InputStream) {
//        fileStream.reset()
        position = 0
    }
}