package com.lzinnbarbosa.komadeck.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lzinnbarbosa.komadeck.presentation.catalogo.CatalogoRoute
import com.lzinnbarbosa.komadeck.presentation.detalhes.DetalhesRoute
import kotlinx.serialization.Serializable

@Serializable
data object RotaCatalogo

@Serializable
data class RotaDetalhes(val midiaId: String)

@Composable
fun KomaDeckNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = RotaCatalogo,
    ) {
        composable<RotaCatalogo> {
            CatalogoRoute(
                onAbrirDetalhes = { midiaId ->
                    navController.navigate(RotaDetalhes(midiaId))
                },
            )
        }

        composable<RotaDetalhes> {
            DetalhesRoute(
                onVoltar = { navController.popBackStack() },
            )
        }
    }
}
