package entity

import com.uchuhimo.konf.ConfigSpec

object ChattORESpec : ConfigSpec("") {
    val chattore by required<ChattOREConfig>()
}

data class ChattOREConfig(
    val discord: DiscordConfig,
    val irc: IrcConfig,
    val format: FormatConfig
)

data class IrcConfig(
    val name: String,
    val server: String,
    val channel: String,
    val password: String
)

data class DiscordConfig(
    val botToken: String,
    val serverId: Long,
    val channelId: Long
)

data class FormatConfig(
    val global: String,
    val message_received: String,
    val message_sent: String,
    val me: String,
    val social_spy: String,
    val command_spy: String,
    val error: String,
    val chattore: String,
    val help: String
)
