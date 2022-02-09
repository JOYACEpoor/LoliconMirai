package nya.xfy

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object LoliconMiraiData : AutoSavePluginData("data") {
    val groupR18Map: MutableMap<Long, Int> by value()
    val groupSetuMap: MutableMap<Long,Boolean> by value()
}