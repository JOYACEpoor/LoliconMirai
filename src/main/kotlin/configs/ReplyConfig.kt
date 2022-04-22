package nya.xfy.configs

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object ReplyConfig : AutoSavePluginConfig("ReplyConfig") {
    val startSearchingReply by value("少女祈祷中...")
    val refuseReply: String by value("不可以色色！")
    val setuOnReply: String by value("已开启色图")
    val setuOffReply: String by value("已关闭色图")
    val r18OffReply: String by value("已关闭 R18")
    val r18OnReply: String by value("已开启 R18")
    val r18MixedReply: String by value("已开始 R18 混合模式")
    val invalidR18Input: String by value("输入无效. 支持参数如下:\n0 - 关闭 R18 模式\n1 - 开始 R18 模式\n2 - R18 混合模式")
    val noResultReply by value("你的xp好怪。。。")
    val connectionFailureReply by value("连接Api失败")
}