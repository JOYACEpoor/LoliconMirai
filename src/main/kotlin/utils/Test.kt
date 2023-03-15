package nya.xfy.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class Test {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val directClient : OkHttpClient = OkHttpClient.Builder().build()
            val response = directClient.newCall(Request.Builder().url("https://api.lolicon.app/setu?r18=2&proxy=i.pixiv.re&num=1&keyword=铃兰").build()).execute()
            val loliconResponse = Json.decodeFromString<LoliconResponse>(response.body!!.string())
            print(loliconResponse.toString())
        }
    }
}