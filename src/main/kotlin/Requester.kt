package nya.xfy

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class Requester(private val subject: Contact) {

    private lateinit var request: Request
    private lateinit var response: okhttp3.Response
    private lateinit var url: String
    private lateinit var okHttpClient: OkHttpClient

    private fun initOkHttpClient() {
        okHttpClient = OkHttpClient.Builder().let {
            it.readTimeout(5, TimeUnit.SECONDS)
            it.writeTimeout(5, TimeUnit.SECONDS)
            it.connectTimeout(5, TimeUnit.SECONDS)
            it.build()
        }
    }

    private fun closeOkHttpClient() {
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
        okHttpClient.cache?.close()
    }
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun request(keyword: String, num: Int) {
        try {
            initOkHttpClient()
            url = "https://api.lolicon.app/setu/v2?r18=2&proxy=i.pixiv.re&num=${num}&keyword=${keyword}"
            if(num in 1..5){
                request = Request.Builder().let {
                    it.url(url)
                    it.build()
                }
                response = okHttpClient.newCall(request).execute()
                if(response.code == 200) {
                    val response: Response = Json.decodeFromString(response.body!!.string())
                    if (response.error != "") {
                        subject.sendMessage("请求api时出错了，要不待会在试试？api错误信息${response.error}")
                    } else if (response.data.isEmpty()) {
                        subject.sendMessage("你的xp好奇怪。。。")
                    } else {
                        if (response.data.lastIndex + 1 < num) {
                            subject.sendMessage("关于[${keyword}]的图片只有${response.data.lastIndex + 1}张。")
                        }
                        for (item in response.data) {
                            request = Request.Builder().let {
                                it.url(item.urls.original)
                                //it.header("Referer","https://www.pixiv.net/")
                                //it.header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")
                                it.build()
                            }
                            val response: okhttp3.Response = okHttpClient.newCall(request).execute()
                            if (response.code == 200) {
                                response.body?.let { it1 -> subject.sendImage(it1.byteStream()) }
                            } else {
                                subject.sendMessage("图片pid: ${item.pid}已经被删除，获取失败")
                            }
                        }
                    }
                } else {
                    subject.sendMessage("请求api出错，请检查网络问题")
                }
            } else if (num < 1) {
                subject.sendMessage("你真小！！")
            } else if (num > 5) {
                subject.sendMessage("进不去！怎么看都进不去吧！！！")
            } else {
                subject.sendMessage("杰哥，这是什么啊？(疑惑状")
            }
        } catch (e: SocketTimeoutException) {
            subject.sendMessage("请求超时了，等等再试试吧？")
            Miraisetuplugin.logger.error(e)
        } catch (e: Throwable) {
            subject.sendMessage("哎呀，出错了。。。")
            Miraisetuplugin.logger.error(e)
        }finally {
            closeOkHttpClient()
        }
    }
}
