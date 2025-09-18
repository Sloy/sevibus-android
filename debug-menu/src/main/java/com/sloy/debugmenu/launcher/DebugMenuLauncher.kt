package com.sloy.debugmenu.launcher

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable

object DebugMenuLauncher {

    internal var menuContainer: MenuContainer? = null

    fun launchMenu(context: Context, menu: @Composable () -> Unit) {
        menuContainer = MenuContainer(menu)
        context.startActivity(Intent(context, DebugMenuActivity::class.java))
    }
}

internal class MenuContainer(val menu: @Composable () -> Unit)
