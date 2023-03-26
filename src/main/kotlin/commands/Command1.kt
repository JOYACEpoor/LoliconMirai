package nya.xfy.commands

import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import nya.xfy.LoliconMirai
import nya.xfy.configs.CommandConfig.random

object Command1 : SimpleCommand(LoliconMirai, "random", random, description = "获取随机色图") {
    @Handler
    suspend fun MemberCommandSenderOnMessage.handle() {
        val time = System.currentTimeMillis()
        LoliconMirai.getSetu(1, "", subject, bot)
        LoliconMirai.logInfo("耗时: ${(System.currentTimeMillis() - time) / 1000}s")
    }
}