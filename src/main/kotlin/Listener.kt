package nya.xfy

import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages

fun listenerRegister() {
    Miraisetuplugin.globalEventChannel().subscribeMessages {
        finding(Regex("""来(\d)*张(.*)色图""")){
            val number = it.groups[1]?.value?.toIntOrNull() ?: 1
            if(it.groups[2] != null) Miraisetuplugin.logger.info(
                "Keyword: ${it.groupValues[2]}}"
            )
            Miraisetuplugin.logger.info(
                "Nums: ${it.groupValues[1]}"
            )
            val setu = Requester(subject)
            setu.request(
                it.groups[2]?.value ?: "",
                number
            )
        }
    }
}