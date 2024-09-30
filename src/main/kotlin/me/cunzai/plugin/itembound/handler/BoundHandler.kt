package me.cunzai.plugin.itembound.handler

import me.cunzai.plugin.itembound.config.ConfigLoader
import me.cunzai.plugin.itembound.data.BoundInfo
import me.cunzai.plugin.itembound.database.ITEM_UPDATE_REDIS_CHANNEL
import me.cunzai.plugin.itembound.database.MySQLHandler
import me.cunzai.plugin.itembound.database.redis
import org.bukkit.inventory.ItemStack
import taboolib.common5.util.encodeBase64
import taboolib.common5.util.replace
import taboolib.expansion.submitChain
import taboolib.module.chat.colored
import taboolib.module.nms.ItemTag
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.getItemTag
import taboolib.module.nms.setItemTag
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir
import taboolib.platform.util.serializeToByteArray
import java.util.UUID

object BoundHandler {

    fun ItemStack.getBoundInfo(): BoundInfo? {
        if (isAir()) return null
        val data = getItemTag()["bound"] ?: return null
        val tag = data.asCompound()
        val boundUuidString = tag["bound_uuid"]?.asString() ?: return null

        val bounder = tag["bounder"]?.asString() ?: return null
        val versionId = tag["version_id"]?.asInt() ?: return null

        return BoundInfo(UUID.fromString(boundUuidString), bounder, versionId)
    }

    fun ItemStack.removeBoundInfo(): ItemStack {
        val tag = getItemTag()
        tag.remove("bound")

        return setItemTag(tag)
    }

    fun ItemStack.setBoundInfo(boundInfo: BoundInfo, config: ConfigLoader.BoundConfig): ItemStack {
        val tag = getItemTag()
        val boundCompound = ItemTag()

        boundCompound["bound_uuid"] = ItemTagData(boundInfo.boundUuid.toString())
        boundCompound["bounder"] = ItemTagData(boundInfo.bounder)
        boundCompound["version_id"] = ItemTagData(boundInfo.versionId)

        tag["bound"] = boundCompound

        val result = if (boundInfo.versionId <= 0) {
            buildItem(setItemTag(tag)) {
                lore += config.bindLoreAdd.colored()
                    .replace("{0}" to boundInfo.bounder)
            }
        } else {
            setItemTag(tag)
        }

        submitChain {
            if (boundInfo.versionId <= 0) {
                MySQLHandler.table.insert(MySQLHandler.datasource, "bound_uuid", "bounder", "version_id", "item_data") {
                    value(boundInfo.boundUuid.toString(), boundInfo.bounder, 0, result.serializeToByteArray())
                }
            } else {
                MySQLHandler.table.update(MySQLHandler.datasource) {
                    set("version_id", boundInfo.versionId)
                    set("item_data", result.serializeToByteArray())

                    where {
                        "bound_uuid" eq boundInfo.boundUuid.toString()
                    }
                }
            }

            redis.publish(ITEM_UPDATE_REDIS_CHANNEL, boundInfo.boundUuid.toString())
        }

        return result
    }

}