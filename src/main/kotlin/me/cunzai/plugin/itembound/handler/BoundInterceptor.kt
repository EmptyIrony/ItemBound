package me.cunzai.plugin.itembound.handler

import me.cunzai.plugin.itembound.handler.BoundHandler.getBoundInfo
import org.bukkit.event.Event
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.sendLang

object BoundInterceptor {

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        val itemStack = e.item ?: return
        val boundInfo = itemStack.getBoundInfo() ?: return
        if (boundInfo.bounder != e.player.name) {
            e.isCancelled = true
            e.setUseItemInHand(Event.Result.DENY)
            e.setUseInteractedBlock(Event.Result.DENY)
            e.player.sendLang("item_has_been_bound", boundInfo.bounder)
        }
    }

    @SubscribeEvent
    fun e(e: InventoryClickEvent) {
        e.currentItem?.let { item ->
            val boundInfo = item.getBoundInfo() ?: return@let
            if (boundInfo.bounder != e.whoClicked.name) {
                e.isCancelled = true
                e.whoClicked.sendLang("item_has_been_bound", boundInfo.bounder)
                return@let
            }
        }

        if (e.click == ClickType.NUMBER_KEY) {
            val hotbarButton = e.hotbarButton
            val item = e.whoClicked.inventory.getItem(hotbarButton)
            val boundInfo = item?.getBoundInfo() ?: return
            if (boundInfo.bounder != e.whoClicked.name) {
                e.whoClicked.sendLang("item_has_been_bound", boundInfo.bounder)
                e.isCancelled = true
                return
            }
        }

    }

}