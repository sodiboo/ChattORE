package chattore.commands

import chattore.ChattORE
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
    @CommandCompletion("@offlinePlayer")
    fun send(player: Player, @Single target: String, message: String) {
        // TODO 'target' represents offline player, 'message' is the message to be sent
        // Note: this will update 'mailTimeouts' so as to avoid flooding.
    }
    @Subcommand("read")
    fun read(player: Player, id: Int) {
        // TODO Implement where 'id' is the id in the database.
        // Note: have checks that id is a message sent to this player
    }
}