package nya.xfy

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.concurrent.TimeUnit

class Requester(private val subject: Contact) {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS).connectTimeout(30, TimeUnit.SECONDS).build()

    private fun getResponseCode(url: String): Int =
        okHttpClient.newCall(Request.Builder().url(url).build()).execute().code

    private fun getImgInputStream(url: String): InputStream =
        okHttpClient.newCall(Request.Builder().url(url).build()).execute().body!!.byteStream()

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun request(keyword: String, num: Int) {
        val url = "https://api.lolicon.app/setu/v2?r18=2&proxy=i.pixiv.re&num=${num}&keyword=${keyword}"
        try {
            if (getResponseCode(url) != 200) {
                subject.sendMessage("Api好像连不上了，待会再试试看吧？")
                Miraisetuplugin.logger.error("url: $url")
                Miraisetuplugin.logger.error("responseCode: ${getResponseCode(url)}")
            } else {
                if (num > 5 || num < 1) {
                    if (num < 1)
                        subject.sendMessage("你真小")
                    if (num > 5)
                        subject.sendMessage("太大了吧！！怎么看都进不去吧！")
                } else
                    sendSetu(
                        Json.decodeFromString(
                            okHttpClient.newCall(Request.Builder().url(url).build()).execute().body?.string()!!
                        ), num
                    )
            }
        } catch (e: Throwable) {
            subject.sendMessage("$e")
            Miraisetuplugin.logger.error(e)
        }
    }

    private suspend fun sendSetu(response: Response, num: Int) {
        try {
        if (response.error != "")
            subject.sendMessage(response.error)
        else if (response.data.isEmpty())
            subject.sendMessage("你的xp好奇怪啊。。。")
        else {
                var i = 0
                for (item in response.data) {
                    i++
                    if (getResponseCode(item.urls.original) != 200)
                        subject.sendMessage("PID: ${item.pid}无法访问，可能已经被删除")
                    else
                        subject.sendImage(getImgInputStream(item.urls.original))
                }
                if (i < num)
                    subject.sendMessage("哎呀，没有了。。。")
            }
        }catch (e: Throwable) {
            subject.sendMessage("$e")
            Miraisetuplugin.logger.error(e)
        }
    }
}