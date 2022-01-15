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
        version = "1.3.1",
    )
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        launch {
            listener()
        }
    }

    private fun listener() {
        Miraisetuplugin.globalEventChannel().subscribeMessages {
            finding(Regex("""来(\d*)张(.*)色图""")) {
                Miraisetuplugin.logger.info("正在获取${it.groupValues[1].toIntOrNull() ?: 1}张${it.groupValues[2]}色图")
                LoliconRequester(subject).request(it.groupValues[2], it.groupValues[1].toIntOrNull() ?: 1)
            }
        }
    }
}
