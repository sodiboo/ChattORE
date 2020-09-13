import net.kyori.adventure.text.serializer.bungeecord.BungeeCordComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.md_5.bungee.api.chat.BaseComponent

fun String.componentize(
    message: String,
    escapedCode: Char = 'r',
    preserveRawMessage: Boolean = false
) : Array<BaseComponent> =
    BungeeCordComponentSerializer.get().serialize(
        LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .extractUrls()
            .build()
            .deserialize(
                if (preserveRawMessage) {
                    this
                        .replaceFirst("%message%", message.replace(
                            Regex("&([0-9a-fklmnor])")) { "&&$escapedCode${it.groupValues[1]}" }
                        )
                } else {
                    this
                        .replaceFirst("%message%", message)
                        .replace(Regex("#([0-9a-f]{6})")) { "&${it.groupValues.first()}" }
                }
            )
    )

fun String.formatGlobal(
    prefix: String = "",
    sender: String = "",
    recipient: String = "",
    message: String = "",
    preserveRawMessage: Boolean = false
) : Array<BaseComponent> =
    this
        .replaceFirst("%prefix%", prefix)
        .replaceFirst("%sender%", sender)
        .replaceFirst("%recipient%", recipient)
        .componentize(message, this[1], preserveRawMessage)
