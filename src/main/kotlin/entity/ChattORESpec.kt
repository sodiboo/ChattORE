package entity

import com.uchuhimo.konf.ConfigSpec

object ChattORESpec : ConfigSpec("") {
    object discord : ConfigSpec() {
        val networkToken by optional("nouNetwork")
        val ircToken by optional("nouIrc")
        val playingMessage by optional("on the ORE Network")
        val channelId by optional(1234L)
        val serverTokens by optional(
            mapOf(
                "serverOne" to "token1",
                "serverTwo" to "token2",
                "serverThree" to "token3"
            )
        )
    }
    object irc : ConfigSpec() {
        val name by optional("ORENetwork")
        val server by optional("irc.esper.net")
        val channel by optional("#openredstone")
        val password by optional("nou")
    }
    object format : ConfigSpec() {
        val global by optional("%prefix% &7| &e%sender%&7: &r%message%")
        val discord by optional("`%prefix% **%sender%**: %message%")
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
