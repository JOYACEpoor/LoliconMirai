package nya.xfy

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.RawForwardMessage
import net.mamoe.mirai.message.data.buildForwardMessage
import nya.xfy.MiraiSetuPlugin.okHttpClient
import okhttp3.Request
import java.net.SocketException
import java.net.SocketTimeoutException

class LoliconRequester {
    private lateinit var response: okhttp3.Response
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun request(map: MutableMap<Long, Int>, subject: Contact, bot: Bot, keyword: String, num: Int, mode: String = "tag") {
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
                                        "keyword" -> subject.sendMessage("你的xp好怪。。。")
                                    }
                                }
                                false -> {
                                    MiraiSetuPlugin.logger.info("正在获取${num}张${keyword}色图,模式=${mode}")
                                    subject.sendMessage(buildForwardMessage(subject, object : ForwardMessage.DisplayStrategy { override fun generateTitle(forward: RawForwardMessage): String = "${num}张${keyword}色图" }) {
                                        if (loliconResponse.data.lastIndex + 1 < num)
                                            add(bot.id, bot.nameCardOrNick, PlainText("关于“${keyword}”的图片只有${loliconResponse.data.lastIndex + 1}张。"))
                                        for (item in loliconResponse.data) {
                                            response = okHttpClient.newCall(Request.Builder().url(item.urls.original).build()).execute()
                                            when (response.code) {
                                                200 -> add(bot.id, bot.nameCardOrNick, subject.uploadImage(response.body!!.byteStream()))
                                                else -> add(bot.id, bot.nameCardOrNick, PlainText("此图片可能已经被删除，获取失败\n${item.urls.original}"))
                                            }
                                        }
                                    })
                                    MiraiSetuPlugin.logger.info("${num}张${keyword}色图发送完毕")
                                }
                            }
                        }
                        else -> subject.sendMessage("请求api时出错了，待会在试试？api错误信息${loliconResponse.error}")
                    }
                }
                else -> subject.sendMessage("请求api出错，请检查网络问题")
            }
        } catch (e: IllegalStateException) {
            subject.sendMessage("图片发送失败了，再试试看吧？")
        } catch (e: SocketTimeoutException) {
            subject.sendMessage("请求${keyword}色图时超时了，等等再试试吧？")
        } catch (e: SocketException) {
            subject.sendMessage("请求${keyword}色图时连接出错了，等等再试试吧？")
        } catch (e: Throwable) {
            subject.sendMessage("哎呀，出错了。。。")
            MiraiSetuPlugin.logger.error(e)
        }finally {
            response.close()
        }
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

