package nya.xfy.datas

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object Data : AutoSavePluginData("data") {
    val groupSetuMap: MutableMap<Long, Boolean> by value()
    val groupR18Map: MutableMap<Long, Int> by value()
}