package chattore.commands

import chattore.ChattORE
import chattore.ChattoreException
import chattore.render
import chattore.entity.ChattORESpec
import chattore.toComponent
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.annotation.Optional
import com.velocitypowered.api.proxy.Player
import java.util.*

@CommandAlias("mail")
@Description("Send a message to an offline player")
@CommandPermission("chattore.mail")
class Mail(private val chattORE: ChattORE) : BaseCommand() {

    private val mailTimeouts = mutableMapOf<UUID, Long>()

    @Default
    @CatchUnknown
    @Subcommand("mailbox")
    @CommandCompletion("@bool")
    fun mailbox(player: Player, @Optional read: String?, @Default("1") page: Int) {
        // TODO 'read' filters inbox on if its been marked as read or not, 'page' is the page in the mailbox viewer
        // Note: 'read' and 'page' will be autopopulated by the mailbox pagination stuff.
    }

    @Subcommand("send")
    @CommandCompletion("@usernameCache")
    fun send(player: Player, @Single target: String, message: String) {
        val now = System.currentTimeMillis().floorDiv(1000)
        mailTimeouts[player.uniqueId]?.let {
            // 60 second timeout to prevent flooding
            if (now < it + 60) throw ChattoreException("You are mailing too quickly!")
        }
        val targetUuid = chattORE.database.usernameToUuidCache[target]
            ?: throw ChattoreException("We do not recognize that user!")
        mailTimeouts[player.uniqueId] = now
        chattORE.database.insertMessage(player.uniqueId, targetUuid, message)
        val response = chattORE.config[ChattORESpec.format.mailSent].render(
            mapOf(
                "message" to message.toComponent(),
                "recipient" to target.toComponent()
            )
        )
        player.sendMessage(response)
    }

    @Subcommand("read")
    fun read(player: Player, id: Int) {
        chattORE.database.readMessage(player.uniqueId, id)?.let {
            val response = chattORE.config[ChattORESpec.format.mailReceived].render(
                mapOf(
                    "message" to it.second.toComponent(),
                    "sender" to chattORE.database.uuidToUsernameCache.getValue(it.first).toComponent()
                )
            )
            player.sendMessage(response)
        } ?: run {
            throw ChattoreException("Invalid message ID!")
        }
    }
}