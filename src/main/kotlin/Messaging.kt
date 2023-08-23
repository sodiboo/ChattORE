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

fun String.legacyDeserialize() = LegacyComponentSerializer.legacy('&').deserialize(this)
fun String.miniMessageDeserialize() = MiniMessage.miniMessage().deserialize(this)
fun String.toComponent() = Component.text(this)

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
