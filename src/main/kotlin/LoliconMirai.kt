package nya.xfy

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.RawForwardMessage
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import nya.xfy.LoliconMiraiData.groupR18Map
import nya.xfy.LoliconMiraiData.groupSetuMap
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object LoliconMirai : KotlinPlugin(JvmPluginDescription(id = "nya.xfy.LoliconMirai", version = "1.5.1")) {

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
        this.globalEventChannel().subscribeGroupMessages(priority = EventPriority.MONITOR) {
            matching(Regex("""来(\d*)张(.*)色图""")) {
                when (groupSetuMap[subject.id]) {
                    true -> {
                        when ((it.groupValues[1].toIntOrNull() ?: 1) in 1..50) {
                            true -> {
                                logger.info("正在获取${it.groupValues[2]}色图")
                                when (LoliconMiraiConfig.recallTime in 1..120) {
                                    true -> request(subject, bot, it.groupValues[1].toIntOrNull() ?: 1, it.groupValues[2]).takeIf { it1 -> it1.nodeList.isNotEmpty() }?.let { it2 -> subject.sendMessage(it2).recallIn(LoliconMiraiConfig.recallTime.toLong() * 1000) }
                                    else -> request(subject, bot, it.groupValues[1].toIntOrNull() ?: 1, it.groupValues[2]).takeIf { it1 -> it1.nodeList.isNotEmpty() }?.let { it2 -> subject.sendMessage(it2) }
                                }
                                logger.info("${it.groupValues[2]}色图发送完毕")
                            }
                            else -> subject.sendMessage("不可以！！！")
                        }
                    }
                    else -> subject.sendMessage("不可以色色！")
                }
            }
            case("开启r18", ignoreCase = true, trim = true) {
                if (sender.permission > MemberPermission.MEMBER || LoliconMiraiConfig.botOwnerId.contains(sender.id)) {
                    groupR18Map[subject.id] = 2
                    subject.sendMessage("已开启r18")
                }
            }
            case("关闭r18", ignoreCase = true, trim = true) {
                if (sender.permission > MemberPermission.MEMBER || LoliconMiraiConfig.botOwnerId.contains(sender.id)) {
                    groupR18Map[subject.id] = 0
                    subject.sendMessage("已关闭r18")
                }
            }
            case("开启色图", ignoreCase = true, trim = true) {
                if (sender.permission > MemberPermission.MEMBER || LoliconMiraiConfig.botOwnerId.contains(sender.id)) {
                    groupSetuMap[subject.id] = true
                    subject.sendMessage("已开启色图")
                }
            }
            case("关闭色图", ignoreCase = true, trim = true) {
                if (sender.permission > MemberPermission.MEMBER || LoliconMiraiConfig.botOwnerId.contains(sender.id)) {
                    groupSetuMap[subject.id] = false
                    subject.sendMessage("已关闭色图")
                }
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun request(subject: Group, bot: Bot, num: Int, keyword: String, mode: String = "tag"): ForwardMessage {
        val mutableList = mutableListOf<ForwardMessage.Node>()
        val response = okHttpClient.newCall(Request.Builder().url("https://api.lolicon.app/setu/v2?r18=${groupR18Map[subject.id]}&proxy=i.pixiv.re&num=${num}&${mode}=${keyword}").build()).execute()
        when (response.isSuccessful) {
            true -> {
                val loliconResponse: LoliconResponse = Json.decodeFromString(response.body!!.string())
                when (loliconResponse.error == "" && loliconResponse.data.isNotEmpty()) {
                    true -> {
                        if (loliconResponse.data.size < num) mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +"关于[${keyword}]的图片只有${loliconResponse.data.size}张" }))
                        try {
                            coroutineScope {
                                for (item in loliconResponse.data) {
                                    launch {
                                        val response = okHttpClient.newCall(Request.Builder().url(item.urls.original).build()).execute()
                                        when (response.isSuccessful) {
                                            true -> mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +response.body!!.byteStream().toExternalResource().toAutoCloseable().uploadAsImage(subject) }))
                                            else -> mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +"哎呀，图片失踪了\n${item.urls.original}" }))
                                        }
                                        response.close()
                                    }
                                }
                            }
                        } catch (e: IllegalStateException) {
                            subject.sendMessage("上传部分${keyword}图片失败了，再试试吧？")
                        } catch (e: SocketTimeoutException) {
                            subject.sendMessage("请求${keyword}色图时超时了，等等再试试吧？")
                        } catch (e: SocketException) {
                            subject.sendMessage("请求${keyword}色图时连接出错了，等等再试试吧？")
                        } catch (e: Throwable) {
                            subject.sendMessage("哎呀，出错了。。。")
                            logger.error(e)
                        }
                    }
                    else -> when (mode == "tag") {
                        true -> request(subject, bot, num, keyword, "keyword")
                        else -> subject.sendMessage("你的xp好怪。。。")
                    }
                }
            }
            else -> subject.sendMessage("无法连接到LoliconApi")
        }
        response.close()
        return RawForwardMessage(mutableList).render(object : ForwardMessage.DisplayStrategy {
            override fun generateTitle(forward: RawForwardMessage): String {
                return when (keyword == "") {
                    true -> "群聊的聊天记录"
                    else -> keyword
                }
            }
        })
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
