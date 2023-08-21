package chattore.commands

import chattore.ChattORE
import chattore.ChattoreException
import chattore.entity.ChattORESpec
import chattore.formatBasic
import chattore.formatGlobal
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.awt.Color
import java.util.*
import java.util.regex.Pattern

class NameGradient(private val name: String, private vararg val colors: Color) {

    fun getRendered(): String = StringBuilder().apply{
        for (i in name.indices) {
            this.append("<#${Integer.toHexString(this@NameGradient.color(i.toFloat()).rgb).drop(2)}>${name[i]}")
        }
        this.append("<reset>")
    }.toString()

    private fun color(value: Float): Color {
        var value = value
        value /= name.length.toFloat() / (colors.size - 1).toFloat()
        val start = colors[value.toInt()]
        val end = colors[value.toInt() + 1]
        val step = value - value.toInt()
        val rStep = end.red - start.red
        val gStep = end.green - start.green
        val bStep = end.blue - start.blue
        return Color(
            start.red + (rStep * step).toInt(),
            start.green + (gStep * step).toInt(),
            start.blue + (bStep * step).toInt()
        )
    }
}

val hexColorMap = mapOf(
    "O" to "#000000",
    "1" to "#00AA00",
    "2" to "#00AA00",
    "3" to "#00AAAA",
    "4" to "#AA0000",
    "5" to "#AA00AA",
    "6" to "#FFAA00",
    "7" to "#AAAAAA",
    "8" to "#555555",
    "9" to "#5555FF",
    "a" to "#55FF55",
    "b" to "#55FFFF",
    "c" to "#FF5555",
    "d" to "#FF55FF",
    "e" to "#FFFF55",
    "f" to "#FFFFFF"
)

@CommandAlias("nick")
@Description("Manage nicknames")
@CommandPermission("chattore.nick")
class Nick(private val chattORE: ChattORE) : BaseCommand() {

    init {
        val hexPattern = Pattern.compile("""#[0-f]{6}""")
    }
    @Subcommand("nick")
    @CommandPermission("chattore.nick.others")
    fun nick(player: Player, @Single target: String, @Single nick: String) {
        val targetUuid = chattORE.database.usernameToUuidCache[target]
            ?: throw ChattoreException("We do not recognize that user!")
        chattORE.database.setNickname(targetUuid, nick)
        val response = chattORE.config[ChattORESpec.format.chattore].formatGlobal(
            message = "Set nickname for $target as $nick."
        )
        player.sendMessage(response)
    }

    @Subcommand("remove")
    @CommandPermission("chattore.nick.remove")
    @CommandCompletion("@usernameCache")
    fun remove(player: Player, @Single target: String) {
        val targetUuid = chattORE.database.usernameToUuidCache[target]
            ?: throw ChattoreException("We do not recognize that user!")
        chattORE.database.removeNickname(targetUuid)
        val response = chattORE.config[ChattORESpec.format.chattore].formatGlobal(
            message = "Removed nickname for $target."
        )
        player.sendMessage(response)
    }

    @Subcommand("color")
    @CommandPermission("chattore.nick.color")
    fun color(player: Player, @Single color: String) {
        // TODO Filter for valid color codes before applying to self
        // Note: Allow hex color codes?
        // Another note: may be worth having a timeout to prevent people from changing to frequently.
        chattORE.database.setNickname(player.uniqueId, "${color}${player.username}")
        val response = chattORE.config[ChattORESpec.format.chattore].formatGlobal(
            message = "Set your username color to $color"
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
        val response = chattORE.config[ChattORESpec.format.chattore].formatBasic(
            MiniMessage.miniMessage().deserialize("Your username has been set to $rendered")
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
        val response = chattORE.config[ChattORESpec.format.chattore].formatBasic(
            MiniMessage.miniMessage().deserialize("Your username has been set to $rendered")
        )
        player.sendMessage(response)
    }

    private fun setNicknameGradient(uniqueId: UUID, username: String, vararg colors: String): String {
        // TODO Validate input. Add support for hex and legacy codes
        val colors = colors.map { Color.decode(it) }
        val nameGradient = NameGradient(username, *colors.toTypedArray())
        val rendered = nameGradient.getRendered()
        chattORE.database.setNickname(uniqueId, rendered)
        return rendered
    }
}
