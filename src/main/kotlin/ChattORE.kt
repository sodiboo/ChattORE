import co.aikar.commands.BaseCommand
import co.aikar.commands.BungeeCommandManager
import co.aikar.commands.CommandIssuer
import co.aikar.commands.RegisteredCommand
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import com.uchuhimo.konf.source.yaml.toYaml
import commands.*
import entity.ChattORESpec
import listener.ChatListener
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.plugin.Plugin
import java.io.File
import java.util.*
import java.util.logging.Level

class ChattORE : Plugin() {
    lateinit var luckPerms: LuckPerms
    lateinit var messaging: Messaging
    val replyMap: MutableMap<UUID, UUID> = hashMapOf()
    override fun onEnable() {
        val config = loadConfig()
        messaging = Messaging(config[ChattORESpec.chattore].format)
        BungeeCommandManager(this).apply {
            registerCommand(Chattore(this@ChattORE))
            registerCommand(HelpOp(this@ChattORE))
            registerCommand(Me(messaging, this.plugin.proxy))
            registerCommand(Message(messaging, this.plugin.proxy, replyMap))
            registerCommand(Reply(messaging, this.plugin.proxy, replyMap))
            setDefaultExceptionHandler(::handleCommandException, false)
        }
        luckPerms = LuckPermsProvider.get()
        this.proxy.pluginManager.registerListener(this, ChatListener(this))
    }

    override fun onDisable() {
    }

    private fun loadConfig(reloaded: Boolean = false): Config {
        if (!dataFolder.exists()) {
            logger.log(Level.INFO, "No resource directory found, creating directory")
            dataFolder.mkdir()
        }
        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            logger.log(Level.INFO, "No config file found, generating from default config.yml")
            configFile.createNewFile()
            Config { addSpec(ChattORESpec) }.from.yaml.inputStream(
                getResourceAsStream("config.yml")
            ).toYaml.toFile(
                configFile
            )
        }
        if (reloaded) {
            logger.log(Level.INFO, "Reloaded config.yml")
        } else {
            logger.log(Level.INFO, "Loaded config.yml")
        }
        return Config { addSpec(ChattORESpec) }.from.yaml.watchFile(configFile)
    }

    fun broadcastChatMessage(user: UUID, message: String) {
        val userManager = luckPerms.userManager
        val luckUser = userManager.getUser(user) ?: return
        val username = this.proxy.getPlayer(user).displayName
        val prefix = luckUser.cachedData.metaData.prefix ?: return
        this.proxy.broadcast(
            *messaging.format.global.formatGlobal(
                prefix = prefix,
                sender = username,
                message = message
                    .replace(Regex("""\s.*"""), " ")
                    .trim()
            )
        )
    }

    fun sendPrivileged(vararg components: BaseComponent, exclude: UUID = UUID.randomUUID()) {
        val privileged = proxy.players.filter {
            it.hasPermission("chattore.privileged")
                && (it.uniqueId != exclude)
        }
        for (user in privileged) {
            user.sendMessage(*components)
        }
    }

    private fun handleCommandException(
        command: BaseCommand,
        registeredCommand: RegisteredCommand<*>,
        sender: CommandIssuer,
        args: List<String>,
        throwable: Throwable
    ) : Boolean {
        val exception = throwable as? ChattoreException ?: return false
        val message = exception.message ?: "Something went wrong!"
        sender.sendMessage(messaging.format.error.replace("%message%", message))
        return true
    }

    fun reload() {
        messaging.format = loadConfig(reloaded = true)[ChattORESpec.chattore].format
    }
}

class ChattoreException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
