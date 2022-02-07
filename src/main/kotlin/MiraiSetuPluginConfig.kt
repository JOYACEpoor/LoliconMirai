package nya.xfy

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object MiraiSetuPluginConfig : AutoSavePluginConfig("config") {
    val recallTime:Int by value(-1)
    val botOwnerId: List<Long> by value()
}