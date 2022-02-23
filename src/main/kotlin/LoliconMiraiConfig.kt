package nya.xfy

import net.mamoe.mirai.console.data.*

object LoliconMiraiConfig : AutoSavePluginConfig("config") {
    val command: String by value("""来点(.*)色图""")
    val proxyAddress: String by value("i.pixiv.re")
    val setuOnCommand: String by value("开启色图")
    val setuOffCommand: String by value("关闭色图")
    val r18OnCommand: String by value("开启r18")
    val r18OffCommand: String by value("关闭r18")
    val startSearchingReply by value("")
    val refuseReply: String by value("不可以色色！")
    val setuOnReply: String by value("已开启色图")
    val setuOffReply: String by value("已关闭色图")
    val r18OnReply: String by value("已开启r18")
    val r18OffReply: String by value("已关闭r18")
    val noMatchResultReply by value("你的xp好怪。。。")
    val connectionFailureReply by value("连接LoliconApi失败")
    val recallTime: Int by value(-1)
    val botOwnerId: List<Long> by value()
}