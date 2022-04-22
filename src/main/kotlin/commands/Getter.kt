package nya.xfy.commands

import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import nya.xfy.LoliconMirai
import nya.xfy.LoliconMirai.log
import nya.xfy.configs.CommandConfig.random
import nya.xfy.utils.Handler
import java.util.*

object Getter : SimpleCommand(LoliconMirai, "random", random, description = "获取随机色图") {
    @OptIn(ConsoleExperimentalApi::class)
    @Handler
    suspend fun MemberCommandSenderOnMessage.handle(@Name("数量") amount: Int = (5..10).random()) {
        val time = System.currentTimeMillis()
        Handler(subject, bot, amount).handle()
        log("耗时: ${(System.currentTimeMillis() - time)/1000}s")
    }
}