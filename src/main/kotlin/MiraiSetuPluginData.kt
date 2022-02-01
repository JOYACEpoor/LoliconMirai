package nya.xfy

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object MiraiSetuPluginData : AutoSavePluginData("data") {
    var groupR18Switch: MutableMap<Long, Int> by value()
    var friendR18Switch: MutableMap<Long, Int> by value()
}