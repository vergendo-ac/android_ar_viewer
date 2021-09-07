package city.augmented.ar_viewer_lib.entity

import city.augmented.api.entity.InfoStickerType
import city.augmented.api.entity.Object3dSubtype
import city.augmented.api.entity.StickerType

interface Sticker {
    val stickerId: String
    val type: StickerType
    val stickerText: String
    val path: String
    val description: String
}

data class InfoSticker(
    override val stickerId: String,
    override val type: StickerType,
    override val stickerText: String,
    override val path: String,
    override val description: String,
    val stickerType: InfoStickerType = InfoStickerType.OTHER,
    val address: String = "",
    val feedbackAmount: Int = 0,
    val image: String = "",
    val phoneNumber: String = "",
    val priceCategory: String = "",
    val rating: Float = 0f,
    val site: String = "",
    val urlTa: String = ""
) : Sticker

data class Object3d(
    override val stickerId: String,
    override val type: StickerType,
    override val stickerText: String,
    override val path: String,
    override val description: String,
    val subType: Object3dSubtype,
    val modelId: String,
    val modelScale: Float,
    val isGrounded: Boolean,
    val isVerticallyAligned: Boolean
) : Sticker

data class VideoSticker(
    override val stickerId: String,
    override val type: StickerType,
    override val stickerText: String,
    override val path: String,
    override val description: String,
    val width: Float = 0f,
    val height: Float = 0f,
) : Sticker