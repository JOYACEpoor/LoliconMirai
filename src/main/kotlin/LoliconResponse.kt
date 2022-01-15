package nya.xfy

import kotlinx.serialization.Serializable

@Serializable
data class LoliconResponse(val error: String, val data: List<Data>) {
    @Serializable
    data class Data(
        val pid: Int,
        val p: Int,
        val uid: Int,
        val title: String,
        val author: String,
        val r18: Boolean,
        val width: Int,
        val height: Int,
        val tags: MutableList<String>,
        val ext: String,
        val uploadDate: Long,
        val urls: Urls
    ) {
        @Serializable
        data class Urls(val original: String)
    }
}