package commands

import ChattoreException
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.uchuhimo.konf.Config
import entity.ChattORESpec
import formatGlobal
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

@CommandAlias("msg|message|vmsg|vmessage|whisper|tell")
@CommandPermission("chattore.message")
class Message(
    private val config: Config,
    private val proxy: ProxyServer,
    private val replyMap: MutableMap<UUID, UUID>
) : BaseCommand() {

    @Default
    @Syntax("[target] <message>")
    @CommandCompletion("@players")
    fun default(player: ProxiedPlayer, target: String, args: Array<String>) {
        val targetPlayer = proxy.getPlayer(target) ?: throw ChattoreException("That user doesn't exist!")
        sendMessage(replyMap, config, player, targetPlayer, args)
    }
}

// I don't like putting this here but eggsdee we'll figure out a better place later
fun sendMessage(
    replyMap: MutableMap<UUID, UUID>,
    config: Config,
    player: ProxiedPlayer,
    targetPlayer: ProxiedPlayer,
    args: Array<String>
) {
    player.sendMessage(
        *config[ChattORESpec.format.me].formatGlobal(
            recipient = targetPlayer.displayName,
            message = args.joinToString(" ")
        )
    )
    targetPlayer.sendMessage(
        *config[ChattORESpec.format.me].formatGlobal(
            sender = player.displayName,
            message = args.joinToString(" ")
        )
    )
    replyMap[targetPlayer.uniqueId] = player.uniqueId
}
