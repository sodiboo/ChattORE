package listener

import ChattORE
import formatGlobal
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.event.TabCompleteEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class ChatListener(
    val chattORE: ChattORE
) : Listener {
    @EventHandler
    fun onTabComplete(event: TabCompleteEvent) {
        // Cancels propagated command suggestions from downstream servers
        if (
            event.cursor.matches(
                Regex("""/(msg|message|vmsg|vmessage|whisper|tell)\s.*\s.*""")
            ) // Cancels all direct message suggestions after a recipient is specified
            || event.cursor.matches(
                Regex("""/(me|helpop|ac|reply|r)\s.*""")
            ) // Cancels all suggestions
        ) {
            event.isCancelled = true
        }
    }
    @EventHandler
    fun onChatEvent(event: ChatEvent) {
        if (event.isCommand || event.isProxyCommand) {
            chattORE.sendPrivileged(
                *chattORE.messaging.format.command_spy.formatGlobal(
                    sender = (event.sender as ProxiedPlayer).displayName,
                    message = event.message,
                    preserveRawMessage = true
                )
            )
        } else {
            if (event.sender !is ProxiedPlayer) return
            event.isCancelled = true
            chattORE.broadcastChatMessage((event.sender as ProxiedPlayer).uniqueId, event.message)
        }
    }
}