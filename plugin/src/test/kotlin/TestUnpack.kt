import io.xol.z.plugin.util.UnpackDefaults
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TestUnpack {

    @Test
    fun testUnpacking() {
        val tempFolder = TemporaryFolder();
        tempFolder.create()
        val random = tempFolder.newFolder()
        random.mkdirs()
        UnpackDefaults(random)
        println((random.list() as Array<String>).joinToString() )
    }
}