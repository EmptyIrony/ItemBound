package me.cunzai.plugin.itembound.command

import me.cunzai.plugin.itembound.config.ConfigLoader
import me.cunzai.plugin.itembound.data.BoundInfo
import me.cunzai.plugin.itembound.handler.BoundHandler.setBoundInfo
import me.cunzai.plugin.itembound.ui.RecallUI
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
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

    @CommandBody(permissionDefault = PermissionDefault.OP, permission = "bind.admin")
    val recallAdmin = subCommand {
        dynamic("玩家名") {
            execute<Player> { sender, _, argument ->
                RecallUI.open(sender, argument)
            }
        }
    }

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val main = mainCommand {
        execute<Player> { sender, _, _ ->
            val item = sender.inventory.itemInMainHand
            if (item.isAir()) {
                sender.sendLang("bind_air")
                return@execute
            }

            val boundConfig = ConfigLoader.boundConfigs.firstOrNull {
                it.matchConfig.check(item)
            }
            if (boundConfig == null) {
                sender.sendLang("can_not_bind_item")
                return@execute
            }

            val success = boundConfig.costConfig.check(sender)
            if (!success) {
                sender.sendLang("no_money")
                return@execute
            }

            boundConfig.costConfig.take(sender)
            sender.inventory.setItemInMainHand(
                item.setBoundInfo(
                    BoundInfo(UUID.randomUUID(), sender.name, 0),
                    boundConfig
                )
            )

            sender.playSound(sender, Sound.BLOCK_ANVIL_USE, 1.6f, 1.6f)

            sender.sendLang("bind_success")
        }
    }

}