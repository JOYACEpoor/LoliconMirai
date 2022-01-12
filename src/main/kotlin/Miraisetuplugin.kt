package nya.xfy

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import java.util.concurrent.TimeUnit

object Miraisetuplugin : KotlinPlugin(
    JvmPluginDescription(
        id = "nya.xfy.miraisetuplugin",
        version = "1.0.1",
    )
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        listenerRegister()
    }
}

private val okHttp = OkHttp.config {
    config {
        readTimeout(30, TimeUnit.SECONDS)
    }
}
val HttpClient = HttpClient(okHttp)