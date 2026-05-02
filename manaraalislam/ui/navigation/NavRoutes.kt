package com.manarat.manaraalislam.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute {
    @Serializable
    data object Login : NavRoute

    @Serializable
    data object InterestSelection : NavRoute

    @Serializable
    data object Home : NavRoute
}
