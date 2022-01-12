package nya.xfy

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.info

object Miraisetuplugin : KotlinPlugin(
    JvmPluginDescription(
        id = "nya.xfy.miraisetuplugin",
        version = "1.1.0",
    )
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        listenerRegister()
    }

    override fun onDisable() {
        httpClient.close()
        super.onDisable()
    }

    private fun listenerRegister() {
        Miraisetuplugin.globalEventChannel().subscribeMessages {
            finding(Regex("""来(\d)*张(.*)色图""")) {
                if (it.groups[2] != null)
                    Miraisetuplugin.logger.info("keyword: ${it.groupValues[2]}}")
                Miraisetuplugin.logger.info("正在获取${it.groupValues[1]}张色图")
                Requester(subject).request(it.groups[2]?.value ?: "", it.groups[1]?.value?.toIntOrNull() ?: 1)
            }
        }
    }
}
