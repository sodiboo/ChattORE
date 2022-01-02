package commands

import ChattORE
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.velocitypowered.api.proxy.Player
import entity.ChattORESpec
import formatGlobal

@CommandAlias("chattore")
@CommandPermission("chattore.manage")
class Chattore(private val chattORE: ChattORE) : BaseCommand() {
    @Default
    @CatchUnknown
    @Subcommand("version")
    fun version(player: Player) {
        player.sendMessage(
            chattORE.config[ChattORESpec.format.chattore].formatGlobal(
                message = "Version &7${chattORE.getVersion()}"
            )
        )
    }

    @Subcommand("reload")
    fun reload(player: Player) {
        chattORE.reload()
        player.sendMessage(
            chattORE.config[ChattORESpec.format.chattore].formatGlobal(
                message = "Reloaded ChattORE"
            )
        )
    }
}