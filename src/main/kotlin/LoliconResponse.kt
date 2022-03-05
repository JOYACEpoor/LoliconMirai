package nya.xfy

@kotlinx.serialization.Serializable
data class LoliconResponse(val error: String, val data: List<Data>) {
    @kotlinx.serialization.Serializable
    data class Data(
        val pid: Int,
        val p: Int,
        val uid: Int,
        val title: String,
        val author: String,
        val r18: Boolean,
        val width: Int,
        val height: Int,
        val tags: List<String>,
        val ext: String,
        val uploadDate: Long,
        val urls: Urls
    ) {
        @kotlinx.serialization.Serializable
        data class Urls(val original: String)
    }
}