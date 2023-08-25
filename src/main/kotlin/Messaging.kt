package chattore;

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

fun fixHexFormatting(str: String): String = str.replace(Regex("#([0-9a-f]{6})")) { "&${it.groupValues.first()}" }

fun String.componentize(): Component =
    LegacyComponentSerializer.builder()
        .character('&')
        .hexCharacter('#')
        .extractUrls()
        .build()
        .deserialize(fixHexFormatting(this))

val urlRegex = """(http|ftp|https):\/\/([\w_-]+(?:(?:\.[\w_-]+)+))([\w.,@?^=%&:\/~+#-]*[\w@?^=%&\/~+#-])""".toRegex()
fun String.legacyDeserialize() = LegacyComponentSerializer.legacy('&').deserialize(this)
fun String.miniMessageDeserialize() = MiniMessage.miniMessage().deserialize(this)
fun String.toComponent() = Component.text(this)
fun String.extractUrls(): Component {
    // "google.com" gets cut to "google.co"
    val parts = urlRegex.split(this)
    val matches = urlRegex.findAll(this).iterator()
    val buildore = Component.text()
    parts.forEach {
        buildore.append(it.legacyDeserialize())
        if (matches.hasNext()) {
            val nextMatch = matches.next()
            buildore.append(
                (
                    "<aqua><click:open_url:${nextMatch.value.removeSuffix('/'.toString())}>" +
                    "<hover:show_text:'<aqua>${nextMatch.value}'>" +
                    "[${nextMatch.groupValues[2]} ↩]" +
                    "</hover>" +
                    "</click><reset>"
                ).miniMessageDeserialize()
            )
        }
    }
    if (parts.size > 1)
        buildore.append(parts.last().legacyDeserialize())
    return buildore.build()
}

fun String.render(
    message: String
): Component = this.render(
    mapOf("message" to Component.text(message))
)

fun String.render(
    message: Component,
): Component = this.render(
    mapOf("message" to message)
)

fun String.render(
    replacements: Map<String, Component> = emptyMap()
): Component = MiniMessage.miniMessage().deserialize(
    this,
    *replacements.map { Placeholder.component(it.key, it.value) }.toTypedArray()
)
