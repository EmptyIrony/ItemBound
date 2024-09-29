package me.cunzai.plugin.itembound.scheduler

import me.cunzai.plugin.itembound.data.BoundInfo
import me.cunzai.plugin.itembound.database.MySQLHandler
import me.cunzai.plugin.itembound.handler.BoundHandler.getBoundInfo
import me.cunzai.plugin.itembound.util.cache
import net.minecraft.world.entity.Entity.RemovalReason
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.submitAsync
import taboolib.module.nms.getName
import taboolib.platform.util.isAir
import taboolib.platform.util.onlinePlayers
import taboolib.platform.util.sendLang
import taboolib.platform.util.serializeToByteArray
import java.util.UUID

object ItemScheduler {
    private val visitedBoundItem = HashSet<UUID>()

    @Schedule(period = 2L)
    fun s() {
        visitedBoundItem.clear()
        for (player in onlinePlayers) {
            tickPlayer(player)
        }
    }

    private fun tickPlayer(player: Player) {
        for ((slot, itemStack) in player.inventory.toList().withIndex()) {
            if (itemStack.isAir()) continue
            checkItem(itemStack) { boundInfo, reason ->
                player.inventory.setItem(slot, null)
                player.sendLang(
                    "clear_item_${reason.langNode}",
                    itemStack.getName(),
                    boundInfo.bounder,
                )
            }
        }
    }

    private fun checkItem(itemStack: ItemStack, removeBlock: (BoundInfo, RemoveReason) -> Unit) {
        if (!Bukkit.isPrimaryThread()) {
            throw IllegalStateException("the check task must running main thread")
        }

        val boundInfo = itemStack.getBoundInfo() ?: return

        val success = visitedBoundItem.add(boundInfo.boundUuid)
        if (!success) {
            removeBlock(boundInfo, RemoveReason.DUPLICATE)
            return
        }

        // try load from local cache
        val ifPresent = cache.getIfPresent(boundInfo.boundUuid)
        if (ifPresent == null) {
            // if not cached, async to load to local cache,
            // and check it on next loop
            submitAsync {
                try {
                    cache.get(boundInfo.boundUuid)
                } catch (_: Exception) {

                }
            }
            return
        }

        if (boundInfo.versionId != ifPresent.second) {
            removeBlock(boundInfo, RemoveReason.VERSION_NOT_MATCHED)
            return
        }

        if (itemStack != ifPresent.first) {
            cache.invalidate(boundInfo.boundUuid)
            submitAsync {
                MySQLHandler.table.update(MySQLHandler.datasource) {
                    set("item_data", itemStack.serializeToByteArray())
                    where {
                        "bound_uuid" eq boundInfo.boundUuid.toString()
                    }
                }
            }
        }
    }

    enum class RemoveReason(val langNode: String) {
        DUPLICATE("duplicate"), VERSION_NOT_MATCHED("owner_recall")
    }

}