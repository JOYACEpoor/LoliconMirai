package nya.xfy

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildForwardMessage
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class LoliconRequester(private val subject: Contact) {

    private lateinit var response: okhttp3.Response

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().let {
        it.readTimeout(5, TimeUnit.SECONDS)
        it.writeTimeout(5, TimeUnit.SECONDS)
        it.connectTimeout(5, TimeUnit.SECONDS)
        it.build()
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

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun request(url: String, keyword: String, num: Int) {
        try {
            response = okHttpClient.newCall(Request.Builder().let {
                it.url(url)
                it.build()
            }).execute()
            if (response.code == 200) {
                val loliconResponse: LoliconResponse = Json.decodeFromString(response.body!!.string())
                if (loliconResponse.error != "") {
                    subject.sendMessage("请求api时出错了，待会在试试？api错误信息${loliconResponse.error}")
                } else if (loliconResponse.data.isEmpty()) {
                    subject.sendMessage("你的xp好奇怪。。。")
                } else {
                    subject.sendMessage(buildForwardMessage(subject, ForwardMessage.DisplayStrategy) {
                        for (item in loliconResponse.data) {
                            response = okHttpClient.newCall(Request.Builder().let {
                                it.url(item.urls.original)
                                //it.header("Referer","https://www.pixiv.net/")
                                //it.header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")
                                it.build()
                            }).execute()
                            if (response.code == 200)
                                add(subject.id, "L", subject.uploadImage(response.body!!.byteStream()))
                            else
                                add(subject.id, "L", PlainText("此图片可能已经被删除，获取失败\n${item.urls.original}"))
                        }
                        if (loliconResponse.data.lastIndex + 1 < num)
                            add(subject.id, "L",PlainText("关于“${keyword}”的图片只有${loliconResponse.data.lastIndex + 1}张。"))
                    })
                    MiraiSetuPlugin.logger.info("${num}张${keyword}色图发送完成")
                }
            } else {
                subject.sendMessage("请求api出错，请检查网络问题")
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
            okHttpClient.dispatcher.executorService.shutdown()
            okHttpClient.connectionPool.evictAll()
            okHttpClient.cache?.close()
        }
    }
}
