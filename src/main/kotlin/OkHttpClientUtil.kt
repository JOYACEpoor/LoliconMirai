package nya.xfy

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


internal fun initOkHttpClient():OkHttpClient=OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS).connectTimeout(30, TimeUnit.SECONDS).build()

internal fun closeOkHttpClient(okHttpClient:OkHttpClient) {
    okHttpClient.dispatcher.executorService.shutdown()
    okHttpClient.connectionPool.evictAll()
    okHttpClient.cache?.close()
}