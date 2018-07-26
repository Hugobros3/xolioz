import io.xol.z.plugin.UnpackDefaults
import org.junit.Test
import org.junit.rules.TemporaryFolder



class TestUnpack {

    @Test
    fun testUnpacking() {
        val random = TemporaryFolder().newFolder()
        random.mkdirs()
        UnpackDefaults(random)
        println((random.list() as Array<String>).joinToString() )
    }
}