package nya.xfy.commands

import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import nya.xfy.LoliconMirai
import nya.xfy.configs.CommandConfig.keyword

object Command2 : SimpleCommand(LoliconMirai, "keyword", keyword, description = "根据关键词获取色图") {
    @OptIn(ConsoleExperimentalApi::class)
    @Handler
    suspend fun MemberCommandSenderOnMessage.handle(@Name("标签/关键词") keyword: String, @Name("数量") amount:Int) {
        val time = System.currentTimeMillis()
        LoliconMirai.getSetu(amount, keyword, subject, bot)
        LoliconMirai.logInfo("耗时: ${(System.currentTimeMillis() - time) / 1000}s")
    }
}