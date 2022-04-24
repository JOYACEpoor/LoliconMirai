package nya.xfy.configs

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object CommandConfig : AutoSavePluginConfig("CommandConfig") {
    val random: String by value("来点色图")
    val keyword: String by value("来点")
    val manager: String by value("色图管理")

    val verbose: Boolean by value(true)
}