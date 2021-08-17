package city.augmented.ar_viewer_lib.entity

import com.doors.tourist2.utils.kotlinMath.Float3
import com.google.ar.sceneform.math.Quaternion

sealed class ArObject(open val position: Float3, open val id: String) {
    data class FlatSticker(
        override val position: Float3 = Float3(),
        override val id: String = ""
    ) : ArObject(position, id)

    data class VideoSticker(
        override val position: Float3 = Float3(),
        val rotation: Quaternion,
        override val id: String = "",
        val url: String,
        val placeholderPosition: List<Float3>
    ) : ArObject(position, id)

    data class NavArrow(
        override val position: Float3 = Float3(),
        override val id: String = "",
        val rotation: Quaternion
    ) : ArObject(position, id)
}