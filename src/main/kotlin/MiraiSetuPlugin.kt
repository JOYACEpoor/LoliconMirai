package nya.xfy

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info

object MiraiSetuPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "nya.xfy.miraisetuplugin",
        version = "1.4.0",
    )
) {

    private var groupMap = MiraiSetuPluginData.groupR18Switch
    private var friendMap = MiraiSetuPluginData.friendR18Switch

    override fun onEnable() {
        logger.info { "Plugin loaded" }

        MiraiSetuPluginConfig.reload()
        MiraiSetuPluginData.reload()

        this.globalEventChannel().subscribeGroupMessages {

            finding(Regex("""来(\d*)张(.*)色图""")) {
                if ((it.groupValues[1].toIntOrNull() ?: 1) < 1) {
                    subject.sendMessage("你真小！！")
                }
                else if ((it.groupValues[1].toIntOrNull() ?: 1) > 5) {
                    subject.sendMessage("进不去！怎么看都进不去吧！！！")
                }
                else {
                    MiraiSetuPlugin.logger.info("正在获取${it.groupValues[1].toIntOrNull() ?: 1}张${it.groupValues[2]}色图")
                    LoliconRequester(subject).request(
                        "https://api.lolicon.app/setu/v2?r18=${groupMap[subject.id]}&proxy=i.pixiv.re&num=${it.groupValues[1].toIntOrNull() ?: 1}&keyword=${it.groupValues[2]}",
                        it.groupValues[2],
                        it.groupValues[1].toIntOrNull() ?: 1
                    )
                }
            }
            case("开启r18", ignoreCase = true) {
                if (sender.permission > MemberPermission.MEMBER ||  MiraiSetuPluginConfig.botOwnerId.contains(sender.id)) {
                    groupMap[group.id] = 2
                    subject.sendMessage("已开启r18")
                }
            }
            case("关闭r18", ignoreCase = true) {
                if (sender.permission > MemberPermission.MEMBER || MiraiSetuPluginConfig.botOwnerId.contains(sender.id)) {
                    groupMap[group.id] = 0
                    subject.sendMessage("已关闭r18")
                }
            }
        }

        this.globalEventChannel().subscribeFriendMessages {
            finding(Regex("""来(\d*)张(.*)色图""")) {
                if ((it.groupValues[1].toIntOrNull() ?: 1) < 1) {
                    subject.sendMessage("你真小！！")
                }
                else if ((it.groupValues[1].toIntOrNull() ?: 1) > 5) {
                    subject.sendMessage("进不去！怎么看都进不去吧！！！")
                }
                else {
                    MiraiSetuPlugin.logger.info("正在获取${it.groupValues[1].toIntOrNull() ?: 1}张${it.groupValues[2]}色图")
                    LoliconRequester(subject).request(
                        "https://api.lolicon.app/setu/v2?r18=${friendMap[subject.id]}&proxy=i.pixiv.re&num=${it.groupValues[1].toIntOrNull() ?: 1}&keyword=${it.groupValues[2]}",
                        it.groupValues[2],
                        it.groupValues[1].toIntOrNull() ?: 1
                    )
                }
            }
            case("开启r18") {
                friendMap[sender.id] = 2
                subject.sendMessage("已开启r18")
            }
            case("关闭r18") {
                friendMap[sender.id] = 0
                subject.sendMessage("已关闭r18")
            }
        }
    }

    override fun onDisable() {
        MiraiSetuPluginData.groupR18Switch = groupMap
        MiraiSetuPluginData.friendR18Switch = friendMap
        super.onDisable()
    }
}
