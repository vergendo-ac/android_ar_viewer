package city.augmented.ar_viewer_lib.entity

import com.google.ar.core.Pose

data class ImageData(
    val imageBytes: ByteArray,
    val orientation: Int,
    val syncPose: Pose
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageData

        if (!imageBytes.contentEquals(other.imageBytes)) return false
        if (orientation != other.orientation) return false
        if (syncPose != other.syncPose) return false

        return true
    }

    override fun hashCode(): Int {
        var result = imageBytes.contentHashCode()
        result = 31 * result + orientation
        result = 31 * result + syncPose.hashCode()
        return result
    }
}