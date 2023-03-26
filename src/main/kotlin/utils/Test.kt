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
            val response = directClient.newCall(Request.Builder().url("https://api.lolicon.app/setu/v2?r18=2&proxy=i.pixiv.re&num=1&tag=%E9%93%83%E5%85%B0").build()).execute()
            val loliconResponse = Json.decodeFromString<LoliconResponse>(response.body!!.string())
            print(loliconResponse.toString())
        }
    }
}