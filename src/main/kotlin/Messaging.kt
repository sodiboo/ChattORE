import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.bungeecord.BungeeCordComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.md_5.bungee.api.chat.BaseComponent

fun String.componentize(
    message: String,
    preserveRawMessage: Boolean = false
) : Component =
    LegacyComponentSerializer.builder()
        .character('&')
        .hexCharacter('#')
        .extractUrls()
        .build()
        .deserialize(this)
        .replaceText("""%message%""".toPattern()) {
            if (preserveRawMessage) {
                TextComponent.of(message).toBuilder()
            } else {
                LegacyComponentSerializer
                    .legacy('&')
                    .deserialize(message.replace(Regex("#([0-9a-f]{6})")) { "&${it.groupValues.first()}" })
                    .toBuilder()
            }
        }


fun String.formatGlobal(
    prefix: String = "",
    sender: String = "",
    recipient: String = "",
    message: String = "",
    preserveRawMessage: Boolean = false
) : Array<BaseComponent> =
    BungeeCordComponentSerializer.get().serialize(
        this
            .replaceFirst("%prefix%", prefix)
            .replaceFirst("%sender%", sender)
            .replaceFirst("%recipient%", recipient)
            .componentize(
                message
                    .replace(Regex("""\s+"""), " ")
                    .trim(),
                preserveRawMessage
            )
    )

