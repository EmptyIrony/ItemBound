package me.cunzai.plugin.itembound.handler

import me.cunzai.plugin.itembound.handler.BoundHandler.getBoundInfo
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.isAir
import taboolib.platform.util.sendLang

object BoundInterceptor {

//    @SubscribeEvent
//    fun e(e: PlayerInteractEvent) {
//        if (e.player.hasPermission("bound.admin")) return
//        val itemStack = e.item ?: return
//        val boundInfo = itemStack.getBoundInfo() ?: return
//        if (boundInfo.bounder != e.player.name) {
//            e.isCancelled = true
//            e.setUseItemInHand(Event.Result.DENY)
//            e.setUseInteractedBlock(Event.Result.DENY)
//            e.player.sendLang("item_has_been_bound", boundInfo.bounder)
//        }
//    }

//    @SubscribeEvent
//    fun e(e: InventoryClickEvent) {
//        if (e.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
//            val item = e.currentItem
//            if (item.isAir()) return
//            val boundInfo = item.getBoundInfo() ?: return
//            if (boundInfo.bounder != e.whoClicked.name) {
//                e.isCancelled = true
//                e.whoClicked.sendLang("item_has_been_bound", boundInfo.bounder)
//            }
//        }
//    }

//    @SubscribeEvent
//    fun e(e: EntityPickupItemEvent) {
//        if (e.entity.hasPermission("bound.admin")) return
//
//        val itemStack = e.item.itemStack
//
//        val boundInfo = itemStack.getBoundInfo() ?: return
//        if (boundInfo.bounder != e.entity.name) {
//            e.item.pickupDelay = 40
//            e.isCancelled = true
//            e.entity.sendLang("item_has_been_bound", boundInfo.bounder)
//            return
//        }
//
//    }

}