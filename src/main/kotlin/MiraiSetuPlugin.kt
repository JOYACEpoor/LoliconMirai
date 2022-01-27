package nya.xfy

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object MiraiSetuPlugin : KotlinPlugin(JvmPluginDescription(id = "nya.xfy.miraisetuplugin", version = "1.4.1")) {

    private val groupMap = MiraiSetuPluginData.groupR18Switch
    private val friendMap = MiraiSetuPluginData.friendR18Switch

    val okHttpClient: OkHttpClient = OkHttpClient.Builder().let {
        it.readTimeout(5, TimeUnit.SECONDS)
        it.writeTimeout(5, TimeUnit.SECONDS)
        it.connectTimeout(5, TimeUnit.SECONDS)
        it.build()
    }

    override fun onEnable() {
        logger.info { "Plugin loaded" }

        MiraiSetuPluginConfig.reload()
        MiraiSetuPluginData.reload()

        this.globalEventChannel().subscribeGroupMessages {
            matching(Regex("""来(\d*)张(.*)色图""")) {
                when (it.groupValues[1].toIntOrNull() ?: 1) {
                    in Int.MIN_VALUE..0 -> subject.sendMessage("你真小！！")
                    in 6..Int.MAX_VALUE -> subject.sendMessage("进不去！怎么看都进不去吧！！！")
                    else -> LoliconRequester().request(groupMap, subject, bot, it.groupValues[2], it.groupValues[1].toIntOrNull() ?: 1)
                }
            }
            case("开启r18", ignoreCase = true, trim = true) {
                if (sender.permission > MemberPermission.MEMBER || MiraiSetuPluginConfig.botOwnerId.contains(sender.id))
                    on(groupMap, subject)
            }
            case("关闭r18", ignoreCase = true, trim = true) {
                if (sender.permission > MemberPermission.MEMBER || MiraiSetuPluginConfig.botOwnerId.contains(sender.id))
                    off(friendMap, subject)
            }
        }
        this.globalEventChannel().subscribeFriendMessages {
            matching(Regex("""来(\d*)张(.*)色图""")) {
                when (it.groupValues[1].toIntOrNull() ?: 1) {
                    in Int.MIN_VALUE..0 -> subject.sendMessage("你真小！！")
                    in 6..Int.MAX_VALUE -> subject.sendMessage("进不去！怎么看都进不去吧！！！")
                    else -> LoliconRequester().request(friendMap, subject, bot, it.groupValues[2], it.groupValues[1].toIntOrNull() ?: 1)
                }
            }
            case("开启r18") {
                on(friendMap, subject)
            }
            case("关闭r18") {
                off(friendMap, subject)
            }
        }
    }


    override fun onDisable() {
        MiraiSetuPluginData.groupR18Switch = groupMap
        MiraiSetuPluginData.friendR18Switch = friendMap
        closeHttpClient(okHttpClient)
        super.onDisable()
    }

    private suspend fun on(map: MutableMap<Long, Int>, subject: Contact) {
        map[subject.id] = 2
        subject.sendMessage("已开启r18")
    }

    private suspend fun off(map: MutableMap<Long, Int>, subject: Contact) {
        map[subject.id] = 0
        subject.sendMessage("已开启r18")
    }

    private fun closeHttpClient(okHttpClient: OkHttpClient) {
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
        okHttpClient.cache?.close()
    }
}
