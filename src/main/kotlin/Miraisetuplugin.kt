package nya.xfy

import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.info

object Miraisetuplugin : KotlinPlugin(
    JvmPluginDescription(
        id = "nya.xfy.miraisetuplugin",
        version = "1.3.0",
    )
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        initOkHttpClient()
        launch {
            listener()
        }
    }

    override fun onDisable() {
        closeOkHttpClient()
        Miraisetuplugin.logger.info { "OkHttpClient Closed" }
        super.onDisable()
    }

    private fun listener() {
        Miraisetuplugin.globalEventChannel().subscribeMessages {
            finding(Regex("""来(\d*)张(.*)色图""")) {
                if ((it.groups[2] != null) && (it.groupValues[2] != ""))
                    Miraisetuplugin.logger.info("keyword: ${it.groupValues[2]}")
                if ((it.groups[1] != null) && (it.groupValues[1] != ""))
                    Miraisetuplugin.logger.info("num: ${it.groupValues[1].toIntOrNull() ?: 1}")
                Miraisetuplugin.logger.info("正在获取色图")
                Requester(subject).request(it.groupValues[2], it.groupValues[1].toIntOrNull() ?: 1)
            }
        }
    }
}
