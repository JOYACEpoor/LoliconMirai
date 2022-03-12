package nya.xfy.commands

import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import nya.xfy.LoliconMirai
import nya.xfy.configs.CommandConfig
import nya.xfy.configs.ReplyConfig
import nya.xfy.datas.Data

object Manager : CompositeCommand(LoliconMirai, "manager", CommandConfig.manager, description = "色图管理") {
    @SubCommand
    suspend fun MemberCommandSenderOnMessage.setuon() {
        Data.groupSetuMap[subject.id] = true.also { subject.sendMessage(ReplyConfig.setuOnReply) }
    }

    @SubCommand
    suspend fun MemberCommandSenderOnMessage.setuoff() {
        Data.groupSetuMap[subject.id] = false.also { subject.sendMessage(ReplyConfig.setuOffReply) }
    }

    @SubCommand
    suspend fun MemberCommandSenderOnMessage.r18on() {
        Data.groupR18Map[subject.id] = 2.also { subject.sendMessage(ReplyConfig.r18OnReply) }
    }

    @SubCommand
    suspend fun MemberCommandSenderOnMessage.r18off() {
        Data.groupR18Map[subject.id] = 0.also { subject.sendMessage(ReplyConfig.r18OffReply) }
    }
}