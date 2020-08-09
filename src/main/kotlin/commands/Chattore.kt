package commands

import ChattORE
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import formatGlobal
import net.md_5.bungee.api.connection.ProxiedPlayer

@CommandAlias("chattore")
@CommandPermission("chattore.manage")
class Chattore(val chattORE: ChattORE) : BaseCommand() {
    @Default
    @Subcommand("version")
    fun version(player: ProxiedPlayer) {
        player.sendMessage(
            *chattORE.messaging.format.chattore.formatGlobal(
                message = "Version &7${chattORE.description.version}"
            )
        )
    }

    @Subcommand("reload")
    fun reload(player: ProxiedPlayer) {
        chattORE.reload()
        player.sendMessage(
            *chattORE.messaging.format.chattore.formatGlobal(
                message = "Reloaded ChattORE"
            )
        )
    }
}