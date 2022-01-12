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

    private lateinit var response: Response
    private suspend fun getImageStream(resource: String): InputStream =
        HttpClient(OkHttp).get(resource)

    private suspend fun sendSetu(response: Response,num: String) {
        if (response.error != "") {
            subject.sendMessage(response.error)
        } else if (response.data.isEmpty()) {
            subject.sendMessage("你的xp好奇怪啊。。。")
        } else {
            var i =0
            for (item in response.data) {
                i++
                subject.sendImage(getImageStream(item.urls.original))
            }
            if(i<num.toInt()){
                subject.sendMessage("哎呀，没有了。。。")
            }
        }
    }
    private suspend fun sendSetu(response: Response) {
        if (response.error != "") {
            subject.sendMessage(response.error)
        } else if (response.data.isEmpty()) {
            subject.sendMessage("你的xp好奇怪啊。。。")
        } else {
            for (item in response.data) {
                subject.sendImage(getImageStream(item.urls.original))
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun request(keyword: String, num: String) {
        Miraisetuplugin.logger.info("正在获取色图")
        try {
            response =
                Json.decodeFromString(HttpClient(OkHttp).get("https://api.lolicon.app/setu/v2?r18=2&proxy=i.pixiv.re&num=${num}&keyword=${keyword}"))
            if (num != "") {
                if (num.toInt() > 5) {
                    subject.sendMessage("那也太多了吧？")
                } else if (num.toInt() < 1) {
                    subject.sendMessage("你怎么这么小？")
                } else {
                    sendSetu(response,num)
                }
            } else {
                sendSetu(response)
            }
        } catch (e: Throwable) {
            Miraisetuplugin.logger.error(e)
            subject.sendMessage("好像出了点问题，待会再试试吧？")
        }
    }
}