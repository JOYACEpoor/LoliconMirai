package nya.xfy

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import java.io.InputStream

class Requester(private val subject: Contact) {

    private suspend fun getImageStream(resource: String): InputStream =
        HttpClient.get(resource)

    private suspend fun sendSetu(response: Response,num: Int) {
        if (response.error != "") {
            subject.sendMessage(response.error)
        } else if (response.data.isEmpty()) {
            subject.sendMessage("你的xp好奇怪啊。。。")
        } else {
            var i = 0
            for (item in response.data) {
                i++
                subject.sendImage(getImageStream(item.urls.original))
            }
            if (i < num){
                subject.sendMessage("哎呀，没有了。。。")
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun request(keyword: String, num: Int) {
        Miraisetuplugin.logger.info("正在获取色图")
        try {
           val response: Response =
                Json.decodeFromString(HttpClient(OkHttp).get("https://api.lolicon.app/setu/v2?r18=2&proxy=i.pixiv.re&num=${num}&keyword=${keyword}"))
                if (num > 5) {
                    subject.sendMessage("那也太多了吧？")
                } else if (num < 1) {
                    subject.sendMessage("你怎么这么小？")
                } else {
                    sendSetu(response, num)
                }

        } catch (e: Throwable) {
            Miraisetuplugin.logger.error(e)
            subject.sendMessage("好像出了点问题，待会再试试吧？")
        }
    }
}