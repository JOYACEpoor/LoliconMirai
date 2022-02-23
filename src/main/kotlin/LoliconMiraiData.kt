package nya.xfy

import net.mamoe.mirai.console.data.*

object LoliconMiraiData : AutoSavePluginData("data") {
    val apiAddress: String by value("https://api.lolicon.app/setu/v2")
    val groupSetuMap: MutableMap<Long, Boolean> by value()
    val groupR18Map: MutableMap<Long, Int> by value()
}