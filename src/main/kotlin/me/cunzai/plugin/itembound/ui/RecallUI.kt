package me.cunzai.plugin.itembound.ui

import kotlinx.coroutines.withContext
import me.cunzai.plugin.itembound.config.ConfigLoader
import me.cunzai.plugin.itembound.data.BoundInfo
import me.cunzai.plugin.itembound.database.MySQLHandler
import me.cunzai.plugin.itembound.handler.BoundHandler.getBoundInfo
import me.cunzai.plugin.itembound.handler.BoundHandler.setBoundInfo
import me.cunzai.plugin.itembound.util.cache
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.expansion.AsyncDispatcher
import taboolib.expansion.SyncDispatcher
import taboolib.expansion.submitChain
import taboolib.library.xseries.getItemStack
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getStringColored
import taboolib.module.configuration.util.getStringListColored
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.PageableChest
import taboolib.platform.util.buildItem
import taboolib.platform.util.deserializeToItemStack
import taboolib.platform.util.sendLang

object RecallUI {

    @Config("recall_ui.yml")
    lateinit var config: Configuration

    fun open(player: Player) {
        submitChain {
            player.openUI(player.loadBoundItems())
        }
    }

    fun open(player: Player, view: String) {
        submitChain {
            player.openUI(view.loadBoundIems())
        }
    }



    private suspend fun Player.openUI(items: List<ItemStack>) = withContext(SyncDispatcher) {
        openMenu<PageableChest<ItemStack>>(title = config.getStringColored("title")!!) {
            map(*config.getStringList("format").toTypedArray())
            set('#', config.getItemStack("placeholder")!!) {
                isCancelled = true
            }
            setPreviousPage(getSlots('<').first()) { _, _ ->
                config.getItemStack("previous_page")!!
            }
            setNextPage(getSlots('>').first()) { _, _ ->
                config.getItemStack("next_page")!!
            }

            val itemLoreAdd = config.getStringListColored("recall.lore_add")

            elements {
                items
            }
            slots(getSlots('@'))
            onGenerate { _, element, _, _ ->
                val boundConfig = ConfigLoader.boundConfigs.firstOrNull {
                    it.matchConfig.check(element)
                } ?: return@onGenerate element.clone()

                val cost = boundConfig.recallCostConfig.description()
                val loreAdd = itemLoreAdd.toMutableList()
                val indexOf = loreAdd.indexOf("%cost%")
                if (indexOf != -1) {
                    loreAdd.removeAt(indexOf)
                    loreAdd.addAll(indexOf, cost)
                }

                buildItem(element.clone()) {
                    lore += loreAdd
                }
            }
            onClick { event, element ->
                event.isCancelled = true
                val boundConfig = ConfigLoader.boundConfigs.firstOrNull {
                    it.matchConfig.check(element)
                } ?: return@onClick

                if (!boundConfig.costConfig.check(this@openUI)) {
                    sendLang("no_money")
                    return@onClick
                }

                boundConfig.costConfig.take(this@openUI)
                val info = element.getBoundInfo()!!
                val newBoundInfo = BoundInfo(
                    info.boundUuid,
                    info.bounder,
                    info.versionId + 1
                )

                cache.put(newBoundInfo.boundUuid, element to newBoundInfo.versionId)

                inventory.setItemInMainHand(
                    element.setBoundInfo(newBoundInfo, boundConfig)
                )

                sendLang("recall_success")
            }

            onClick { event ->
                event.isCancelled = true
            }
        }
    }

    suspend fun Player.loadBoundItems(): List<ItemStack> = withContext(AsyncDispatcher) {
        return@withContext name.loadBoundIems()
    }

    private suspend fun String.loadBoundIems(): List<ItemStack> = withContext(AsyncDispatcher) {
        MySQLHandler.table.select(MySQLHandler.datasource) {
            where {
                "bounder" eq this@loadBoundIems
            }
        }.map {
            getBlob("item_data").binaryStream.readAllBytes().deserializeToItemStack()
        }
    }

}