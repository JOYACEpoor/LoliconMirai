package nya.xfy.utils

@kotlinx.serialization.Serializable
data class LoliconResponse(val code: Int, val msg: String, val count: Int, val data: List<Data>) {
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
        val url: String
    )
}