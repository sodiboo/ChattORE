package chattore.commands

import chattore.ChattORE
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.velocitypowered.api.proxy.Player

@CommandAlias("nick")
@Description("Manage nicknames")
@CommandPermission("chattore.nick")
class Nick(private val chattore: ChattORE) : BaseCommand() {

    @Subcommand("nick")
    @CommandPermission("chattore.nick.nick")
    fun nick(player: Player, @Single target: String, @Single nick: String) {
        // TODO Sets the nickname of a player to the nickname provided
        // Note: idea here is to support legacy color codes and the adventure minimessage
    }

    @Subcommand("remove")
    @CommandPermission("chattore.nick.remove")
    fun remove(player: Player, @Single target: String) {
        // TODO Removes a nickname for a user
    }

    @Subcommand("color")
    @CommandPermission("chattore.nick.color")
    fun color(player: Player, @Single color: String) {
        // TODO Filter for valid color codes before applying to self
        // Note: this command is meant to be ran by an end-user setting their own nickname
        // Another note: may be worth having a timeout to prevent people from changing to frequently.
    }

    @Subcommand("gradient")
    @CommandPermission("chattore.nick.gradient")
    fun gradient(player: Player, @Single start: String, end: String) {
        // TODO This is effectively setgradient but with the target pre-defined
        // Note: this command is meant to be ran by an end-user setting their own nickname
        // Another note: may be worth having a timeout to prevent people from changing to frequently.
    }

    @Subcommand("setgradient")
    @CommandPermission("chattore.nick.setgradient")
    fun setgradient(player: Player, @Single target: String, @Single start: String, @Single end: String) {
        // TODO This is the staff-issued setgradient that applies the gradient to another user
    }
}
