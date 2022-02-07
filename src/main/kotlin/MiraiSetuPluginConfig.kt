package nya.xfy

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object MiraiSetuPluginConfig : AutoSavePluginConfig("config") {
    val readTimeout:Int by value(5)
    val writeTimeout:Int by value(5)
    val connectTimeout:Int by value(5)
    val recallTime:Int by value(-1)
    val botOwnerId: List<Long> by value()
}