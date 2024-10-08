package me.cunzai.plugin.itembound.command

import me.cunzai.plugin.itembound.config.ConfigLoader
import me.cunzai.plugin.itembound.data.BoundInfo
import me.cunzai.plugin.itembound.database.MySQLHandler
import me.cunzai.plugin.itembound.handler.BoundHandler.getBoundInfo
import me.cunzai.plugin.itembound.handler.BoundHandler.removeBoundInfo
import me.cunzai.plugin.itembound.handler.BoundHandler.setBoundInfo
import me.cunzai.plugin.itembound.ui.RecallUI
import me.cunzai.plugin.itembound.util.cache
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.common.platform.function.submitAsync
import taboolib.common5.util.replace
import taboolib.module.chat.colored
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir
import taboolib.platform.util.sendLang
import java.util.*

@CommandHeader(name = "bound", permissionDefault = PermissionDefault.TRUE)
object BoundCommand {

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val recall = subCommand {
        execute<Player> { sender, _, _ ->
            RecallUI.open(sender)
        }
    }

    @CommandBody(permission = "bound.admin")
    val adminBind = subCommand {
        dynamic("玩家名") {
            execute<Player> { sender, _, argument ->
                sender.bindItem(argument)
            }
        }
    }

    @CommandBody(permission = "bound.admin")
    val adminUnbind = subCommand {
        execute<Player> { sender, _, argument ->
            val item = sender.inventory.itemInMainHand
            if (item.isAir()) return@execute

            val boundInfo = item.getBoundInfo() ?: return@execute
            val config =
                ConfigLoader.boundConfigs.firstOrNull { conf -> conf.matchConfig.check(item) } ?: return@execute

            sender.inventory.setItemInMainHand(
                buildItem(item) {
                    lore -= config.bindLoreAdd.replace("{0}" to boundInfo.bounder).toSet()
                }.removeBoundInfo()
            )

            submitAsync {
                MySQLHandler.delete(boundInfo.boundUuid)
                cache.invalidate(boundInfo)
            }
        }
    }

    @CommandBody(permissionDefault = PermissionDefault.OP, permission = "bind.admin")
    val recallAdmin = subCommand {
        dynamic("玩家名") {
            execute<Player> { sender, _, argument ->
                RecallUI.open(sender, argument)
            }
        }
    }

    @CommandBody(permissionDefault = PermissionDefault.OP)
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            ConfigLoader.conifg.reload()
            ConfigLoader.loadConfig()
            RecallUI.config.reload()
            sender.sendMessage("ok")
        }
    }

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val main = mainCommand {
        execute<Player> { sender, _, _ ->
            sender.bindItem()
        }
    }

    private fun Player.bindItem(bindTo: String? = null) {
        val sender = this
        val item = sender.inventory.itemInMainHand
        if (item.isAir()) {
            sender.sendLang("bind_air")
            return
        }

        val boundConfig = ConfigLoader.boundConfigs.firstOrNull {
            it.matchConfig.check(item)
        }
        if (boundConfig == null) {
            sender.sendLang("can_not_bind_item")
            return
        }

        val alreadyBound = item.getBoundInfo()
        if (alreadyBound != null) {
            sender.sendLang("item_has_been_bound", alreadyBound.bounder)
            return
        }

        if (item.amount > 1) {
            sender.sendMessage("&c无法绑定堆叠物品".colored())
            return
        }

        if (bindTo == null) {
            val success = boundConfig.costConfig.check(sender)
            if (!success) {
                sender.sendLang("no_money")
                return
            }

            boundConfig.costConfig.take(sender)
        }

        sender.inventory.setItemInMainHand(
            item.setBoundInfo(
                BoundInfo(UUID.randomUUID(), bindTo ?: sender.name, 0),
                boundConfig
            )
        )

        sender.playSound(sender, Sound.BLOCK_ANVIL_USE, 1.6f, 1.6f)

        sender.sendLang("bind_success")
    }

}