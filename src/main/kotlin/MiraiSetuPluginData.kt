package nya.xfy

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object MiraiSetuPluginData : AutoSavePluginData("data") {
    val groupR18Map: MutableMap<Long, Int> by value()
    val friendR18Map: MutableMap<Long, Int> by value()
}