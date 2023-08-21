package chattore;

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

fun fixHexFormatting(str: String): String = str.replace(Regex("#([0-9a-f]{6})")) { "&${it.groupValues.first()}" }

fun String.componentize(): Component =
    LegacyComponentSerializer.builder()
        .character('&')
        .hexCharacter('#')
        .extractUrls()
        .build()
        .deserialize(fixHexFormatting(this))

fun String.formatError(
    message: String
): Component = MiniMessage.miniMessage().deserialize(
    this,
    Placeholder.component("message", PlainTextComponentSerializer.plainText().deserialize(message))
)

// hack? yes ! work? maybe !
fun String.formatGlobal(
    message: String
): Component = this.formatGlobal(
    "",
    "",
    "",
    message
)

fun String.formatGlobal(
    prefix: String = "",
    sender: String = "",
    recipient: String = "",
    message: String = "",
    preserveRawMessage: Boolean = false
): Component = this.formatGlobal(
    prefix,
    Component.text(sender),
    Component.text(recipient),
    message,
    preserveRawMessage
)

fun String.formatGlobal(
    prefix: String = "",
    sender: Component = Component.text(""),
    recipient: Component = Component.text(""),
    message: String = "",
    preserveRawMessage: Boolean = false
): Component {
    val message = message
        .replace(Regex("""\s+"""), " ")
        .trim()
    return MiniMessage.miniMessage().deserialize(
        this,
        Placeholder.component("prefix", LegacyComponentSerializer.legacy('&').deserialize(prefix)),
        Placeholder.component("sender", sender),
        Placeholder.component("recipient", recipient),
        Placeholder.component("message",
            if (preserveRawMessage) {
                PlainTextComponentSerializer.plainText().deserialize(message)
            } else {
                LegacyComponentSerializer
                    .legacy('&')
                    .deserialize(fixHexFormatting(message))
            }
        )
    )
}

fun String.formatBasic(
    message: String,
): Component = formatBasic(
    LegacyComponentSerializer
        .legacy('&')
        .deserialize(message)
)

fun String.formatBasic(
    message: Component,
): Component =
    MiniMessage.miniMessage().deserialize(
        this,
        Placeholder.component("message", message)
    )
