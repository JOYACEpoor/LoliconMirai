package nya.xfy

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
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.RawForwardMessage
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import nya.xfy.LoliconMiraiConfig.connectTimeout
import nya.xfy.LoliconMiraiConfig.readTimeout
import nya.xfy.LoliconMiraiConfig.writeTimeout
import nya.xfy.LoliconMiraiData.groupR18Map
import nya.xfy.LoliconMiraiData.groupSetuMap
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object LoliconMirai : KotlinPlugin(JvmPluginDescription(id = "nya.xfy.LoliconMirai", version = "1.5.1")) {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().readTimeout(readTimeout.toLong(), TimeUnit.SECONDS).writeTimeout(writeTimeout.toLong(), TimeUnit.SECONDS).connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS).build()

    override fun onEnable() {
        runBlocking {
            launch { LoliconMiraiConfig.reload() }
            launch { LoliconMiraiData.reload() }
        }
        launch { functionListener() }
        launch { managerListener() }
    }

    override fun onDisable() {
        launch { okHttpClient.dispatcher.executorService.shutdown() }
        launch{ okHttpClient.connectionPool.evictAll() }
        launch { okHttpClient.cache?.close() }
        launch { super.onDisable() }
    }

    private fun functionListener() {
        this.globalEventChannel().subscribeGroupMessages {
            matching(Regex("""来(\d*)张(.*)色图""")) {
                when (groupSetuMap[subject.id]) {
                    true -> {
                        when ((it.groupValues[1].toIntOrNull() ?: 1) in 1..50) {
                            true -> {
                                when (LoliconMiraiConfig.recallTime in 1..120) {
                                    true -> request(subject, bot, it.groupValues[1].toIntOrNull() ?: 1, it.groupValues[2]).takeIf { it1 -> it1.nodeList.isNotEmpty() }?.let { it2 -> subject.sendMessage(it2).recallIn(LoliconMiraiConfig.recallTime.toLong() * 1000) }
                                    else -> request(subject, bot, it.groupValues[1].toIntOrNull() ?: 1, it.groupValues[2]).takeIf { it1 -> it1.nodeList.isNotEmpty() }?.let { it2 -> subject.sendMessage(it2) }
                                }
                            }
                            else -> subject.sendMessage("不可以！！！")
                        }
                    }
                    else -> subject.sendMessage("不可以色色！")
                }
            }
        }
    }

    private fun managerListener() {
        this.globalEventChannel().subscribeGroupMessages {
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
        try {
            when (response.isSuccessful) {
                true -> {
                    val loliconResponse: LoliconResponse = Json.decodeFromString(response.body!!.string())
                    when (loliconResponse.error == "") {
                        true -> {
                                when (loliconResponse.data.isNotEmpty()) {
                                    true -> {
                                        runBlocking {
                                            launch {
                                                when(keyword==""){
                                                    true -> logger.info("正在获取[${num}]张色图")
                                                    else -> logger.info("正在获取[${num}]张${mode}=[${keyword}]的色图")
                                                }
                                            }
                                            launch {
                                                if (loliconResponse.data.size < num) mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +PlainText("关于[${keyword}]的图片只有${loliconResponse.data.size}张") }))
                                            }
                                            for (item in loliconResponse.data) {
                                                launch {
                                                    val response = okHttpClient.newCall(Request.Builder().url(item.urls.original).build()).execute()
                                                    when (response.isSuccessful) {
                                                        true -> mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +subject.uploadImage(response.body!!.byteStream().toExternalResource().toAutoCloseable()) }))
                                                        else -> mutableList.add(ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +PlainText("哎呀，图片失踪了\n${item.urls.original}") }))
                                                    }
                                                    response.close()
                                                }
                                            }
                                        }
                                        logger.info("${num}张${keyword}色图发送完毕")
                                    }
                                    else -> {
                                        when (mode=="tag") {
                                            true -> request(subject, bot,  num, keyword,"keyword")
                                            else -> subject.sendMessage("你的xp好怪。。。")
                                        }
                                    }
                                }
                            }
                            else -> subject.sendMessage("api出错了，待会在试试？api错误信息${loliconResponse.error}")
                        }
                    }
                    else -> subject.sendMessage("无法连接到LoliconApi，请检查网络问题")
                }
            } catch (e: IllegalStateException) {
            subject.sendMessage("发送${keyword}色图失败了，再试试吧？")
        } catch (e: SocketTimeoutException) {
            subject.sendMessage("请求${keyword}色图时超时了，等等再试试吧？")
        } catch (e: SocketException) {
            subject.sendMessage("请求${keyword}色图时连接出错了，等等再试试吧？")
        } catch (e: Throwable) {
            subject.sendMessage("哎呀，出错了。。。")
            logger.error(e)
        } finally {
            response.close()
        }
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
