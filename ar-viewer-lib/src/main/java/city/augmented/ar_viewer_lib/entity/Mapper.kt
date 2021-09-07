package city.augmented.ar_viewer_lib.entity

import city.augmented.api.entity.StickerType
import city.augmented.api.model.QuaternionDto
import city.augmented.api.model.StickerDto
import city.augmented.api.model.Vector3dDto
import city.augmented.ar_viewer_lib.utils.kotlinMath.Float3
import com.google.ar.sceneform.math.Quaternion

fun StickerDto.toEntity(): Sticker {
    return when (type) {
        StickerType.OBJECT3D -> Object3d(
            stickerId = this.stickerId,
            type = this.type,
            stickerText = this.stickerText,
            path = this.path,
            description = this.description,
            subType = this.objectSubType,
            modelId = this.objectModelId,
            modelScale = floatFromString(this.objectModelScale),
            isGrounded = this.objectIsGrounded.value,
            isVerticallyAligned = this.objectIsVerticallyAligned.value
        )
        StickerType.VIDEO -> VideoSticker(
            stickerId = this.stickerId,
            type = this.type,
            stickerText = this.stickerText,
            path = this.path,
            description = this.description,
            width = floatFromString(this.videoWidth),
            height = floatFromString(this.videoHeight)
        )
        else -> InfoSticker(
            stickerId = this.stickerId,
            type = this.type,
            stickerText = this.stickerText,
            path = this.path,
            description = this.description,
            stickerType = this.infoStickerType,
            address = this.address,
            feedbackAmount = intFromString(this.feedbackAmount),
            image = this.image,
            phoneNumber = this.phoneNumber,
            priceCategory = this.priceCategory,
            rating = floatFromString(this.rating),
            site = this.site,
            urlTa = this.urlTa
        )
    }
}

private fun intFromString(string: String) = try {
    string.toInt()
} catch (e: NumberFormatException) {
    0
}

private fun floatFromString(string: String) = try {
    string.toFloat()
} catch (e: NumberFormatException) {
    0.0f
}

fun Vector3dDto.toFloatArray() = floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
fun Vector3dDto.toFloat3() = Float3(x.toFloat(), y.toFloat(), z.toFloat())
fun QuaternionDto.toFloatArray() = floatArrayOf(x, y, z, w)
fun QuaternionDto.toQuaternion() = Quaternion(x, y, z, w)
