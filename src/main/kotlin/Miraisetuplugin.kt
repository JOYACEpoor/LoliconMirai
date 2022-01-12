package nya.xfy

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

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

    override fun onDisable() {
        super.onDisable()
    }
}