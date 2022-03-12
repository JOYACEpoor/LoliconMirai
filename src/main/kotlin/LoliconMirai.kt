package nya.xfy

import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import nya.xfy.commands.Getter
import nya.xfy.commands.GetterWithKeyword
import nya.xfy.commands.Manager
import nya.xfy.configs.CommandConfig
import nya.xfy.configs.RecallConfig
import nya.xfy.configs.ReplyConfig
import nya.xfy.datas.Data

object LoliconMirai : KotlinPlugin(JvmPluginDescription(id = "nya.xfy.loliconmirai", version = "1.6.1")) {
    override fun onEnable() {
        ReplyConfig.reload()
        RecallConfig.reload()
        Data.reload()
        CommandConfig.reload()
        CommandManager.registerCommand(Getter)
        CommandManager.registerCommand(GetterWithKeyword)
        CommandManager.registerCommand(Manager)
    }
}
