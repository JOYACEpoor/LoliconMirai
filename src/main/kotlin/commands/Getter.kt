package nya.xfy.commands

import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import nya.xfy.LoliconMirai
import nya.xfy.configs.CommandConfig.random
import nya.xfy.utils.Handler
import java.util.*

object Getter : SimpleCommand(LoliconMirai, "random", random, description = "获取随机色图") {
    @Handler
    suspend fun MemberCommandSenderOnMessage.handle() {
        val time = System.currentTimeMillis()
        Handler(subject, bot).handle()
        LoliconMirai.logger.info("耗时: ${(System.currentTimeMillis() - time)/1000}s")
    }
}