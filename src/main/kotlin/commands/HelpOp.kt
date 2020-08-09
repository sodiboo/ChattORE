package commands

import ChattORE
import ChattoreException
import Messaging
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import formatGlobal
import net.md_5.bungee.api.connection.ProxiedPlayer

@CommandAlias("helpop|ac")
@CommandPermission("chattore.helpop")
class HelpOp(
    private val chattORE: ChattORE
) : BaseCommand() {

    @Default
    @Syntax("[message]")
    fun default(player: ProxiedPlayer, args: Array<String>) {
        if (args.isEmpty()) throw ChattoreException("You have to have a problem first!") // : )
        val message = chattORE.messaging.format.help.formatGlobal(
            sender = player.displayName,
            message = args.joinToString(" ")
        )
        player.sendMessage(*message)
        chattORE.sendPrivileged(
            *message,
            exclude = player.uniqueId
        )
    }
}
