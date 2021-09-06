package city.augmented.ar_viewer_lib.entity

import city.augmented.ar_viewer_lib.utils.kotlinMath.Float3

data class PinData(
    val position: Point,
    val relativePosition: Float3,
    val sticker: InfoSticker,
    var shouldShowFull: Boolean = false
)

data class Point(
    val x: Int,
    val y: Int
) {
    override fun toString(): String {
        return "Point($x, $y)"
    }
}