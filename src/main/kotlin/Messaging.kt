import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.md_5.bungee.api.chat.BaseComponent

fun fixHexFormatting(str: String): String = str.replace(Regex("#([0-9a-f]{6})")) { "&${it.groupValues.first()}" }

fun String.componentize(
    message: String,
    preserveRawMessage: Boolean = false
): Component =
    LegacyComponentSerializer.builder()
        .character('&')
        .hexCharacter('#')
        .extractUrls()
        .build()
        .deserialize(this)
        .replaceText(
            TextReplacementConfig
                .builder()
                .matchLiteral("""%message%""")
                .replacement(
                    if (preserveRawMessage) {
                        PlainTextComponentSerializer.plainText().deserialize(message)
                    } else {
                        LegacyComponentSerializer
                            .legacy('&')
                            .deserialize(fixHexFormatting(message))
                    }
                )
                .build()
        )


fun String.formatGlobal(
    prefix: String = "",
    sender: String = "",
    recipient: String = "",
    message: String = "",
    preserveRawMessage: Boolean = false
) : Array<BaseComponent> =
    BungeeComponentSerializer.get().serialize(
        this
            .replaceFirst("%prefix%", fixHexFormatting(prefix))
            .replaceFirst("%sender%", sender)
            .replaceFirst("%recipient%", recipient)
            .componentize(
                message
                    .replace(Regex("""\s+"""), " ")
                    .trim(),
                preserveRawMessage
            )
    )

