package nya.xfy.commands

import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import nya.xfy.LoliconMirai
import nya.xfy.configs.CommandConfig
import nya.xfy.utils.Handler
import java.util.*

object GetterWithKeyword : SimpleCommand(LoliconMirai, "keyword", CommandConfig.keyword, description = "根据关键词获取色图") {
    @Handler
    suspend fun MemberCommandSenderOnMessage.handle(keyword: String) {
        val time = Date().time
        Handler(subject, bot).handle(keyword)
        LoliconMirai.logger.info("耗时${Date().time - time}ms")
    }
}