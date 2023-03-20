package chattore.listener

import chattore.ChattORE
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.player.TabCompleteEvent
import com.velocitypowered.api.proxy.Player
import chattore.entity.ChattORESpec
import chattore.formatGlobal

class ChatListener(
    private val chattORE: ChattORE
) {
    @Subscribe
    fun onTabComplete(event: TabCompleteEvent) {
        // TODO: Autocomplete player names and stuff idk
        event.suggestions.clear()
    }

    @Subscribe
    fun onChatEvent(event: PlayerChatEvent) {
        event.result = PlayerChatEvent.ChatResult.denied()

        val pp = event.player
        pp.currentServer.ifPresent { server ->
            chattORE.logger.info("${pp.username}: ${event.message}")
            chattORE.broadcastChatMessage(server.serverInfo.name, pp.uniqueId, event.message)
        }
    }

    @Subscribe
    fun onCommandEvent(event: CommandExecuteEvent) {
        chattORE.sendPrivileged(
            chattORE.config[ChattORESpec.format.commandSpy].formatGlobal(
                sender = (event.commandSource as? Player)?.username ?: "Console",
                message = event.command,
                preserveRawMessage = true
            )
        )
    }
}