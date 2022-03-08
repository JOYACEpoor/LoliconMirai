package nya.xfy

import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.RawForwardMessage
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object LoliconMirai : KotlinPlugin(JvmPluginDescription(id = "nya.xfy.LoliconMirai", version = "1.6.0")) {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().readTimeout(0, TimeUnit.SECONDS).writeTimeout(0, TimeUnit.SECONDS).connectTimeout(0, TimeUnit.SECONDS).build()

    override fun onEnable() {

        LoliconMiraiConfig.reload()
        LoliconMiraiData.reload()

        CommandManager.registerCommand(object : SimpleCommand(LoliconMirai, "来点色图", description = "无关键词 色图") {
            @Handler
            suspend fun MemberCommandSenderOnMessage.handle() {
                sendSetu(subject, bot)
            }
        })
        CommandManager.registerCommand(object : SimpleCommand(LoliconMirai, "来点", description = "带关键词 色图") {
            @Handler
            suspend fun MemberCommandSenderOnMessage.handle(keyword: String) {
                sendSetu(subject, bot, keyword)
            }
        })

        CommandManager.registerCommand(object : CompositeCommand(LoliconMirai, "manage", description = "管理色图") {
            @SubCommand
            suspend fun MemberCommandSenderOnMessage.setuOn() {
                LoliconMiraiData.groupSetuMap[subject.id] =
                    true.also { subject.sendMessage(LoliconMiraiConfig.setuOnReply) }
            }

            @SubCommand
            suspend fun MemberCommandSenderOnMessage.setuOff() {
                LoliconMiraiData.groupSetuMap[subject.id] =
                    false.also { subject.sendMessage(LoliconMiraiConfig.setuOffReply) }
            }

            @SubCommand
            suspend fun MemberCommandSenderOnMessage.r18On() {
                LoliconMiraiData.groupR18Map[subject.id] =
                    2.also { subject.sendMessage(LoliconMiraiConfig.r18OnReply) }
            }

            @SubCommand
            suspend fun MemberCommandSenderOnMessage.r18Off() {
                LoliconMiraiData.groupR18Map[subject.id] =
                    0.also { subject.sendMessage(LoliconMiraiConfig.r18OffReply) }
            }
        })
    }

    //Override sendMessage
    private suspend fun sendMessage(subject: Group, message: String?) =
        subject.takeIf { message != "" && message != null }?.sendMessage(message!!)

    //SendSetuHere
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun sendSetu(subject: Group, bot: Bot, keyword: String = "", mode: String = "tag") {
        val mutableList = mutableListOf<ForwardMessage.Node>()
        when (LoliconMiraiData.groupSetuMap[subject.id]) {
            true -> {
                val response = okHttpClient.newCall(Request.Builder().url("https://api.lolicon.app/setu/v2?r18=${LoliconMiraiData.groupR18Map[subject.id]}&proxy=i.pixiv.re&num=${(1..10).random()}&${mode}=${keyword}").build()).execute()
                sendMessage(subject, LoliconMiraiConfig.startSearchingReply)
                when (response.isSuccessful) {
                    true -> {
                        val loliconResponse: LoliconResponse = Json.decodeFromString(response.body!!.string())
                        when (loliconResponse.error == "" && loliconResponse.data.isNotEmpty()) {
                            true -> {
                                supervisorScope {
                                    for (item in loliconResponse.data) {
                                        launch {
                                            val response = okHttpClient.newCall(Request.Builder().url(item.urls.original).build()).execute()
                                            logger.info("PID: ${item.pid}获取中")
                                            try {
                                                when (response.isSuccessful) {
                                                    true -> mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +subject.uploadImage(response.body!!.byteStream().toExternalResource().toAutoCloseable()) }))
                                                    else -> mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +"哎呀，图片失踪了\n${item.urls.original}" }))
                                                }
                                            } catch (e: Exception) { mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +"哎呀，图片失踪了\n${item.urls.original}" }))
                                            } finally { response.close() }
                                            logger.info("PID: ${item.pid}上传完毕")
                                        }
                                    }
                                }
                            }
                            else -> when (mode) {
                                "tag" -> sendSetu(subject, bot, keyword, "keyword")
                                "keyword" -> sendMessage(subject, LoliconMiraiConfig.noResultReply)
                            }
                        }
                    }
                    else -> sendMessage(subject, LoliconMiraiConfig.connectionFailureReply)
                }
                response.close()
            }
            else -> sendMessage(subject, LoliconMiraiConfig.refuseReply)
        }
        subject.sendMessage(when (keyword) {
                "" -> RawForwardMessage(mutableList).render(ForwardMessage.DisplayStrategy)
                else -> RawForwardMessage(mutableList).render(object : ForwardMessage.DisplayStrategy {
                    override fun generateTitle(forward: RawForwardMessage) = keyword
                })
            }
        ).takeIf { LoliconMiraiConfig.recallTime in 1..120 }?.recallIn((LoliconMiraiConfig.recallTime * 1000).toLong())
    }
}
