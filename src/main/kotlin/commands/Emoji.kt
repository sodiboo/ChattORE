package chattore.commands

import chattore.ChattORE
import chattore.entity.ChattORESpec
import chattore.miniMessageDeserialize
import chattore.render
import chattore.toComponent
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import com.velocitypowered.api.proxy.Player

@CommandAlias("emoji")
@Description("Preview an emoji")
@CommandPermission("chattore.emoji")
class Emoji(private val chattORE: ChattORE, private val emojis: Map<String, String>) : BaseCommand() {
    @Default
    @CommandCompletion("@emojis")
    fun default(player: Player, vararg emojiNames: String) {
        if (!emojis.keys.containsAll(emojiNames.toSet())) {
            val notEmoji = emojiNames.toSet().minus(emojis.keys)
            player.sendMessage(
                chattORE.config[ChattORESpec.format.error].render(
                    "The following are not valid emojis: ${notEmoji.joinToString(", ")}".toComponent()
                )
            )
        }
        val emojiMiniMessage = emojiNames.toSet().intersect(emojis.keys).joinToString(", ") {
            "<hover:show_text:${it}>${emojis[it]}</hover>"
        }
        player.sendMessage(
            chattORE.config[ChattORESpec.format.chattore].render(
                "Emojis: $emojiMiniMessage".miniMessageDeserialize()
            )
        )
    }
}