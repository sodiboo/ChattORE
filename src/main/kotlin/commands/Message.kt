package commands

import ChattoreException
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.uchuhimo.konf.Config
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import entity.ChattORESpec
import formatGlobal
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
    fun default(player: Player, target: String, args: Array<String>) {
        proxy.getPlayer(target).ifPresentOrElse({ targetPlayer ->
            sendMessage(replyMap, config, player, targetPlayer, args)
        }, {
            throw ChattoreException("That user doesn't exist!")
        })

    }
}

// I don't like putting this here but eggsdee we'll figure out a better place later
fun sendMessage(
    replyMap: MutableMap<UUID, UUID>,
    config: Config,
    player: Player,
    targetPlayer: Player,
    args: Array<String>
) {
    player.sendMessage(
        config[ChattORESpec.format.messageSent].formatGlobal(
            recipient = targetPlayer.username,
            message = args.joinToString(" ")
        )
    )
    targetPlayer.sendMessage(
        config[ChattORESpec.format.messageReceived].formatGlobal(
            sender = player.username,
            message = args.joinToString(" ")
        )
    )
    replyMap[targetPlayer.uniqueId] = player.uniqueId
}
