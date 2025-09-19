package com.sloy.sevibus.feature.debug.inappreview

import android.content.Context
import com.sloy.debugmenu.base.DebugModuleDataSource
import kotlinx.serialization.json.Json

class InAppReviewDebugModuleDataSource(context: Context) : DebugModuleDataSource<InAppReviewDebugModuleState>(context) {

    override val defaultValue
        get() = InAppReviewDebugModuleState()

    override fun Json.decode(jsonString: String): InAppReviewDebugModuleState = decodeFromString(jsonString)
    override fun Json.encode(value: InAppReviewDebugModuleState): String = encodeToString(value)
}