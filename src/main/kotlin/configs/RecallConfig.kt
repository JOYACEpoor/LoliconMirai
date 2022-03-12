package nya.xfy.configs

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object RecallConfig : AutoSavePluginConfig("RecallConfig") {
    val recallTime: Int by value(-1)
}