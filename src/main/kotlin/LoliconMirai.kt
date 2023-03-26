package nya.xfy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription.Companion.loadFromResource
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import nya.xfy.commands.Command1
import nya.xfy.commands.Command2
import nya.xfy.commands.Manager
import nya.xfy.configs.CommandConfig
import nya.xfy.configs.NetworkConfig
import nya.xfy.configs.RecallConfig
import nya.xfy.configs.ReplyConfig
import nya.xfy.datas.Data
import nya.xfy.utils.LoliconResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object LoliconMirai : KotlinPlugin(loadFromResource()) {

    private lateinit var client: OkHttpClient

    override fun onEnable() {
        init()
    }

    private fun init(){
        //reloadConfig
        ReplyConfig.reload()
        RecallConfig.reload()
        Data.reload()
        CommandConfig.reload()
        NetworkConfig.reload()
        //registerCommand
        CommandManager.registerCommand(Command1)
        CommandManager.registerCommand(Command2)
        CommandManager.registerCommand(Manager)
        //initHttpClient
        client = OkHttpClient.Builder().apply {
            this.connectTimeout(NetworkConfig.connectTimeout, TimeUnit.SECONDS)
            this.callTimeout(NetworkConfig.callTimeout, TimeUnit.SECONDS)
            this.readTimeout(NetworkConfig.readTimeout, TimeUnit.SECONDS)
            this.writeTimeout(NetworkConfig.writeTimeout, TimeUnit.SECONDS)
            if (NetworkConfig.proxySwitch) {
                this.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(NetworkConfig.proxyAddress, NetworkConfig.proxyPort)))
                logInfo("色图代理已开启！", false)
                logInfo("代理地址：${NetworkConfig.proxyAddress}", false)
                logInfo("代理端口：${NetworkConfig.proxyPort}", false)
            } else
                logInfo("色图代理未开启！", false)
        }.build()
        //initNudgeListener
        GlobalEventChannel.subscribe<NudgeEvent> {
            if(target == this.bot){
                LoliconMirai.launch {
                    getSetu(1, "", subject, bot)
                }
            }
            ListeningStatus.LISTENING
        }
    }
    internal fun logInfo(msg: String, verbose: Boolean = true) {
        if (verbose && !CommandConfig.verbose) {
            return
        }
        logger.info(msg)
    }
    internal fun logWarning(msg: String, verbose: Boolean = true) {
        if (verbose && !CommandConfig.verbose) {
            return
        }
        logger.warning(msg)
    }
    internal fun logError(msg: String, verbose: Boolean = true) {
        if (verbose && !CommandConfig.verbose) {
            return
        }
        logger.error(msg)
    }

    private fun getLoliconResponse(str: String) = Json.decodeFromString<LoliconResponse>(str)
    private fun getForwardMessageNode(bot: Bot, message: Message): ForwardMessage.Node = ForwardMessage.Node(bot.id, 0, bot.nameCardOrNick, buildMessageChain { +message })

    suspend fun getSetu(amount: Int = 1, keyword: String = "", subject: Contact,bot: Bot, mode: String = "tag") {
        if (amount !in (1..20)) {
            subject.sendMessage(ReplyConfig.invalidAmountInput)
            return
        }
        if (Data.groupSetuMap[subject.id] == null && Data.groupSetuMap[subject.id] == false) {
            subject.sendMessage(ReplyConfig.refuseReply)
            return
        }
        try {
            val response = client.newCall(Request.Builder().url("https://api.lolicon.app/setu/v2?r18=${Data.groupR18Map[subject.id]}&proxy=${NetworkConfig.reverseProxyLink}&num=1&${mode}=${keyword.replace("+", "&${mode}=")}").build()).execute()
            if (response.isSuccessful) {
                logInfo("解析中\n${response.request}")
                val loliconResponse: LoliconResponse = getLoliconResponse(response.body!!.string())
                if (loliconResponse.data.isNotEmpty()) {
                    val actualAmount: Int
                    subject.sendMessage(RawForwardMessage(responseHandler(subject, bot, loliconResponse).also {
                            actualAmount = it.size
                            if (actualAmount < amount) {
                                it.add(0, getForwardMessageNode(bot, PlainText(ReplyConfig.amountNotReach.replace("<Keyword>", keyword).replace("<ActualAmount>", actualAmount.toString()))))
                            }
                        }).render(object : ForwardMessage.DisplayStrategy {
                            override fun generateTitle(forward: RawForwardMessage) = "${actualAmount}张${if (keyword.isEmpty()) "色图" else " $keyword"}"
                        })).takeIf { RecallConfig.recallTime in 1..120 }?.recallIn((RecallConfig.recallTime * 1000).toLong())
                }else {
                    when (mode) {
                        "tag" -> this.getSetu(amount, keyword, subject, bot,"keyword")
                        "keyword" -> when (loliconResponse.error == "") {
                            true -> subject.sendMessage(ReplyConfig.noResultReply)
                            else -> subject.sendMessage(loliconResponse.error)
                        }
                        else -> subject.sendMessage(ReplyConfig.exceptionReply)
                    }
                }
            }else {
                subject.sendMessage(ReplyConfig.connectionFailureReply)
            }
            response.close()
        } catch (e: IOException) {
            subject.sendMessage(ReplyConfig.exceptionReply.replace("<Exception>", e.toString()))
        }
    }

    private suspend fun responseHandler(subject: Contact,bot: Bot, loliconResponse: LoliconResponse): MutableList<ForwardMessage.Node> {
        return mutableListOf<ForwardMessage.Node>().apply {
            supervisorScope {
                launch(Dispatchers.IO) { subject.sendMessage(ReplyConfig.startSearchingReply) }
                for (item in loliconResponse.data) {
                    launch(Dispatchers.IO) {
                        val response = client.newCall(Request.Builder().url(item.urls.original).header("referer", "https://www.pixiv.net/").build()).execute()
                        logInfo("PID: ${item.pid}获取中")
                        try {
                            when (response.isSuccessful) {
                                true -> this@apply.add(getForwardMessageNode(bot,subject.uploadImage(response.body!!.byteStream().toExternalResource().toAutoCloseable())))
                                else -> this@apply.add(getForwardMessageNode(bot, PlainText("哎呀，图片失踪了\n${item.urls}")))
                            }
                        } catch (e: Exception) {
                            this@apply.add(getForwardMessageNode(bot,PlainText("哎呀，图片失踪了\n${e}\n${item.urls}")))
                        } finally {
                            response.close()
                        }
                        logInfo("PID: ${item.pid}上传完毕")
                    }
                }
            }
        }
    }
}
