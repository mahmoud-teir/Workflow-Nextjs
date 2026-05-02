package com.manarat.manaraalislam.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AthkarCategory(
    val name: String,
    val items: List<AthkarItem>
)

@Serializable
data class AthkarItem(
    val text: String,
    val count: Int,
    val description: String? = null,
    val reference: String? = null
)
