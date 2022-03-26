package nya.xfy.configs

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object NetworkConfig : AutoSavePluginConfig("NetworkConfig") {
    val connectTimeout: Long by value(0L)
    val callTimeout: Long by value(0L)
    val readTimeout: Long by value(0L)
    val writeTimeout: Long by value(0L)
    val proxyLink: String by value("i.pixiv.re")
    val proxySwitch: Boolean by value(false)
    val proxyAddress: String by value("127.0.0.1")
    val proxyPort: Int by value(10809)
}