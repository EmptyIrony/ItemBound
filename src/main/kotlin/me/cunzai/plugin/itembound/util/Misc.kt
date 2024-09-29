package me.cunzai.plugin.itembound.util

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import me.cunzai.plugin.itembound.database.MySQLHandler
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.deserializeToItemStack
import java.util.UUID
import java.util.concurrent.TimeUnit

val cache: LoadingCache<UUID, Pair<ItemStack, Int>> = CacheBuilder.newBuilder()
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build(object :CacheLoader<UUID, Pair<ItemStack, Int>>() {
        override fun load(key: UUID): Pair<ItemStack, Int> {
            return MySQLHandler.table.select(MySQLHandler.datasource) {
                rows("version_id", "item_data")
                where {
                    "bound_uuid" eq key.toString()
                }
            }.firstOrNull {
                getBlob("item_data").binaryStream.readAllBytes().deserializeToItemStack() to
                getInt("version_id")
            } ?: throw IllegalArgumentException("item can found")
        }
    })


fun Player.getPoints(): Int {
    return PlayerPoints.getInstance().api.look(this.uniqueId)
}

fun Player.setPoints(value: Int) {
    PlayerPoints.getInstance().api.set(uniqueId, value)
}