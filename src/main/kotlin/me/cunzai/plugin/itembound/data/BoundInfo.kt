package me.cunzai.plugin.itembound.data

import java.util.UUID

data class BoundInfo(
    val boundUuid: UUID,
    val bounder: String,
    val versionId: Int,
)
