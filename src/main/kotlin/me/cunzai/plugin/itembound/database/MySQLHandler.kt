package me.cunzai.plugin.itembound.database

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.Index
import taboolib.module.database.Table
import taboolib.module.database.getHost

object MySQLHandler {

    @Config("database.yml")
    lateinit var config: Configuration

    val host by lazy {
        config.getHost("mysql")
    }

    val datasource by lazy {
        host.createDataSource()
    }

    val table by lazy {
        Table("item_bound_data", host) {
            add ("bound_uuid"){
                type(ColumnTypeSQL.VARCHAR, 36)
            }

            add("bounder") {
                type(ColumnTypeSQL.VARCHAR, 36)
            }

            add("version_id") {
                type(ColumnTypeSQL.INT)
            }

            add("item_data") {
                type(ColumnTypeSQL.LONGBLOB)
            }
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun i() {
        table.workspace(datasource) {
            createTable(checkExists = true)
            createIndex(Index(("idx_bound_uuid"), listOf("bound_uuid"), checkExists = true))
        }.run()
    }

}