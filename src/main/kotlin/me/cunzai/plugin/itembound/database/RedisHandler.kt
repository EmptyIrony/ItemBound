package me.cunzai.plugin.itembound.database

import me.cunzai.plugin.itembound.util.cache
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submitAsync
import taboolib.expansion.AlkaidRedis
import taboolib.expansion.fromConfig
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import java.util.UUID


@Config(value = "database.yml")
lateinit var config: Configuration

const val ITEM_UPDATE_REDIS_CHANNEL = "item_bound:item_update_notification"

val redis by lazy {
    AlkaidRedis.create()
        .fromConfig(config.getConfigurationSection("redis")!!)
        .connect()
        .connection()
}

@Awake(LifeCycle.ENABLE)
fun i() {
    redis.subscribe(ITEM_UPDATE_REDIS_CHANNEL) {
        val itemUuid = UUID.fromString(message)
        cache.invalidate(itemUuid)
        submitAsync(now = true) {
            cache.get(itemUuid)
        }
    }
}