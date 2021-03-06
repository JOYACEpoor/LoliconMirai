package nya.xfy

import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import nya.xfy.commands.Getter
import nya.xfy.commands.GetterWithKeyword
import nya.xfy.commands.Manager
import nya.xfy.configs.CommandConfig
import nya.xfy.configs.NetworkConfig
import nya.xfy.configs.RecallConfig
import nya.xfy.configs.ReplyConfig
import nya.xfy.datas.Data
import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object LoliconMirai : KotlinPlugin(JvmPluginDescription(id = "nya.xfy.LoliconMirai", version = "1.6.5")) {

    lateinit var customClient: OkHttpClient
    lateinit var directClient:OkHttpClient

    override fun onEnable() {
        reloader()
        init()
        commandRegister()
    }

    private fun reloader(){
        ReplyConfig.reload()
        RecallConfig.reload()
        Data.reload()
        CommandConfig.reload()
        NetworkConfig.reload()
    }

    private fun commandRegister(){
        CommandManager.registerCommand(Getter)
        CommandManager.registerCommand(GetterWithKeyword)
        CommandManager.registerCommand(Manager)
    }

    private fun init(){
        customClient = OkHttpClient.Builder().apply {
            this.connectTimeout(NetworkConfig.connectTimeout, TimeUnit.SECONDS)
            this.callTimeout(NetworkConfig.callTimeout, TimeUnit.SECONDS)
            this.readTimeout(NetworkConfig.readTimeout, TimeUnit.SECONDS)
            this.writeTimeout(NetworkConfig.writeTimeout, TimeUnit.SECONDS)
            if (NetworkConfig.proxySwitch) {
                this.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(NetworkConfig.proxyAddress, NetworkConfig.proxyPort)))
                log("色图代理已开启！", false)
                log("代理地址：${NetworkConfig.proxyAddress}", false)
                log("代理端口：${NetworkConfig.proxyPort}", false)
            } else
                log("色图代理未开启！", false)
        }.build()
        directClient = OkHttpClient.Builder().apply {
            this.connectTimeout(NetworkConfig.connectTimeout, TimeUnit.SECONDS)
            this.callTimeout(NetworkConfig.callTimeout, TimeUnit.SECONDS)
            this.readTimeout(NetworkConfig.readTimeout, TimeUnit.SECONDS)
            this.writeTimeout(NetworkConfig.writeTimeout, TimeUnit.SECONDS)
        }.build()
    }

    internal fun log(msg: String, verbose: Boolean = true) {
        if (verbose && !CommandConfig.verbose) {
            return
        }
        logger.info(msg)
    }
}
