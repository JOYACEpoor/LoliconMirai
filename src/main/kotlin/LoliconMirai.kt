package nya.xfy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.RawForwardMessage
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import nya.xfy.LoliconMiraiConfig.botOwnerId
import nya.xfy.LoliconMiraiConfig.recallTime
import nya.xfy.LoliconMiraiData.groupR18Map
import nya.xfy.LoliconMiraiData.groupSetuMap
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object LoliconMirai : KotlinPlugin(JvmPluginDescription(id = "nya.xfy.LoliconMirai", version = "1.5.3")), CoroutineScope {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().let {
        it.readTimeout(0, TimeUnit.SECONDS)
        it.writeTimeout(0, TimeUnit.SECONDS)
        it.connectTimeout(0, TimeUnit.SECONDS)
    }.build()

    override fun onEnable() {
        runBlocking {
            launch { LoliconMiraiConfig.reload() }
            launch { LoliconMiraiData.reload() }
        }
        listener()
    }

    private fun listener() {
        this.globalEventChannel().subscribeGroupMessages {
            finding(Regex("""来点(.*)色图""")) {
                when (groupSetuMap[subject.id]) {
                    true -> {
                        logger.info("正在获取${it.groupValues[1]}色图")
                        sendSetu(subject, bot, it.groupValues[1])
                        logger.info("${it.groupValues[1]}色图发送完毕")
                    }
                    else -> subject.sendMessage("不可以色色！")
                }
            }
        }
        this.globalEventChannel().subscribeGroupMessages(priority = EventPriority.MONITOR) {
            case("开启r18", ignoreCase = true, trim = true) {
                if(getPermission(sender)) {
                    groupR18Map[subject.id] = 2
                    subject.sendMessage("已开启r18")
                }
            }
            case("关闭r18", ignoreCase = true, trim = true) {
                if(getPermission(sender)) {
                    groupR18Map[subject.id] = 0
                    subject.sendMessage("已关闭r18")
                }
            }
            case("开启色图", ignoreCase = true, trim = true) {
                if(getPermission(sender)) {
                    groupSetuMap[subject.id] = true
                    subject.sendMessage("已开启色图")
                }
            }
            case("关闭色图", ignoreCase = true, trim = true) {
                if(getPermission(sender)) {
                    groupSetuMap[subject.id] = false
                    subject.sendMessage("已关闭色图")
                }
            }
        }
    }

    private fun getPermission(sender:Member):Boolean=sender.permission > MemberPermission.MEMBER || botOwnerId.contains(sender.id)

    @Suppress("NAME_SHADOWING")
    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun sendSetu(subject: Group, bot: Bot, keyword: String, mode: String = "tag") {
        val mutableList = mutableListOf<ForwardMessage.Node>()
        val response = okHttpClient.newCall(Request.Builder().url("https://api.lolicon.app/setu/v2?r18=${groupR18Map[subject.id]}&proxy=i.pixiv.re&num=${(1..10).random()}&${mode}=${keyword}").build()).execute()
        when (response.isSuccessful) {
            true -> {
                val loliconResponse: LoliconResponse = Json.decodeFromString(response.body!!.string())
                when (loliconResponse.error == "" && loliconResponse.data.isNotEmpty()) {
                    true -> {
                        supervisorScope  {
                            for (item in loliconResponse.data) {
                                launch {
                                    val response = okHttpClient.newCall(Request.Builder().url(item.urls.original).build()).execute()
                                    logger.info("PID: ${item.pid}获取中")
                                    when (response.isSuccessful) {
                                        true -> mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +subject.uploadImage(response.body!!.byteStream().toExternalResource().toAutoCloseable()) }))
                                        else -> mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +"哎呀，图片失踪了\n${item.urls.original}" }))
                                    }
                                    response.close()
                                    logger.info("PID: ${item.pid}上传完毕")
                                }
                            }
                        }
                    }
                    else -> when (mode == "tag") {
                        true -> sendSetu(subject, bot, keyword, "keyword")
                        else -> subject.sendMessage("你的xp好怪。。。")
                    }
                }
            }
            else -> subject.sendMessage("无法连接到LoliconApi")
        }
        response.close()
        subject.sendMessage(RawForwardMessage(mutableList).render(object : ForwardMessage.DisplayStrategy {
            override fun generateTitle(forward: RawForwardMessage) = when (keyword == "") {
                    true -> "群聊的聊天记录"
                    else -> keyword
                }
        })).takeIf { recallTime in 1..120 }?.recallIn((recallTime*1000).toLong())
    }

    @Serializable
    private data class LoliconResponse(val error: String, val data: List<Data>) {
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
            val tags: List<String>,
            val ext: String,
            val uploadDate: Long,
            val urls: Urls
        ) {
            @Serializable
            data class Urls(val original: String)
        }
    }
}
