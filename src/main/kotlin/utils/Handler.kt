package nya.xfy.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import nya.xfy.LoliconMirai
import nya.xfy.LoliconMirai.customClient
import nya.xfy.LoliconMirai.directClient
import nya.xfy.LoliconMirai.log
import nya.xfy.LoliconMirai.logger
import nya.xfy.configs.NetworkConfig.proxyLink
import nya.xfy.configs.RecallConfig
import nya.xfy.configs.ReplyConfig
import nya.xfy.configs.ReplyConfig.connectionFailureReply
import nya.xfy.configs.ReplyConfig.exceptionReply
import nya.xfy.configs.ReplyConfig.noResultReply
import nya.xfy.configs.ReplyConfig.refuseReply
import nya.xfy.datas.Data.groupR18Map
import nya.xfy.datas.Data.groupSetuMap
import okhttp3.Request
import java.io.IOException

class Handler(private val subject: Group, private val bot: Bot, private val amount: Int, private val keyword: String = "") {

    private suspend fun sendMessage(message: String) = subject.takeIf { message != "" }?.sendMessage(message)
    private fun getForwardMessageNode(message: Message): ForwardMessage.Node = ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +message })

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun handle(mode: String = "tag") {
        try {
            when (groupSetuMap[subject.id]) {
                true -> {
                    val response = directClient.newCall(Request.Builder()
                        .url("https://api.lolicon.app/setu/v2?r18=${groupR18Map[subject.id]}&proxy=${proxyLink}&num=${amount}&${mode}=${
                            keyword.replace("+",
                                "&${mode}=")
                        }").build()).execute()
                    when (response.isSuccessful) {
                        true -> {
                            log("解析中\n${response.request}")
                            val loliconResponse: LoliconResponse = Json.decodeFromString(response.body!!.string())
                            when (loliconResponse.data.isNotEmpty()) {
                                true -> subject.sendMessage(RawForwardMessage(responseHandler(loliconResponse)).render(
                                    object : ForwardMessage.DisplayStrategy {
                                        override fun generateTitle(forward: RawForwardMessage) =
                                            "${amount}张${if (keyword.isEmpty()) "色图" else " $keyword"}"
                                    })).takeIf { RecallConfig.recallTime in 1..120 }
                                    ?.recallIn((RecallConfig.recallTime * 1000).toLong())
                                else -> when (mode) {
                                    "tag" -> handle("keyword")
                                    "keyword" -> when (loliconResponse.error == "") {
                                        true -> sendMessage(noResultReply)
                                        else -> sendMessage(loliconResponse.error)
                                    }
                                }
                            }
                        }
                        else -> sendMessage(connectionFailureReply)
                    }
                    response.close()
                }
                else -> sendMessage(refuseReply)
            }
        } catch (e: IOException) {
            sendMessage(exceptionReply.replace("<Exception>", e.toString()))
        }
    }

    private suspend fun responseHandler(loliconResponse: LoliconResponse): MutableList<ForwardMessage.Node> {
        return mutableListOf<ForwardMessage.Node>().apply {
            supervisorScope {
                launch(Dispatchers.IO) { sendMessage(ReplyConfig.startSearchingReply) }
                for (item in loliconResponse.data) {
                    launch(Dispatchers.IO) {
                        val response = customClient.newCall(Request.Builder().url(item.urls.original).header("referer", "https://www.pixiv.net/").build()).execute()
                        log("PID: ${item.pid}获取中")
                        try {
                            when (response.isSuccessful) {
                                true -> this@apply.add(getForwardMessageNode(subject.uploadImage(response.body!!.byteStream().toExternalResource().toAutoCloseable())))
                                else -> this@apply.add(getForwardMessageNode(PlainText("哎呀，图片失踪了\n${item.urls.original}")))
                            }
                        } catch (e: Exception) {
                            this@apply.add(getForwardMessageNode(PlainText("哎呀，图片失踪了\n${e}\n${item.urls.original}")))
                        } finally {
                            response.close()
                        }
                        log("PID: ${item.pid}上传完毕")
                    }
                }
            }
        }
    }
}