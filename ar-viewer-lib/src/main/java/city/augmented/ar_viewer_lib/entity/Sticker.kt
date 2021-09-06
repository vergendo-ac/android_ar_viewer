package city.augmented.ar_viewer_lib.entity

enum class StickerType {
    OBJECT3D, INFOSTICKER, VIDEO, IMAGE, CUSTOM;

    companion object {
        fun fromString(name: String) = when (name) {
            "3D" -> OBJECT3D
            "VIDEO" -> VIDEO
            "IMAGE" -> IMAGE
            "CUSTOM" -> CUSTOM
            else -> INFOSTICKER
        }
    }
}

abstract class Sticker(
    val stickerId: String,
    val type: StickerType,
    val stickerText: String,
    val path: String,
    val description: String
)

abstract class InfoSticker(
    stickerId: String,
    type: StickerType,
    stickerText: String,
    path: String,
    description: String,
    val stickerType: InfoStickerType = InfoStickerType.OTHER,
    val address: String = "",
    val feedbackAmount: Int = 0,
    val image: String = "",
    val phoneNumber: String = "",
    val priceCategory: String = "",
    val rating: Float = 0f,
    val site: String = "",
    val urlTa: String = ""
) : Sticker(stickerId, type, stickerText, path, description)

abstract class Object3d(
    stickerId: String,
    type: StickerType,
    stickerText: String,
    path: String,
    description: String,
    val subType: Object3dSubtype,
    val modelId: String,
    val modelScale: Float,
    val isGrounded: Boolean,
    val isVerticallyAligned: Boolean
) : Sticker(stickerId, type, stickerText, path, description)

abstract class VideoSticker(
    stickerId: String,
    type: StickerType,
    stickerText: String,
    path: String,
    description: String,
    val width: Float,
    val height: Float,
) : Sticker(stickerId, type, stickerText, path, description)

enum class Object3dSubtype {
    OBJECT, NAVMESH, TRANSFER, CUSTOM;

    companion object {
        fun fromString(name: String) = when (name) {
            "NAVMESH" -> NAVMESH
            "TRANSFER" -> TRANSFER
            "CUSTOM" -> CUSTOM
            else -> OBJECT
        }
    }
}

enum class InfoStickerType {
    REST,
    SHOP,
    PLACE,
    OTHER,
    TEXT,
    VIDEO;

    override fun toString() =
        when (this) {
            REST -> "restaurant"
            SHOP -> "shop"
            PLACE -> "place"
            OTHER -> "other"
            TEXT -> "text"
            VIDEO -> "video"
        }

    companion object {
        fun fromString(string: String): InfoStickerType {
            return when (string) {
                "restaurant" -> REST
                "shop" -> SHOP
                "place" -> PLACE
                "text" -> TEXT
                "video" -> VIDEO
                else -> OTHER
            }
        }
    }
}