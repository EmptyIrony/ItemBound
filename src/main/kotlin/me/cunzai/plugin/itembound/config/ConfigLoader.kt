package me.cunzai.plugin.itembound.config

import me.cunzai.plugin.itembound.util.getPoints
import me.cunzai.plugin.itembound.util.setPoints
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.library.configuration.Path
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Configuration.Companion.toObject
import taboolib.module.lang.asLangTextList
import taboolib.platform.compat.getBalance
import taboolib.platform.compat.withdrawBalance
import taboolib.platform.util.hasLore
import taboolib.platform.util.hasName

object ConfigLoader {

    @Config("bound.yml")
    lateinit var conifg: Configuration

    val boundConfigs = ArrayList<BoundConfig>()

    @Awake(LifeCycle.ENABLE)
    fun i() {
        loadConfig()
    }

    fun loadConfig() {
        boundConfigs.clear()
        for (key in conifg.getKeys(false)) {
            val section = conifg.getConfigurationSection(key)!!
            boundConfigs += section.toObject<BoundConfig>(ignoreConstructor = true)
        }
    }

    data class BoundConfig(
        @Path("match")
        val matchConfig: MatchConfig,
        @Path("cost")
        val costConfig: CostConfig,
        @Path("recall_cost")
        val recallCostConfig: CostConfig,
        @Path("bind_lore_add")
        val bindLoreAdd: List<String>
    )

    data class MatchConfig(
        val name: String?,
        val lore: String?,
        val material: MutableList<String>?,
    ) {
        fun check(itemStack: ItemStack): Boolean {
            name?.apply {
                if (!itemStack.hasName(this)) return false
            }
            lore?.apply {
                if (!itemStack.hasLore(this)) return false
            }
            material?.apply {
                val typeName = itemStack.type.name
                val passed = material.any {
                    typeName.contains(it)
                }
                if (!passed) return false
            }

            return true
        }
    }

    data class CostConfig(
        val coins: Int?,
        val points: Int?,
    ) {
        fun check(player: Player): Boolean {
            coins?.apply {
                if (player.getBalance() < this) return false
            }
            points?.apply {
                if (player.getPoints() < this) return false
            }


            return true
        }

        fun take(player: Player) {
            coins?.apply {
                player.withdrawBalance(this.toDouble())
            }
            points?.apply {
                player.setPoints(player.getPoints() - this)
            }
        }

        fun description(): List<String> {
            val description = ArrayList<String>()
            coins?.apply {
                description += console().asLangTextList("cost_coins", this)
            }
            points?.apply {
                description += console().asLangTextList("cost_points", this)
            }

            return description
        }
    }

}