package com.example.dummyshop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.dummyshop.ui.detail.ProductDetailRoute
import com.example.dummyshop.ui.list.ProductsRoute

@Composable
fun DummyShopNavHost(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Products
    ) {
        composable(route = Routes.Products) {
            ProductsRoute(
                onOpenProduct = { id ->
                    navController.navigate(Routes.productDetail(id))
                }
            )
        }

        composable(
            route = Routes.ProductDetail,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
            deepLinks = listOf(navDeepLink { uriPattern = "dummyshop://product/{id}" })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: return@composable
            ProductDetailRoute(
                productId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
