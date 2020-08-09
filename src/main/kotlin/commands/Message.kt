package commands

import ChattoreException
import Messaging
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import formatGlobal
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

@CommandAlias("msg|message|vmsg|whisper|tell")
@CommandPermission("chattore.message")
class Message(
    private val messaging: Messaging,
    private val proxy: ProxyServer,
    private val replyMap: MutableMap<UUID, UUID>
) : BaseCommand() {

    @Default
    @Syntax("[player] [message]")
    @CommandCompletion("@players")
    fun default(player: ProxiedPlayer, args: Array<String>) {
        if (args.isEmpty()) throw ChattoreException("You have to have something &oto&c send to someone!")
        val target = proxy.getPlayer(args[0]) ?: throw ChattoreException("That user doesn't exist!")
        if (args.drop(1).isEmpty()) throw ChattoreException("Can't have an empty private message!")
        player.sendMessage(
            *messaging.format.message_sent.formatGlobal(
                recipient = target.displayName,
                message = args.drop(1).joinToString(" ")
            )
        )
        target.sendMessage(
            *messaging.format.message_received.formatGlobal(
                sender = player.displayName,
                message = args.drop(1).joinToString(" ")
            )
        )
        replyMap[target.uniqueId] = player.uniqueId
    }
}
