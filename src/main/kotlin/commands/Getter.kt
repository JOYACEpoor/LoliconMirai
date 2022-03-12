package nya.xfy.commands

import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import nya.xfy.LoliconMirai
import nya.xfy.configs.CommandConfig
import nya.xfy.utils.Handler

object Getter : SimpleCommand(LoliconMirai, "random", CommandConfig.random, description = "获取随机色图") {
    @Handler
    suspend fun MemberCommandSenderOnMessage.handle() {
        Handler(subject, bot).handle()
    }
}