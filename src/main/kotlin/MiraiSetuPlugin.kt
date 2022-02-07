package nya.xfy

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
import net.mamoe.mirai.utils.info
import nya.xfy.MiraiSetuPluginConfig.botOwnerId
import nya.xfy.MiraiSetuPluginConfig.recallTime
import nya.xfy.MiraiSetuPluginData.groupR18Map
import nya.xfy.MiraiSetuPluginData.groupSetuMap
import nya.xfy.MiraiSetuPluginData.loliconApi
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.TimeUnit

object MiraiSetuPlugin : KotlinPlugin(JvmPluginDescription(id = "nya.xfy.miraisetuplugin", version = "1.5.0")) {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).writeTimeout(5, TimeUnit.SECONDS).connectTimeout(5, TimeUnit.SECONDS).build()

    override fun onEnable() {
        MiraiSetuPluginConfig.reload()
        MiraiSetuPluginData.reload()
        logger.info { "Plugin loaded" }
        groupSetuListener()
    }

    override fun onDisable() {
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
        okHttpClient.cache?.close()
        super.onDisable()
    }

    private fun groupSetuListener() {
        this.globalEventChannel().subscribeGroupMessages {
            matching(Regex("""来(\d*)张(.*)色图""")) {
                when (groupSetuMap[subject.id]==true) {
                    true -> {
                        when ((it.groupValues[1].toIntOrNull() ?: 1) in 1..5) {
                            true -> {
                                logger.info("正在获取${it.groupValues[1].toIntOrNull() ?: 1}张${it.groupValues[2]}色图")
                                when (recallTime) {
                                    in 1..120 -> request(subject, bot, it.groupValues[1].toIntOrNull() ?: 1,it.groupValues[2]).takeIf { it.nodeList.isNotEmpty() }?.let { subject.sendMessage(it).recallIn(recallTime.toLong() * 1000) }
                                    else -> request(subject, bot, it.groupValues[1].toIntOrNull() ?: 1,it.groupValues[2]).takeIf { it.nodeList.isNotEmpty() }?.let { subject.sendMessage(it) }
                                }
                                logger.info("${it.groupValues[1].toIntOrNull() ?: 1}张${it.groupValues[2]}色图发送完毕")
                            }
                            else -> subject.sendMessage("不可以！！！")
                        }
                    }
                    false -> subject.sendMessage("不可以色色！")
                }
            }
            case("开启r18", ignoreCase = true, trim = true) {
                if (sender.permission > MemberPermission.MEMBER || botOwnerId.contains(sender.id)) {
                    groupR18Map[subject.id] = 2
                    subject.sendMessage("已开启r18")
                }
            }
            case("关闭r18", ignoreCase = true, trim = true) {
                if (sender.permission > MemberPermission.MEMBER || botOwnerId.contains(sender.id)) {
                    groupR18Map[subject.id] = 0
                    subject.sendMessage("已关闭r18")
                }
            }
            case("开启色图", ignoreCase = true, trim = true) {
                if (sender.permission > MemberPermission.MEMBER || botOwnerId.contains(sender.id)) {
                    groupSetuMap[subject.id] = true
                    subject.sendMessage("已开启色图")
                }
            }
            case("关闭色图", ignoreCase = true, trim = true) {
                if (sender.permission > MemberPermission.MEMBER || botOwnerId.contains(sender.id)) {
                    groupSetuMap[subject.id] = false
                    subject.sendMessage("已关闭色图")
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun request(subject: Group, bot:Bot, num:Int, keyword:String, mode:String="tag"):ForwardMessage{
        val mutableList = mutableListOf<ForwardMessage.Node>()
        lateinit var response: okhttp3.Response
            try {
                response = okHttpClient.newCall(Request.Builder().url("${loliconApi}?r18=${groupR18Map[subject.id]}&proxy=i.pixiv.re&num=${num}&${mode}=${keyword}").build()).execute()
                logger.info("${response.request.url}")
                val loliconResponse: LoliconResponse = Json.decodeFromString(response.body!!.string())
                when (response.isSuccessful) {
                    true -> {
                        when (loliconResponse.error) {
                            "" -> {
                                when (loliconResponse.data.isEmpty()) {
                                    true -> {
                                        when (mode) {
                                            "tag" -> request(subject, bot,  num, keyword,"keyword")
                                            "keyword" -> subject.sendMessage("你的xp好怪。。。")
                                        }
                                    }
                                    else ->{
                                        for (item in loliconResponse.data) {
                                            response = okHttpClient.newCall(Request.Builder().url(item.urls.original).build()).execute()
                                            when (response.isSuccessful) {
                                                true -> mutableList.add(ForwardMessage.Node(bot.id, Date().time.toInt(), bot.nameCardOrNick, buildMessageChain { +subject.uploadImage(response.body!!.byteStream().toExternalResource().toAutoCloseable()) }))
                                                else -> mutableList.add(ForwardMessage.Node(bot.id, Date().time.toInt(),bot.nameCardOrNick, buildMessageChain { +PlainText("哎呀，图片失踪了\n${item.urls.original}") }))
                                            }
                                        }
                                        if (loliconResponse.data.lastIndex + 1 < num)
                                            subject.sendMessage("关于[${keyword}]的图片只有${loliconResponse.data.size}张")
                                    }
                                }
                            }
                            else -> subject.sendMessage("请求api时出错了，待会在试试？api错误信息${loliconResponse.error}")
                        }
                    }
                    else -> subject.sendMessage("请求api出错，请检查网络问题")
                }
            } catch (e: IllegalStateException) {
                subject.sendMessage("图片发送失败了，再试试看吧？")
            } catch (e: SocketTimeoutException) {
                subject.sendMessage("请求${keyword}色图时超时了，等等再试试吧？")
            } catch (e: SocketException) {
                subject.sendMessage("请求${keyword}色图时连接出错了，等等再试试吧？")
            } catch (e: Throwable) {
                subject.sendMessage("哎呀，出错了。。。")
                logger.error(e)
            }finally {
                response.close()
            }
        return RawForwardMessage(mutableList).render(object : ForwardMessage.DisplayStrategy { override fun generateTitle(forward: RawForwardMessage): String { return "${num}张${keyword}色图" } })
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
            val tags: MutableList<String>,
            val ext: String,
            val uploadDate: Long,
            val urls: Urls
        ) {
            @Serializable
            data class Urls(val original: String)
        }
    }
}
