package nya.xfy

import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import nya.xfy.LoliconMiraiConfig.botOwnerId
import nya.xfy.LoliconMiraiConfig.command
import nya.xfy.LoliconMiraiConfig.connectionFailureReply
import nya.xfy.LoliconMiraiConfig.noMatchResultReply
import nya.xfy.LoliconMiraiConfig.proxyAddress
import nya.xfy.LoliconMiraiConfig.r18OffCommand
import nya.xfy.LoliconMiraiConfig.r18OffReply
import nya.xfy.LoliconMiraiConfig.r18OnCommand
import nya.xfy.LoliconMiraiConfig.r18OnReply
import nya.xfy.LoliconMiraiConfig.recallTime
import nya.xfy.LoliconMiraiConfig.refuseReply
import nya.xfy.LoliconMiraiConfig.setuOffCommand
import nya.xfy.LoliconMiraiConfig.setuOffReply
import nya.xfy.LoliconMiraiConfig.setuOnCommand
import nya.xfy.LoliconMiraiConfig.setuOnReply
import nya.xfy.LoliconMiraiConfig.startSearchingReply
import nya.xfy.LoliconMiraiData.apiAddress
import nya.xfy.LoliconMiraiData.groupR18Map
import nya.xfy.LoliconMiraiData.groupSetuMap
import okhttp3.*
import java.util.concurrent.*

object LoliconMirai : KotlinPlugin(JvmPluginDescription(id = "nya.xfy.LoliconMirai", version = "1.5.3")), CoroutineScope {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().readTimeout(0, TimeUnit.SECONDS).writeTimeout(0, TimeUnit.SECONDS).connectTimeout(0, TimeUnit.SECONDS).build()

    override fun onEnable() {
        runBlocking {
            launch { LoliconMiraiConfig.reload() }
            launch { LoliconMiraiData.reload() }
        }
        listener()
    }

    private fun listener() {
        this.globalEventChannel().subscribeGroupMessages {
            finding(Regex(command)) {
                when (groupSetuMap[subject.id]) {
                    true -> {
                        sendMessage(subject, startSearchingReply)
                        logger.info("正在获取${it.groupValues[1]}色图")
                        sendSetu(subject, bot, it.groupValues[1])
                        logger.info("${it.groupValues[1]}色图发送完毕")
                    }
                    else -> subject.sendMessage(refuseReply)
                }
            }
        }
        this.globalEventChannel().subscribeGroupMessages(priority = EventPriority.MONITOR) {
            case(setuOnCommand, ignoreCase = true, trim = true) {
                if (getPermission(sender)) {
                    groupSetuMap[subject.id] = true
                    sendMessage(subject, setuOnReply)
                }
            }
            case(setuOffCommand, ignoreCase = true, trim = true) {
                if (getPermission(sender)) {
                    groupSetuMap[subject.id] = false
                    sendMessage(subject, setuOffReply)
                }
            }
            case(r18OnCommand, ignoreCase = true, trim = true) {
                if (getPermission(sender)) {
                    groupR18Map[subject.id] = 2
                    sendMessage(subject, r18OnReply)
                }
            }
            case(r18OffCommand, ignoreCase = true, trim = true) {
                if (getPermission(sender)) {
                    groupR18Map[subject.id] = 0
                    sendMessage(subject, r18OffReply)
                }
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun sendSetu(subject: Group, bot: Bot, keyword: String, mode: String = "tag") {
        val mutableList = mutableListOf<ForwardMessage.Node>()
        val response = okHttpClient.newCall(Request.Builder().url("${apiAddress}?r18=${groupR18Map[subject.id]}&proxy=${proxyAddress}&num=${(1..10).random()}&${mode}=${keyword}").build()).execute()
        when (response.isSuccessful) {
            true -> {
                val loliconResponse: LoliconResponse = Json.decodeFromString(response.body!!.string())
                when (loliconResponse.error == "" && loliconResponse.data.isNotEmpty()) {
                    true -> {
                        supervisorScope {
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
                        else -> sendMessage(subject, noMatchResultReply)
                    }
                }
            }
            else -> sendMessage(subject, connectionFailureReply)
        }
        response.close()
        subject.sendMessage(RawForwardMessage(mutableList).render(object : ForwardMessage.DisplayStrategy {
            override fun generateTitle(forward: RawForwardMessage) = when (keyword == "") {
                true -> "群聊的聊天记录"
                else -> keyword
            }
        })).takeIf { recallTime in 1..120 }?.recallIn((recallTime * 1000).toLong())
    }

    private fun getPermission(sender: Member): Boolean =
        sender.permission > MemberPermission.MEMBER || botOwnerId.contains(sender.id)

    private suspend fun sendMessage(subject: Group, message: String) =
        message.takeIf { message != "" }?.let { subject.sendMessage(message) }

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
