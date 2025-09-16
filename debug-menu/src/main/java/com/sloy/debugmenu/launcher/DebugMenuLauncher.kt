package com.sloy.debugmenu.launcher

import androidx.compose.runtime.Composable

object DebugMenuLauncher {

    internal var menuContainer: MenuContainer? = null

    fun launchMenu(menu: @Composable () -> Unit) {
        menuContainer = MenuContainer(menu)
    }
}

internal class MenuContainer(val menu: @Composable () -> Unit)
