package commands

import ChattORE
import ChattoreException
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import com.velocitypowered.api.proxy.Player
import entity.ChattORESpec
import formatGlobal

@CommandAlias("helpop|ac")
@CommandPermission("chattore.helpop")
class HelpOp(
    private val chattORE: ChattORE
) : BaseCommand() {

    @Default
    @Syntax("[message]")
    fun default(player: Player, args: Array<String>) {
        if (args.isEmpty()) throw ChattoreException("You have to have a problem first!") // : )
        val message = chattORE.config[ChattORESpec.format.help].formatGlobal(
            sender = player.username,
            message = args.joinToString(" ")
        )
        player.sendMessage(message)
        chattORE.sendPrivileged(
            message,
            exclude = player.uniqueId
        )
    }
}
