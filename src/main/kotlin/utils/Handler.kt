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
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.RawForwardMessage
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import nya.xfy.LoliconMirai
import nya.xfy.configs.NetworkConfig
import nya.xfy.configs.RecallConfig
import nya.xfy.configs.ReplyConfig
import nya.xfy.datas.Data
import okhttp3.Request

class Handler(private val subject: Group, private val bot: Bot) {

    private suspend fun sendMessage(message: String) = subject.takeIf { message != "" }?.sendMessage(message)

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun handle(keyword: String = "", mode: String = "tag") {
        when (Data.groupSetuMap[subject.id]) {
            true -> {
                val response = LoliconMirai.okHttpClient.newCall(Request.Builder().url("https://api.lolicon.app/setu/v2?r18=${Data.groupR18Map[subject.id]}&proxy=${NetworkConfig.proxyLink}&num=${(5..10).random()}&${mode}=${keyword}").build()).execute()
                when (response.isSuccessful) {
                    true -> {
                        val loliconResponse: LoliconResponse = Json.decodeFromString(response.body!!.string())
                        when (loliconResponse.error == "" && loliconResponse.data.isNotEmpty()) {
                            true -> responseHandler(loliconResponse, keyword)
                            else -> when (mode) {
                                "tag" -> handle(keyword, "keyword")
                                "keyword" -> sendMessage(ReplyConfig.noResultReply)
                            }
                        }
                    }
                    else -> sendMessage(ReplyConfig.connectionFailureReply)
                }
                response.close()
            }
            else -> sendMessage(ReplyConfig.refuseReply)
        }
    }

    private suspend fun responseHandler(loliconResponse: LoliconResponse, keyword: String) {
        sendMessage(ReplyConfig.startSearchingReply)
        val mutableList = mutableListOf<ForwardMessage.Node>()
        supervisorScope {
            for (item in loliconResponse.data) {
                launch(Dispatchers.IO) {
                    val response = LoliconMirai.okHttpClient.newCall(Request.Builder().url(item.urls.original).header("referer","https://www.pixiv.net/").build()).execute()
                    LoliconMirai.logger.info("PID: ${item.pid}获取中")
                    try {
                        when (response.isSuccessful) {
                            true -> mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +subject.uploadImage(response.body!!.byteStream().toExternalResource().toAutoCloseable()) }))
                            else -> mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +"哎呀，图片失踪了\n${item.urls.original}" }))
                        }
                    } catch (e: Exception) {
                        mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +"哎呀，图片失踪了\n${e}\n${item.urls.original}" }))
                    } finally {
                        response.close()
                    }
                    LoliconMirai.logger.info("PID: ${item.pid}上传完毕")
                }
            }
        }
        subject.sendMessage(when (keyword) {
            "" -> RawForwardMessage(mutableList).render(ForwardMessage.DisplayStrategy)
            else -> RawForwardMessage(mutableList).render(object : ForwardMessage.DisplayStrategy {
                override fun generateTitle(forward: RawForwardMessage) = keyword
            })
        }
        ).takeIf { RecallConfig.recallTime in 1..120 }?.recallIn((RecallConfig.recallTime * 1000).toLong())
    }
}