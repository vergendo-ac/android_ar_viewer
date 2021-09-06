package city.augmented.ar_viewer_lib.entity

import android.net.Uri
import city.augmented.ar_viewer_lib.utils.kotlinMath.Float3
import com.google.ar.sceneform.math.Quaternion

abstract class ArObject(
    val id: String,
    val position: Float3
)

abstract class FlatObject(
    id: String,
    position: Float3,
    val stickerData: InfoSticker
) : ArObject(id, position)

abstract class VideoObject(
    id: String,
    position: Float3,
    val rotation: Quaternion,
    val url: String,
    val placeholderPosition: List<Float3>
) : ArObject(id, position)

abstract class Model3dObject(
    id: String,
    position: Float3,
    val resUri: Uri,
    val rotation: Quaternion
) : ArObject(id, position)