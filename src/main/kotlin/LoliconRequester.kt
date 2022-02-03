package nya.xfy

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.RawForwardMessage
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import nya.xfy.MiraiSetuPlugin.logger
import nya.xfy.MiraiSetuPlugin.okHttpClient
import okhttp3.Request
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*

class LoliconRequester {
    private lateinit var response: okhttp3.Response
    private val mutableList = mutableListOf<ForwardMessage.Node>()
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun request(
        map: MutableMap<Long, Int>,
        subject: Contact,
        bot: Bot,
        keyword: String,
        num: Int,
        mode: String = "tag"
    ): ForwardMessage {
        try {
            response = okHttpClient.newCall(Request.Builder().url("https://api.lolicon.app/setu/v2?r18=${map[subject.id]}&proxy=i.pixiv.re&num=${num}&${mode}=${keyword}").build()).execute()
            val loliconResponse: LoliconResponse = Json.decodeFromString(response.body!!.string())
            when (response.code) {
                200 -> {
                    when (loliconResponse.error) {
                        "" -> {
                            when (loliconResponse.data.isEmpty()) {
                                true -> {
                                    when (mode) {
                                        "tag" -> request(map, subject, bot, keyword, num, "keyword")
                                        "keyword" -> mutableList.add(ForwardMessage.Node(bot.id, Date().time.toInt(), bot.nameCardOrNick, buildMessageChain { +PlainText("你的xp好怪。。。") }))
                                    }
                                }
                                else ->{
                                    if (loliconResponse.data.lastIndex + 1 < num)
                                        mutableList.add(ForwardMessage.Node(bot.id, Date().time.toInt(), bot.nameCardOrNick, buildMessageChain { +PlainText("关于[${keyword}]的图片只有${loliconResponse.data.lastIndex + 1}张") }))
                                    for (item in loliconResponse.data) {
                                        response = okHttpClient.newCall(Request.Builder().url(item.urls.original).build()).execute()
                                        when (response.code) {
                                            200 -> mutableList.add(ForwardMessage.Node(bot.id, Date().time.toInt(), bot.nameCardOrNick, buildMessageChain { +subject.uploadImage(response.body!!.byteStream().toExternalResource().toAutoCloseable()) }))
                                            else -> mutableList.add(ForwardMessage.Node(bot.id, Date().time.toInt(),bot.nameCardOrNick, buildMessageChain { +PlainText("哎呀，图片失踪了\n${item.urls.original}") }))
                                        }
                                    }
                                }
                            }
                        }
                        else -> mutableList.add(ForwardMessage.Node(bot.id, Date().time.toInt(), bot.nameCardOrNick, buildMessageChain { +PlainText("请求api时出错了，待会在试试？api错误信息${loliconResponse.error}") }))
                    }
                }
                else -> mutableList.add(ForwardMessage.Node(bot.id, Date().time.toInt(), bot.nameCardOrNick, buildMessageChain { +PlainText("请求api出错，请检查网络问题") }))
            }
        } catch (e: IllegalStateException) {
            mutableList.add(ForwardMessage.Node(bot.id, Date().time.toInt(), bot.nameCardOrNick, buildMessageChain { +PlainText("图片发送失败了，再试试看吧？") }))
        } catch (e: SocketTimeoutException) {
            mutableList.add(ForwardMessage.Node(bot.id, Date().time.toInt(), bot.nameCardOrNick, buildMessageChain { +PlainText("请求${keyword}色图时超时了，等等再试试吧？") }))
        } catch (e: SocketException) {
            mutableList.add(ForwardMessage.Node(bot.id, Date().time.toInt(), bot.nameCardOrNick, buildMessageChain { +PlainText("请求${keyword}色图时连接出错了，等等再试试吧？") }))
         } catch (e: Throwable) {
            mutableList.add(ForwardMessage.Node(bot.id, Date().time.toInt(), bot.nameCardOrNick, buildMessageChain { +PlainText("哎呀，出错了。。。") }))
            logger.error(e)
        } finally {
            response.close()
        }
        return RawForwardMessage(mutableList).render(object : ForwardMessage.DisplayStrategy { override fun generateTitle(forward: RawForwardMessage): String { return "${num}张${keyword}色图" } })
    }

    @Serializable
    private data class LoliconResponse(val error: String, val data: List<Data>) {
        @Serializable
        data class Data(
            val pid: Int,
            val p: Int,
            val uid: Int,
            val title: String,
            val author: String,
            val r18: Boolean,
            val width: Int,
            val height: Int,
            val tags: MutableList<String>,
            val ext: String,
            val uploadDate: Long,
            val urls: Urls
        ) {
            @Serializable
            data class Urls(val original: String)
        }
    }
}

