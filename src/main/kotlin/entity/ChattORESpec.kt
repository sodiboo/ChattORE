package entity

import com.uchuhimo.konf.ConfigSpec

object ChattORESpec : ConfigSpec("") {
    object discord : ConfigSpec() {
        val botToken by optional("nou")
        val serverId by optional(1234)
        val channelId by optional(1234)
    }
    object irc : ConfigSpec() {
        val name by optional("ORENetwork")
        val server by optional("irc.esper.net")
        val channel by optional("#openredstone")
        val password by optional("nou")
    }
    object format : ConfigSpec() {
        val global by optional("%prefix% &7| &e%sender%&7: &r%message%")
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
