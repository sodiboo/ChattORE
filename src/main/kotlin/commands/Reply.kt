package commands

import ChattoreException
import Messaging
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import formatGlobal
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

@CommandAlias("r|reply")
@CommandPermission("chattore.reply")
class Reply(
    private val messaging: Messaging,
    private val proxy: ProxyServer,
    private val replyMap: MutableMap<UUID, UUID>
) : BaseCommand() {

    @Default
    fun default(player: ProxiedPlayer, args: Array<String>) {
        val target = proxy.getPlayer(
            replyMap[player.uniqueId] ?: throw ChattoreException(
                "You have no one to reply to!"
            )
        ) ?: throw ChattoreException(
            "The person you are trying to reply to is no longer online!"
        )
        player.sendMessage(
            *messaging.format.message_sent.formatGlobal(
                recipient = target.displayName,
                message = args.joinToString(" ")
            )
        )
        target.sendMessage(
            *messaging.format.message_received.formatGlobal(
                sender = player.displayName,
                message = args.joinToString(" ")
            )
        )
        replyMap[target.uniqueId] = player.uniqueId
    }
}