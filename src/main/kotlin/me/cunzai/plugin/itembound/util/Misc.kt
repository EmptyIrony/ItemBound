package me.cunzai.plugin.itembound.util

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import me.cunzai.plugin.itembound.database.MySQLHandler
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.TimeUnit

val cache: LoadingCache<UUID, Int> = CacheBuilder.newBuilder()
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build(object :CacheLoader<UUID, Int>() {
        override fun load(key: UUID): Int {
            return MySQLHandler.table.select(MySQLHandler.datasource) {
                rows("version_id")
                where {
                    "bound_uuid" eq key.toString()
                }
            }.firstOrNull {
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