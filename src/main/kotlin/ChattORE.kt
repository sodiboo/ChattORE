import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.RegisteredCommand
import co.aikar.commands.VelocityCommandManager
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import com.uchuhimo.konf.source.yaml.toYaml
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import commands.*
import entity.ChattORESpec
import listener.ChatListener
import listener.DiscordListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.MessageBuilder
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path
import java.util.*

private const val VERSION = "0.1.0-SNAPSHOT"

@Plugin(
    id = "chattore",
    name = "ChattORE",
    version = VERSION,
    url = "https://openredstone.org",
    description = "Because we want to have a chat system that actually wOREks for us.",
    authors = ["Nickster258", "PaukkuPalikka", "StackDoubleFlow"],
    dependencies = [Dependency(id = "luckperms")]
)
class ChattORE(val proxy: ProxyServer, val logger: Logger, @DataDirectory dataFolder: Path) {
    lateinit var luckPerms: LuckPerms
    lateinit var config: Config
    private val replyMap: MutableMap<UUID, UUID> = hashMapOf()
    private var discordMap: Map<String, DiscordApi> = hashMapOf()
    private val dataFolder = dataFolder.toFile()

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        config = loadConfig()
        VelocityCommandManager(proxy, this).apply {
            registerCommand(Chattore(this@ChattORE))
            registerCommand(HelpOp(this@ChattORE))
            registerCommand(Me(config, this@ChattORE))
            registerCommand(Message(config, proxy, replyMap))
            registerCommand(Reply(config, proxy, replyMap))
            setDefaultExceptionHandler(::handleCommandException, false)
        }
        discordMap = loadDiscordTokens()
        discordMap.forEach { (_, discordApi) -> discordApi.updateActivity(config[ChattORESpec.discord.playingMessage]) }
        discordMap.values.firstOrNull()?.addListener(DiscordListener(this))
        luckPerms = LuckPermsProvider.get()
        proxy.eventManager.register(this, ChatListener(this))
    }

    private fun loadDiscordTokens(): Map<String, DiscordApi> {
        val availableServers = proxy.allServers.map { it.serverInfo.name.toLowerCase() }.sorted()
        val configServers = config[ChattORESpec.discord.serverTokens].map { it.key.toLowerCase() }.sorted()
        if (availableServers != configServers) {
            logger.warn(
                """
                    Supplied server keys in Discord configuration section does not match available servers:
                    Available servers: ${availableServers.joinToString()}
                    Configured servers: ${configServers.joinToString()}
                """.trimIndent()
            )
        }
        return config[ChattORESpec.discord.serverTokens].mapValues { (_, token) ->
            DiscordApiBuilder()
                .setToken(token)
                .login()
                .join()
        }
    }

    private fun loadConfig(reloaded: Boolean = false): Config {
        if (!dataFolder.exists()) {
            logger.info("No resource directory found, creating directory")
            dataFolder.mkdir()
        }
        val configFile = File(dataFolder, "config.yml")
        val loadedConfig = if (!configFile.exists()) {
            logger.info("No config file found, generating from default config.yml")
            configFile.createNewFile()
            Config { addSpec(ChattORESpec) }
        } else {
            Config { addSpec(ChattORESpec) }.from.yaml.watchFile(configFile)
        }
        loadedConfig.toYaml.toFile(configFile)
        logger.info("${if (reloaded) "Rel" else "L"}oaded config.yml")
        return loadedConfig
    }

    fun broadcast(component: Component) {
        proxy.allPlayers.forEach { it.sendMessage(component) }
    }

    fun broadcastChatMessage(originServer: String, user: UUID, message: String) {
        val userManager = luckPerms.userManager
        val luckUser = userManager.getUser(user) ?: return
        val name = this.proxy.getPlayer(user).get().username
        val prefix = luckUser.cachedData.metaData.prefix ?: return
        broadcast(
            config[ChattORESpec.format.global].formatGlobal(
                prefix = prefix,
                sender = name,
                message = message
            )
        )

        val discordApi = discordMap[originServer] ?: return
        val channel =
            discordApi.getChannelById(config[ChattORESpec.discord.channelId]).orElse(null) as? TextChannel ?: run {
                logger.error("Could not get specified discord channel")
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
        broadcast(
            config[ChattORESpec.format.discord].formatGlobal(
                sender = sender,
                message = message
            )
        )
    }

    fun sendPrivileged(component: Component, exclude: UUID = UUID.randomUUID()) {
        val privileged = proxy.allPlayers.filter {
            it.hasPermission("chattore.privileged")
                    && (it.uniqueId != exclude)
        }
        for (user in privileged) {
            user.sendMessage(component)
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

    fun getVersion(): String {
        return VERSION
    }

    fun reload() {
        // TODO
    }
}

class ChattoreException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
