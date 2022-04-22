package nya.xfy.commands

import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import nya.xfy.LoliconMirai
import nya.xfy.configs.CommandConfig.manager
import nya.xfy.configs.ReplyConfig.invalidR18Input
import nya.xfy.configs.ReplyConfig.r18MixedReply
import nya.xfy.configs.ReplyConfig.r18OffReply
import nya.xfy.configs.ReplyConfig.r18OnReply
import nya.xfy.configs.ReplyConfig.setuOffReply
import nya.xfy.configs.ReplyConfig.setuOnReply
import nya.xfy.datas.Data.groupR18Map
import nya.xfy.datas.Data.groupSetuMap

object Manager : CompositeCommand(LoliconMirai, "manager", manager, description = "色图管理") {

    @OptIn(ConsoleExperimentalApi::class)
    @SubCommand
    @Description("设置色图开启状态")
    suspend fun MemberCommandSenderOnMessage.setu(@Name("on/off") state: Boolean) {
        groupSetuMap[subject.id] = state
        subject.sendMessage(if (state) setuOnReply else setuOffReply)
    }

    @OptIn(ConsoleExperimentalApi::class)
    @SubCommand
    @Description("设置 R18 模式")
    suspend fun MemberCommandSenderOnMessage.r18(@Name("0/1/2") state: Int) {
        when (state) {
            0 -> {
                groupR18Map[subject.id] = 0
                subject.sendMessage(r18OffReply)
            }
            1 -> {
                groupR18Map[subject.id] = 1
                subject.sendMessage(r18OnReply)
            }
            2 -> {
                groupR18Map[subject.id] = 2
                subject.sendMessage(r18MixedReply)
            }
            else -> {
                subject.sendMessage(invalidR18Input)
            }
        }
    }
}