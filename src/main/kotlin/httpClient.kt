package nya.xfy

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import java.util.concurrent.TimeUnit

val httpClient = HttpClient(OkHttp.config {
    config {
        connectTimeout(30,TimeUnit.SECONDS)
        writeTimeout(30,TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        retryOnConnectionFailure(true)
    }
})
