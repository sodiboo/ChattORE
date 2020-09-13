package commands

import ChattoreException
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import com.uchuhimo.konf.Config
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

@CommandAlias("r|reply")
@CommandPermission("chattore.reply")
class Reply(
    private val config: Config,
    private val proxy: ProxyServer,
    private val replyMap: MutableMap<UUID, UUID>
) : BaseCommand() {

    @Default
    fun default(player: ProxiedPlayer, args: Array<String>) {
        val target = proxy.getPlayer(
            replyMap[player.uniqueId] ?: throw ChattoreException(
                "You have no one to reply to!"
            )
        ) ?: throw ChattoreException(
            "The person you are trying to reply to is no longer online!"
        )
        sendMessage(replyMap, config, player, target, args)
    }
}