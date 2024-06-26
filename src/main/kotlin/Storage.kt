package chattore

import chattore.commands.MailboxItem
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object About : Table("about") {
    val uuid = varchar("about_uuid", 36).index()
    val about = varchar("about_about", 512)
    override val primaryKey = PrimaryKey(uuid)
}

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
    val uuid = varchar("nick_uuid", 36).index()
    val nick = varchar("nick_nick", 2048)
    override val primaryKey = PrimaryKey(uuid)
}

object UsernameCache : Table("username_cache") {
    val uuid = varchar("cache_user", 36).index()
    val username = varchar("cache_username", 16).index()
    override val primaryKey = PrimaryKey(uuid)
}

class Storage(
    dbFile: String
) {
    var uuidToUsernameCache = mapOf<UUID, String>()
    var usernameToUuidCache = mapOf<String, UUID>()
    private val database = Database.connect("jdbc:sqlite:${dbFile}", "org.sqlite.JDBC")

    init {
        initTables()
    }

    private fun initTables() = transaction(database) {
        SchemaUtils.create(About, Mail, Nick, UsernameCache)
    }

    fun setAbout(uuid: UUID, about: String) = transaction(database) {
        if (About.select { About.uuid eq uuid.toString() }.count() == 0L) {
            About.insert {
                it[this.uuid] = uuid.toString()
                it[this.about] = about
            }
        } else {
            About.update({ About.uuid eq uuid.toString() }) {
                it[this.about] = about
            }
        }
    }

    fun getAbout(uuid: UUID) : String? = transaction(database) {
        About.select { About.uuid eq uuid.toString() }.firstOrNull()?.let { it[About.about] }
    }

    fun removeNickname(target: UUID) = transaction(database) {
        Nick.deleteWhere { Nick.uuid eq target.toString() }
    }

    fun getNickname(target: UUID): String? = transaction(database) {
        Nick.select { Nick.uuid eq target.toString() }.firstOrNull()?.let { it[Nick.nick] }
    }

    fun setNickname(target: UUID, nickname: String) = transaction(database) {
        if (Nick.select { Nick.uuid eq target.toString() }.count() == 0L) {
            Nick.insert {
                it[this.uuid] = target.toString()
                it[this.nick] = nickname
            }
        } else {
            Nick.update({ Nick.uuid eq target.toString() }) {
                it[this.nick] = nickname
            }
        }
    }

    fun ensureCachedUsername(user: UUID, username: String) = transaction(database) {
        if (UsernameCache.select { UsernameCache.uuid eq user.toString() }.count() == 0L) {
            UsernameCache.insert {
                it[this.uuid] = user.toString()
                it[this.username] = username
            }
        } else {
            UsernameCache.update({ UsernameCache.uuid eq user.toString() }) {
                it[this.username] = username
            }
        }
        updateLocalUsernameCache()
    }

    fun updateLocalUsernameCache() {
        uuidToUsernameCache = transaction(database) {
            UsernameCache.selectAll().associate {
                UUID.fromString(it[UsernameCache.uuid]) to it[UsernameCache.username]
            }
        }
        usernameToUuidCache = uuidToUsernameCache.entries.associate{(k,v)-> v to k}
    }

    fun insertMessage(sender: UUID, recipient: UUID, message: String) = transaction(database) {
        Mail.insert {
            it[this.timestamp] = System.currentTimeMillis().floorDiv(1000).toInt()
            it[this.sender] = sender.toString()
            it[this.recipient] = recipient.toString()
            it[this.message] = message
        }
    }

    fun readMessage(recipient: UUID, id: Int): Pair<UUID, String>? = transaction(database) {
        Mail.select {
            (Mail.id eq id) and (Mail.recipient eq recipient.toString())
        }.firstOrNull()?.let { toReturn ->
            markRead(id, true)
            UUID.fromString(toReturn[Mail.sender]) to toReturn[Mail.message]
        }
    }

    fun getMessages(recipient: UUID): List<MailboxItem> = transaction(database) {
        Mail.select { Mail.recipient eq recipient.toString() }
            .orderBy(Mail.timestamp to SortOrder.DESC) .map {
            MailboxItem(
                it[Mail.id],
                it[Mail.timestamp],
                UUID.fromString(it[Mail.sender]),
                it[Mail.read]
            )
        }
    }

    private fun markRead(id: Int, read: Boolean) = transaction(database) {
        Mail.update({Mail.id eq id}) {
            it[this.read] = read
        }
    }
}