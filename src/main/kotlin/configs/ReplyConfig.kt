package nya.xfy.configs

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object ReplyConfig : AutoSavePluginConfig("ReplyConfig") {
    val startSearchingReply by value("少女祈祷中...")
    val refuseReply: String by value("不可以色色！")
    val setuOnReply: String by value("已开启色图")
    val setuOffReply: String by value("已关闭色图")
    val r18OnReply: String by value("已开启r18")
    val r18OffReply: String by value("已关闭r18")
    val noResultReply by value("你的xp好怪。。。")
    val connectionFailureReply by value("连接Api失败")
}