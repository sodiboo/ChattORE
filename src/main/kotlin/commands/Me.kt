package commands

import ChattORE
import ChattoreException
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import com.uchuhimo.konf.Config
import com.velocitypowered.api.proxy.Player
import entity.ChattORESpec
import formatGlobal

@CommandAlias("me")
@CommandPermission("chattore.me")
class Me(
    private val config: Config,
    private val chattORE: ChattORE
) : BaseCommand() {

    @Default
    @Syntax("[message]")
    fun default(player: Player, args: Array<String>) {
        if (args.isEmpty()) throw ChattoreException("You have to &ohave&c a thonk first!")
        chattORE.broadcast(
            config[ChattORESpec.format.me].formatGlobal(
                sender = player.username,
                message = args.joinToString(" ")
            )
        )
    }
}
