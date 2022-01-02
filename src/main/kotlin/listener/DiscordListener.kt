package chattore.listener

import chattore.ChattORE
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener

class DiscordListener(
    private val chattORE: ChattORE
) : MessageCreateListener {
    override fun onMessageCreate(event: MessageCreateEvent) {
        if (event.messageAuthor.isBotUser) return
        chattORE.broadcastDiscordMessage(event.messageAuthor.displayName, event.messageContent)
    }
}
