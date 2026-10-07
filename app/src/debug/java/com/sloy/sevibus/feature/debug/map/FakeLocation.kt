package com.sloy.sevibus.feature.debug.map

import com.sloy.sevibus.domain.model.Position
import kotlinx.serialization.Serializable

@Serializable
enum class FakeLocation(val label: String, val position: Position) {
    Triana("Triana", Position(37.385222, -6.011210)),
    Centro("Centro", Position(37.388600, -5.995300)),
    Nervion("Nervión", Position(37.382600, -5.973200)),
    Huelva("Huelva", Position(37.261400, -6.944700)),
}
