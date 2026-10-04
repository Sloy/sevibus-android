package com.sloy.debugmenu.testing

import android.content.Context
import org.mockito.Mockito
import org.mockito.kotlin.mock

fun testContext(): Context = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
