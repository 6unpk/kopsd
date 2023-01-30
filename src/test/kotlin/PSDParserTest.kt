import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream

class PSDParserTest: StringSpec({
    "PSD Read" {
        val fs = withContext(Dispatchers.IO) {
            FileInputStream("./prototype-sample-rc-1.psd")
        }
        val parser = PSDParser(fs).apply {
            parseHeader()
            parseColorModeData()
            parseImageResource()
            parseResourceMaskAndLayer()
        }
        withContext(Dispatchers.IO) {
            fs.close()
        }
    }
})