package city.augmented.ar_viewer_lib.entity

import city.augmented.ar_viewer_lib.utils.kotlinMath.Float3
import com.google.ar.core.Pose
import com.google.ar.sceneform.math.Quaternion

interface ArObject {
    val id: String
    val position: Float3
    val syncPose: Pose
}

data class FlatObject(
    override val id: String,
    override val position: Float3,
    override val syncPose: Pose,
    val stickerData: InfoSticker
) : ArObject

data class VideoObject(
    override val id: String,
    override val position: Float3,
    override val syncPose: Pose,
    val orientation: Quaternion,
    val placeholderNodes: List<Float3>,
    val videoData: VideoSticker
) : ArObject

data class Model3dObject(
    override val id: String,
    override val position: Float3,
    override val syncPose: Pose,
    val modelData: Object3d
) : ArObject