package chattore.listener

import chattore.ChattORE
import chattore.entity.ChattORESpec
import chattore.render
import chattore.toComponent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.event.player.TabCompleteEvent
import com.velocitypowered.api.proxy.Player
import java.util.concurrent.TimeUnit

class ChatListener(
    private val chattORE: ChattORE
) {
    @Subscribe
    fun onTabComplete(event: TabCompleteEvent) {
        // TODO: Autocomplete player names and stuff idk
        event.suggestions.clear()
    }

    @Subscribe
    fun onJoin(event: ServerPreConnectEvent) {
        chattORE.database.ensureCachedUsername(
            event.player.uniqueId,
            event.player.username
        )
    }

    @Subscribe
    fun joinEvent(event: LoginEvent) {
        val unreadCount = chattORE.database.getMessages(event.player.uniqueId).filter { !it.read }.size
        if (unreadCount > 0)
            chattORE.proxy.scheduler.buildTask(chattORE, Runnable {
                event.player.sendMessage(chattORE.config[ChattORESpec.format.mailUnread].render(mapOf(
                    "count" to "$unreadCount".toComponent()
                )))
            })
                .delay(2L, TimeUnit.SECONDS)
                .schedule()
        if (!chattORE.config[ChattORESpec.clearNicknameOnChange]) return
        val existingName = chattORE.database.uuidToUsernameCache[event.player.uniqueId] ?: return
        if (existingName == event.player.username) return
        chattORE.database.removeNickname(event.player.uniqueId)
    }

    @Subscribe
    fun joinMessage(event: ServerPostConnectEvent) {
        if (event.player.uniqueId in chattORE.onlinePlayers) return
        chattORE.onlinePlayers.add(event.player.uniqueId)
        val username = event.player.username
        chattORE.broadcast(
            chattORE.config[ChattORESpec.format.join].render(mapOf(
                "player" to username.toComponent()
            ))
        )
        chattORE.broadcastPlayerConnection(
            chattORE.config[ChattORESpec.format.joinDiscord].replace(
                "<player>",
                username
            )
        )
    }

    @Subscribe
    fun leaveMessage(event: DisconnectEvent) {
        if (event.player.uniqueId !in chattORE.onlinePlayers) return
        chattORE.onlinePlayers.remove(event.player.uniqueId)
        val username = event.player.username
        chattORE.broadcast(
            chattORE.config[ChattORESpec.format.leave].render(mapOf(
                "player" to username.toComponent()
            ))
        )
        chattORE.broadcastPlayerConnection(
            chattORE.config[ChattORESpec.format.leaveDiscord].replace(
                "<player>",
                username
            )
        )
    }

    @Subscribe
    fun onChatEvent(event: PlayerChatEvent) {
        val pp = event.player
        pp.currentServer.ifPresent { server ->
            chattORE.logger.info("${pp.username} (${pp.uniqueId}): ${event.message}")
            var result = event.message
            if (event.message.contains("&k") && !pp.hasPermission("chattore.chat.obfuscate")) {
                pp.sendMessage(chattORE.config[ChattORESpec.format.error].render(mapOf(
                    "message" to "You do not have permission to obfuscate text!".toComponent()
                )))
                result = event.message.replace("&k", "")
            }
            chattORE.broadcastChatMessage(server.serverInfo.name, pp.uniqueId, result)
        }
    }

    @Subscribe
    fun onCommandEvent(event: CommandExecuteEvent) {
        chattORE.sendPrivileged(
            chattORE.config[ChattORESpec.format.commandSpy].render(
                mapOf(
                    "message" to event.command.toComponent(),
                    "sender" to ((event.commandSource as? Player)?.username ?: "Console").toComponent()
                )
            )
        )
    }
}