package chattore.entity

import com.uchuhimo.konf.ConfigSpec

object ChattORESpec : ConfigSpec("") {

    val storage by optional("storage.db")

    object discord : ConfigSpec() {
        val enable by optional(false)
        val networkToken by optional("nouNetwork")
        val playingMessage by optional("on the ORE Network")
        val channelId by optional(1234L)
        val serverTokens by optional(
            mapOf(
                "serverOne" to "token1",
                "serverTwo" to "token2",
                "serverThree" to "token3"
            )
        )
        val format by optional("`%prefix%` **%sender%**: %message%")
    }

    object format : ConfigSpec() {
        val global by optional("%prefix% &7| &e%sender%&7: &r%message%")
        val discord by optional("&3Discord &7| &5%sender%&7: &r%message%")
        val mailReceived by optional("&6[&cFrom %sender%&6]&r %message%")
        val mailSent by optional("&6[&cTo %recipient%&6]&r %message%")
        val messageReceived by optional("&6[&c%sender% &6->&c me&6]&r %message%")
        val messageSent by optional("&6[&cme &6->&c %recipient%&6]&r %message%")
        val me by optional("* &o%sender% %message% &r*")
        val socialSpy by optional("&6[&c%sender% &6->&c %recipient%&6]&r %message%")
        val commandSpy by optional("&6%sender%: %message%")
        val error by optional("&c&lOh NO ! &7: &c%message%")
        val chattore by optional("&6[&cChattORE&6] &c%message%")
        val help by optional("&6[&cHelp&6]&c %sender%&7: &r%message%")
    }
}
