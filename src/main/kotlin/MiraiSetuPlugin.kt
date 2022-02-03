package nya.xfy

import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info
import nya.xfy.MiraiSetuPluginConfig.botOwnerId
import nya.xfy.MiraiSetuPluginConfig.friendSetuSwitch
import nya.xfy.MiraiSetuPluginConfig.groupSetuSwitch
import nya.xfy.MiraiSetuPluginConfig.recallTime
import nya.xfy.MiraiSetuPluginData.friendR18Map
import nya.xfy.MiraiSetuPluginData.groupR18Map
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object MiraiSetuPlugin : KotlinPlugin(JvmPluginDescription(id = "nya.xfy.miraisetuplugin", version = "1.4.3")) {

    val okHttpClient: OkHttpClient = OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).writeTimeout(5, TimeUnit.SECONDS).connectTimeout(5, TimeUnit.SECONDS).build()

    override fun PluginComponentStorage.onLoad() {
        MiraiSetuPluginConfig.reload()
        MiraiSetuPluginData.reload()
    }

    override fun onEnable() {
        logger.info { "Plugin loaded" }
        groupSetuListener(groupSetuSwitch)
        friendSetuListener(friendSetuSwitch)
    }

    override fun onDisable() {
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
        okHttpClient.cache?.close()
        super.onDisable()
    }

    private fun groupSetuListener(flag: Boolean) {
        this.globalEventChannel().subscribeGroupMessages {
            matching(Regex("""来(\d*)张(.*)色图""")) {
                when (flag) {
                    true -> {
                        when (it.groupValues[1].toIntOrNull() ?: 1) {
                            in 1..5 -> {
                                logger.info("正在获取${it.groupValues[1].toIntOrNull() ?: 1}张${it.groupValues[2]}色图")
                                when (recallTime) {
                                    in 1..120 -> subject.sendMessage(LoliconRequester().request(groupR18Map, subject, bot, it.groupValues[2], it.groupValues[1].toIntOrNull() ?: 1)).recallIn(recallTime.toLong())
                                    else -> subject.sendMessage(LoliconRequester().request(groupR18Map, subject, bot, it.groupValues[2], it.groupValues[1].toIntOrNull() ?: 1))
                                }
                                logger.info("${it.groupValues[1].toIntOrNull() ?: 1}张${it.groupValues[2]}色图响应完毕")
                            }
                            else -> subject.sendMessage("不可以！！！")
                        }
                    }
                    false -> subject.sendMessage("不可以色色！")
                }
            }
            case("开启r18", ignoreCase = true, trim = true) {
                if (sender.permission > MemberPermission.MEMBER || botOwnerId.contains(sender.id))
                    on(groupR18Map, subject)
            }
            case("关闭r18", ignoreCase = true, trim = true) {
                if (sender.permission > MemberPermission.MEMBER || botOwnerId.contains(sender.id))
                    off(groupR18Map, subject)
            }
        }
    }

    private fun friendSetuListener(flag: Boolean) {
        this.globalEventChannel().subscribeFriendMessages {
            matching(Regex("""来(\d*)张(.*)色图""")) {
                when (flag) {
                    true -> {
                        when (it.groupValues[1].toIntOrNull() ?: 1) {
                            in 1..5 -> {
                                logger.info("正在获取${it.groupValues[1].toIntOrNull() ?: 1}张${it.groupValues[2]}色图")
                                when (recallTime) {
                                    in 1..120 -> subject.sendMessage(LoliconRequester().request(friendR18Map, subject, bot, it.groupValues[2], it.groupValues[1].toIntOrNull() ?: 1)).recallIn(recallTime.toLong())
                                    else -> subject.sendMessage(LoliconRequester().request(friendR18Map, subject, bot, it.groupValues[2], it.groupValues[1].toIntOrNull() ?: 1))
                                }
                                logger.info("${it.groupValues[1].toIntOrNull() ?: 1}张${it.groupValues[2]}色图响应完毕")
                            }
                            else -> subject.sendMessage("不可以！！！")
                        }
                    }
                    false -> subject.sendMessage("不可以色色！")
                }
            }
            case("开启r18") {
                on(friendR18Map, subject)
            }
            case("关闭r18") {
                off(friendR18Map, subject)
            }
        }
    }
    private suspend fun on(map: MutableMap<Long, Int>, subject: Contact) {
        map[subject.id] = 2
        subject.sendMessage("已开启r18")
    }

    private suspend fun off(map: MutableMap<Long, Int>, subject: Contact) {
        map[subject.id] = 0
        subject.sendMessage("已关闭r18")
    }
}
