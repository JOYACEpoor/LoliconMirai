package nya.xfy

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

internal lateinit var okHttpClient: OkHttpClient

internal fun initOkHttpClient() {
    okHttpClient = OkHttpClient.Builder().let {
        it.readTimeout(30, TimeUnit.SECONDS)
        it.writeTimeout(30, TimeUnit.SECONDS)
        it.connectTimeout(30, TimeUnit.SECONDS)
        it.build()
    }
}

internal fun closeOkHttpClient() {
    okHttpClient.dispatcher.executorService.shutdown()
    okHttpClient.connectionPool.evictAll()
    okHttpClient.cache?.close()
}