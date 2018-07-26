import com.google.gson.GsonBuilder
import io.xol.z.plugin.player.PlayerProfile
import org.junit.Test
import java.lang.Thread.sleep

class TestPlayerProfile {
    @Test
    fun basicPlayerProfileTest() {
        val gson = GsonBuilder().setPrettyPrinting().create()

        val michel = PlayerProfile(uuid=42, name="Michel")

        sleep(5000)

        michel.updateTime()

        val byeMichel = gson.toJson(michel)
        val michelAgain = gson.fromJson(byeMichel, PlayerProfile::class.java)

        println(byeMichel)
        println(michelAgain.timeConnected)
    }
}