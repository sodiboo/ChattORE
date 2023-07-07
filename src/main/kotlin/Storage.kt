package chattore

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object Mail : Table("mail") {
    val id = integer("mail_id").autoIncrement()
    val timestamp = integer("mail_timestamp")
    val sender = varchar("mail_sender", 36).index()
    val recipient = varchar("mail_recipient", 36).index()
    val read = bool("mail_read").default(false)
    val message = varchar("mail_message", 512)
    override val primaryKey = PrimaryKey(id)
}

object Nick : Table("nick") {
    val user = varchar("nick_user", 36).index()
    val nick = varchar("nick_nick", 2048)
    override val primaryKey = PrimaryKey(user)
}

class Storage(
    dbFile: String
) {
    private val database = Database.connect("jdbc:sqlite:${dbFile}", "org.sqlite.JDBC")

    init {
        initTables()
    }

    private fun initTables() = transaction(database) {
        SchemaUtils.create(Mail, Nick)
    }

    fun removeNickname(target: UUID) = transaction(database) {
        Nick.deleteWhere { Nick.user eq target.toString() }
    }

    fun getNickname(target: UUID): String? = transaction(database) {
        Nick.select { Nick.user eq target.toString() }.firstOrNull()?.let { it[Nick.nick] }
    }

    fun setNickname(target: UUID, nickname: String) = transaction(database) {
        if (Nick.select { Nick.user eq target.toString() }.count() == 0L) {
            Nick.insert {
                it[this.user] = target.toString()
                it[this.nick] = nickname
            }
        } else {
            Nick.update({ Nick.user eq target.toString() }) {
                it[this.nick] = nickname
            }
        }
    }

    fun insertMessage(sender: UUID, recipient: UUID, message: String) = transaction(database) {
        Mail.insert {
            it[this.timestamp] = System.currentTimeMillis().floorDiv(1000).toInt()
            it[this.sender] = sender.toString()
            it[this.recipient] = recipient.toString()
            it[this.message] = message
        }
    }

    fun readMessage(id: Int): String? = transaction(database) {
        Mail.select {
            Mail.id eq id
        }.firstOrNull()?.let { toReturn ->
            markRead(id, true)
            toReturn[Mail.message]
        }
    }

    fun getMessages(recipient: UUID) = transaction(database) {
        Mail.select { Mail.recipient eq recipient.toString() }
    }

    fun markRead(id: Int, read: Boolean) = transaction(database) {
        Mail.update({Mail.id eq id}) {
            it[this.read] = read
        }
    }

    fun getMail(recipient: UUID) = transaction(database) {
        Mail.select { Mail.recipient eq recipient.toString() }.toList()
    }
}