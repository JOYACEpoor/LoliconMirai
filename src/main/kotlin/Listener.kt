package nya.xfy

import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.MessageEvent

fun listenerRegister() {
    GlobalEventChannel.subscribeAlways<MessageEvent> {
        if (message.contentToString().contains("[来]\\d*[张].*[色图]".toRegex())) {
            Miraisetuplugin.logger.info(
                "Keyword:" + message.contentToString().replace("[来]\\d*[张]".toRegex(), "").replace("[色图]".toRegex(), "")
            )
            Miraisetuplugin.logger.info(
                "Nums:" + message.contentToString().replace("[来]".toRegex(), "").replace("[张].*[色图]".toRegex(), "")
            )
            val setu = Requester(subject)
            setu.request(
                message.contentToString().replace("[来]\\d*[张]".toRegex(), "").replace("[色图]".toRegex(), ""),
                message.contentToString().replace("[来]".toRegex(), "").replace("[张].*[色图]".toRegex(), "")
            )
        }
    }
}