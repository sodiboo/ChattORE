package commands

import ChattORE
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import entity.ChattORESpec
import formatGlobal
import net.md_5.bungee.api.connection.ProxiedPlayer

@CommandAlias("chattore")
@CommandPermission("chattore.manage")
class Chattore(private val chattORE: ChattORE) : BaseCommand() {
    @Default @CatchUnknown
    @Subcommand("version")
    fun version(player: ProxiedPlayer) {
        player.sendMessage(
            *chattORE.config[ChattORESpec.format.chattore].formatGlobal(
                message = "Version &7${chattORE.description.version}"
            )
        )
    }

    @Subcommand("reload")
    fun reload(player: ProxiedPlayer) {
        chattORE.reload()
        player.sendMessage(
            *chattORE.config[ChattORESpec.format.chattore].formatGlobal(
                message = "Reloaded ChattORE"
            )
        )
    }
}