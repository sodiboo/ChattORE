package commands

import ChattoreException
import Messaging
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import formatGlobal
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer

@CommandAlias("me")
@CommandPermission("chattore.me")
class Me(
    private val messaging: Messaging,
    private val proxy: ProxyServer
) : BaseCommand() {

    @Default
    @Syntax("[message]")
    fun default(player: ProxiedPlayer, args: Array<String>) {
        if (args.isEmpty()) throw ChattoreException("You have to &ohave&c a thonk first!")
        proxy.broadcast(
            *messaging.format.me.formatGlobal(
                sender = player.displayName,
                message = args.joinToString(" ")
            )
        )
    }
}
