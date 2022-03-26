package nya.xfy.commands

import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import nya.xfy.LoliconMirai
import nya.xfy.configs.CommandConfig.manager
import nya.xfy.configs.ReplyConfig.r18OffReply
import nya.xfy.configs.ReplyConfig.r18OnReply
import nya.xfy.configs.ReplyConfig.setuOffReply
import nya.xfy.configs.ReplyConfig.setuOnReply
import nya.xfy.datas.Data.groupR18Map
import nya.xfy.datas.Data.groupSetuMap

object Manager : CompositeCommand(LoliconMirai, "manager", manager, description = "色图管理") {


    @SubCommand
    suspend fun MemberCommandSenderOnMessage.setuon() {
        groupSetuMap[subject.id] = true.also { subject.sendMessage(setuOnReply) }
    }

    @SubCommand
    suspend fun MemberCommandSenderOnMessage.setuoff() {
        groupSetuMap[subject.id] = false.also { subject.sendMessage(setuOffReply) }
    }

    @SubCommand
    suspend fun MemberCommandSenderOnMessage.r18on() {
        groupR18Map[subject.id] = 2.also { subject.sendMessage(r18OnReply) }
    }

    @SubCommand
    suspend fun MemberCommandSenderOnMessage.r18off() {
        groupR18Map[subject.id] = 0.also { subject.sendMessage(r18OffReply) }
    }
}