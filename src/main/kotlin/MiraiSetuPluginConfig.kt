package nya.xfy

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object MiraiSetuPluginConfig : AutoSavePluginConfig("config") {
    val friendSetu:Boolean by value(true)
    val groupSetu:Boolean by value(true)
    val botOwnerId: List<Long> by value()
}