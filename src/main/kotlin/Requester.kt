package nya.xfy

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import okhttp3.Request

class Requester(private val subject: Contact) {

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun request(okHttpClient:OkHttpClient,keyword: String, num: Int) {
        val url = "https://api.lolicon.app/setu/v2?r18=2&proxy=i.pixiv.re&num=${num}&keyword=${keyword}"
        try {
            val responseCode: Int = okHttpClient.newCall(Request.Builder().url(url).build()).execute().code
            if (responseCode != 200) {
                subject.sendMessage("Api好像连不上了，待会再试试看吧？")
                Miraisetuplugin.logger.error("url: $url")
                Miraisetuplugin.logger.error("responseCode: $responseCode")
            } else {
                if (num > 5 || num < 1) {
                    if (num < 1)
                        subject.sendMessage("你真小")
                    if (num > 5)
                        subject.sendMessage("太大了吧！！怎么看都进不去吧！")
                } else
                    sendSetu(okHttpClient,
                        Json.decodeFromString(
                            okHttpClient.newCall(Request.Builder().url(url).build()).execute().body?.string()!!
                        ), num
                    )
            }
            closeOkHttpClient()
        } catch (e: Throwable) {
            subject.sendMessage("哎呀，出错了，待会再试试吧？")
            Miraisetuplugin.logger.error(e)
        }
    }

    private suspend fun sendSetu(okHttpClient:OkHttpClient,response: Response, num: Int) {
        try {
        if (response.error != "")
            subject.sendMessage(response.error)
        else if (response.data.isEmpty())
            subject.sendMessage("你的xp好奇怪啊。。。")
        else {
                var i = 0
                for (item in response.data) {
                    i++
                    if (okHttpClient.newCall(Request.Builder().url(item.urls.original).build()).execute().code != 200)
                        subject.sendMessage("PID: ${item.pid}无法访问，可能已经被删除")
                    else {
                        subject.sendImage(
                            okHttpClient.newCall(Request.Builder().url(item.urls.original).build())
                                .execute().body!!.byteStream()
                        )
                    }
                }
                if (i < num)
                    subject.sendMessage("哎呀，没有了。。。")
            }
            closeOkHttpClient()
        }catch (e: Throwable) {
            subject.sendMessage("哎呀，出错了，待会再试试吧？")
            Miraisetuplugin.logger.error(e)
        }
    }
}