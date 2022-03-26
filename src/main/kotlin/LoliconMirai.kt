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

object LoliconMirai : KotlinPlugin(JvmPluginDescription(id = "nya.xfy.LoliconMirai", version = "1.6.3")) {
    lateinit var okHttpClient: OkHttpClient

    override fun onEnable() {
        ReplyConfig.reload()
        RecallConfig.reload()
        Data.reload()
        CommandConfig.reload()
        NetworkConfig.reload()
        okHttpClient = OkHttpClient.Builder().apply {
            this.connectTimeout(NetworkConfig.connectTimeout, TimeUnit.SECONDS)
            this.callTimeout(NetworkConfig.callTimeout, TimeUnit.SECONDS)
            this.readTimeout(NetworkConfig.readTimeout, TimeUnit.SECONDS)
            this.writeTimeout(NetworkConfig.writeTimeout, TimeUnit.SECONDS)
            if (NetworkConfig.proxySwitch) {
                this.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(NetworkConfig.proxyAddress, NetworkConfig.proxyPort)))
                logger.info("色图代理已开启！")
                logger.info("代理地址：${NetworkConfig.proxyAddress}")
                logger.info("代理端口：${NetworkConfig.proxyPort}")
            } else
                logger.info("色图代理未开启！")
        }.build()
        CommandManager.registerCommand(Getter)
        CommandManager.registerCommand(GetterWithKeyword)
        CommandManager.registerCommand(Manager)
    }
}
