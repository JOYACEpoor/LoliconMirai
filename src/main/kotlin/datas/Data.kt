package nya.xfy.datas

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object Data : AutoSavePluginData("data") {
    val okHttpClient:OkHttpClient=OkHttpClient.Builder().apply {
        this.connectTimeout(0,TimeUnit.SECONDS)
        this.callTimeout(0,TimeUnit.SECONDS)
        this.readTimeout(0,TimeUnit.SECONDS)
        this.writeTimeout(0,TimeUnit.SECONDS)
    }.build()
    val groupSetuMap: MutableMap<Long, Boolean> by value()
    val groupR18Map: MutableMap<Long, Int> by value()
}