package chattore.commands

import chattore.*
import chattore.entity.ChattORESpec
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.velocitypowered.api.proxy.Player
import java.util.*

// TODO: 8/23/2023 Add to autocompletes?
val hexColorMap = mapOf(
    "O" to Pair("#000000", "black"),
    "1" to Pair("#00AA00", "dark_blue"),
    "2" to Pair("#00AA00", "dark_green"),
    "3" to Pair("#00AAAA", "dark_aqua"),
    "4" to Pair("#AA0000", "dark_red"),
    "5" to Pair("#AA00AA", "dark_purple"),
    "6" to Pair("#FFAA00", "gold"),
    "7" to Pair("#AAAAAA", "gray"),
    "8" to Pair("#555555", "dark_gray"),
    "9" to Pair("#5555FF", "blue"),
    "a" to Pair("#55FF55", "green"),
    "b" to Pair("#55FFFF", "aqua"),
    "c" to Pair("#FF5555", "red"),
    "d" to Pair("#FF55FF", "light_purple"),
    "e" to Pair("#FFFF55", "yellow"),
    "f" to Pair("#FFFFFF", "white")
)

val hexPattern = """#[0-f]{6}""".toRegex()

fun String.validateColor() = if (this.startsWith("&")) {
    if (this.length != 2) {
        throw ChattoreException("When providing legacy color codes, use a single character after &.")
    }
    val code = hexColorMap[this.substring(1)]
        ?: throw ChattoreException("Invalid color code provided")
    code.second
} else if (hexPattern.matches(this)) {
    this
} else if (this in hexColorMap.values.map { it.second }) {
    this
} else {
    throw ChattoreException("Invalid color code provided")
}

@CommandAlias("nick")
@Description("Manage nicknames")
@CommandPermission("chattore.nick")
class Nick(private val chattORE: ChattORE) : BaseCommand() {

    // TODO: 8/23/2023 Add timeout map

    @Subcommand("nick")
    @CommandPermission("chattore.nick.others")
    @CommandCompletion("@usernameCache")
    fun nick(player: Player, @Single target: String, @Single nick: String) {
        val targetUuid = chattORE.database.usernameToUuidCache[target]
            ?: throw ChattoreException("We do not recognize that user!")
        val nickname = if (nick.contains("&")) {
            nick.legacyDeserialize()
        } else {
            nick.miniMessageDeserialize()
        }
        val rendered = nickname.miniMessageSerialize()
        chattORE.database.setNickname(targetUuid, rendered)
        sendPlayerNotifications(target, player, targetUuid, rendered)
    }

    @Subcommand("remove")
    @CommandPermission("chattore.nick.remove")
    @CommandCompletion("@usernameCache")
    fun remove(player: Player, @Single target: String) {
        val targetUuid = chattORE.database.usernameToUuidCache[target]
            ?: throw ChattoreException("We do not recognize that user!")
        chattORE.database.removeNickname(targetUuid)
        val response = chattORE.config[ChattORESpec.format.chattore].render(
            "Removed nickname for $target."
        )
        player.sendMessage(response)
    }

    @Subcommand("color")
    @CommandPermission("chattore.nick.color")
    fun color(player: Player, @Single color: String) {
        // Note: worth having a timeout to prevent people from changing too frequently.
        val code = "<${color.validateColor()}>"
        val nickname = "${code}${player.username}<reset>"
        chattORE.database.setNickname(player.uniqueId, nickname)
        val response = chattORE.config[ChattORESpec.format.chattore].render(
            "Set your nickname to $nickname".miniMessageDeserialize()
        )
        player.sendMessage(response)
    }

    @Subcommand("gradient")
    @CommandPermission("chattore.nick.gradient")
    fun gradient(player: Player, vararg colors: String) {
        if (colors.size < 2) throw ChattoreException("Not enough colors!")
        if (colors.size > 3) throw ChattoreException("Too many colors!")
        // Note: worth having a timeout to prevent people from changing too frequently.
        val rendered = setNicknameGradient(player.uniqueId, player.username, *colors)
        val response = chattORE.config[ChattORESpec.format.chattore].render(
            "Your nickname has been set to $rendered".miniMessageDeserialize()
        )
        player.sendMessage(response)
    }

    @Subcommand("setgradient")
    @CommandPermission("chattore.nick.setgradient")
    @CommandCompletion("@usernameCache")
    fun setgradient(player: Player, @Single target: String, vararg colors: String) {
        if (colors.size < 2) throw ChattoreException("Not enough colors!")
        val targetUuid = chattORE.database.usernameToUuidCache[target]
            ?: throw ChattoreException("We do not recognize that user!")
        val rendered = setNicknameGradient(targetUuid, target, *colors)
        sendPlayerNotifications(target, player, targetUuid, rendered)
    }

    private fun sendPlayerNotifications(target: String, executor: Player, targetUuid: UUID, rendered: String) {
        val response = chattORE.config[ChattORESpec.format.chattore].render(
            "Set nickname for $target as $rendered.".miniMessageDeserialize()
        )
        executor.sendMessage(response)
        chattORE.proxy.getPlayer(targetUuid).ifPresent {
            it.sendMessage(
                chattORE.config[ChattORESpec.format.chattore].render(
                    "Your nickname has been set to $rendered".miniMessageDeserialize()
                )
            )
        }
    }

    private fun setNicknameGradient(uniqueId: UUID, username: String, vararg colors: String): String {
        val codes = colors.map { it.validateColor() }
        val tag = "<gradient:${codes.joinToString(':'.toString())}>"
        val nickname = "$tag$username<reset>"
        chattORE.database.setNickname(uniqueId, nickname)
        return nickname
    }
}
