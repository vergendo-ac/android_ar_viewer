package city.augmented.ar_viewer

import city.augmented.api.model.CameraDto
import city.augmented.api.model.LocalizationResultDto
import city.augmented.ar_viewer_lib.entity.*
import city.augmented.ar_viewer_lib.utils.combineWith
import city.augmented.ar_viewer_lib.utils.kotlinMath.Float3
import city.augmented.ar_viewer_lib.utils.kotlinMath.Float4
import city.augmented.ar_viewer_lib.utils.kotlinMath.Mat4
import city.augmented.ar_viewer_lib.utils.srvToLocalTransform
import city.augmented.ar_viewer_lib.utils.toFloat4
import city.augmented.ar_viewer_lib.utils.toMat4
import com.google.ar.core.Pose

fun LocalizationResultDto.toLocalEntities(syncPose: Pose): List<ArObject> {
    val camera: CameraDto = camera ?: return emptyList()
    val matrix = srvToLocalTransform(
        syncPose.toMat4(),
        Pose(
            camera.pose.position.toFloatArray(),
            camera.pose.orientation.toFloatArray()
        ).toMat4(),
        1f
    )

    return objects?.combineWith(placeholders!!) { obj, pl ->
        obj.placeholder.placeholderId == pl.placeholderId
    }?.map {
        val obj = it.first
        val placeHolder = it.second
        val placeholderId = placeHolder.placeholderId
        val position = placeHolder.pose.position.toFloat3().toLocal(matrix)
        val orientation = placeHolder.pose.orientation.toQuaternion()
        when (val sticker = obj.sticker.toEntity()) {
            is Object3d -> Model3dObject(placeholderId, position, sticker)
            is VideoSticker -> VideoObject(
                placeholderId,
                position,
                orientation,
                placeHolder.getNodesPositions().toLocal(matrix),
                sticker
            )
            else -> FlatObject(placeholderId, position, sticker as InfoSticker)
        }

    } ?: emptyList()
}


private fun Float3.toLocal(matrix: Mat4): Float3 {
    val pointVec3 = this.toFloat4()
    val pointAr: Float4 = matrix * pointVec3
    return pointAr.xyz
}

private fun List<Float3>.toLocal(matrix: Mat4): List<Float3> =
    this.map { it.toLocal(matrix) }
