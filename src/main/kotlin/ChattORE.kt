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
import listener.DiscordListener
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.plugin.Plugin
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.channel.Channel
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.MessageBuilder
import java.io.File
import java.util.*
import java.util.logging.Level

class ChattORE : Plugin() {
    lateinit var luckPerms: LuckPerms
    lateinit var config: Config
    private val replyMap: MutableMap<UUID, UUID> = hashMapOf()
    private var discordMap: Map<String, DiscordApi> = hashMapOf()
    override fun onEnable() {
        config = loadConfig()
        BungeeCommandManager(this).apply {
            registerCommand(Chattore(this@ChattORE))
            registerCommand(HelpOp(this@ChattORE))
            registerCommand(Me(config, this.plugin.proxy))
            registerCommand(Message(config, this.plugin.proxy, replyMap))
            registerCommand(Reply(config, this.plugin.proxy, replyMap))
            setDefaultExceptionHandler(::handleCommandException, false)
        }
        discordMap = loadDiscordTokens()
        discordMap.forEach { (_, discordApi) -> discordApi.updateActivity(config[ChattORESpec.discord.playingMessage]) }
        discordMap.values.firstOrNull()?.addListener(DiscordListener(this))
        luckPerms = LuckPermsProvider.get()
        this.proxy.pluginManager.registerListener(this, ChatListener(this))
    }

    override fun onDisable() {
        discordMap.forEach { (_, discordApi) -> discordApi.disconnect() }
    }

    private fun loadDiscordTokens(): Map<String, DiscordApi> {
        val availableServers = this.proxy.servers.map { it.key.toLowerCase() }.sorted()
        val configServers = config[ChattORESpec.discord.serverTokens].map { it.key.toLowerCase() }.sorted()
        if (availableServers != configServers) {
            logger.log(
                Level.WARNING,
                """
                    Supplied server keys in Discord configuration section does not match available servers:
                    Available servers: ${availableServers.joinToString()}
                    Configured servers: ${configServers.joinToString()}
                """.trimIndent()
            )
        }
        return config[ChattORESpec.discord.serverTokens].mapValues { (name, token) ->
            DiscordApiBuilder()
                .setToken(token)
                .login()
                .join()
        }
    }

    private fun loadConfig(reloaded: Boolean = false): Config {
        if (!dataFolder.exists()) {
            logger.log(Level.INFO, "No resource directory found, creating directory")
            dataFolder.mkdir()
        }
        val configFile = File(dataFolder, "config.yml")
        val loadedConfig = if (!configFile.exists()) {
            logger.log(Level.INFO, "No config file found, generating from default config.yml")
            configFile.createNewFile()
            Config { addSpec(ChattORESpec) }
        } else {
            Config { addSpec(ChattORESpec) }.from.yaml.watchFile(configFile)
        }
        loadedConfig.toYaml.toFile(configFile)
        logger.log(Level.INFO, "${if (reloaded) "Rel" else "L"}oaded config.yml")
        return loadedConfig
    }

    fun broadcastChatMessage(originServer: String, user: UUID, message: String) {
        val userManager = luckPerms.userManager
        val luckUser = userManager.getUser(user) ?: return
        val name = this.proxy.getPlayer(user).displayName
        val prefix = luckUser.cachedData.metaData.prefix ?: return
        this.proxy.broadcast(
            *config[ChattORESpec.format.global].formatGlobal(
                prefix = prefix,
                sender = name,
                message = message
            )
        )

        val discordApi = discordMap[originServer] ?: return
        val channel = discordApi.getChannelById(config[ChattORESpec.discord.channelId]).orElse(null) as? TextChannel ?: run {
            logger.severe("Could not get specified discord channel")
            return
        }

        val plainPrefix = PlainTextComponentSerializer.plainText().serialize(prefix.componentize())
        val content = config[ChattORESpec.discord.format]
            .replace("%prefix%", plainPrefix)
            .replace("%sender%", name)
            .replace("%message%", message)
        val discordMessage = MessageBuilder().setContent(content)
        discordMessage.send(channel)
    }

    fun broadcastDiscordMessage(sender: String, message: String) {
        this.proxy.broadcast(
            *config[ChattORESpec.format.discord].formatGlobal(
                sender = sender,
                message = message
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
    ): Boolean {
        val exception = throwable as? ChattoreException ?: return false
        val message = exception.message ?: "Something went wrong!"
        sender.sendMessage(config[ChattORESpec.format.error].replace("%message%", message))
        return true
    }

    fun reload() {

    }
}

class ChattoreException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
