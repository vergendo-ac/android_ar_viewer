package city.augmented.ar_viewer_lib.utils

import city.augmented.ar_viewer_lib.utils.kotlinMath.*
import com.google.ar.core.Pose
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sign

fun inverseMatrix(matrix: Mat4): Mat4 {
    val rotation = matrix.upperLeft
    val position = matrix.position

    val newPosition: Float3 = transpose(-rotation) * position
    val newRotation = transpose(rotation)
    return Mat4(
        Float4(newRotation.x, 0.0f),
        Float4(newRotation.y, 0.0f),
        Float4(newRotation.z, 0.0f),
        Float4(newPosition, 1.0f)
    )
}

fun srvToLocalTransform(local: Mat4, server: Mat4, scaleScalar: Float): Mat4 {
    val scale = Mat4.diagonal(Float4(scaleScalar, scaleScalar, scaleScalar, 1.0f))

    val tf_cb_ca = Mat4(
        Float4(0.0f, 1.0f, 0.0f, 0.0f),
        Float4(1.0f, 0.0f, 0.0f, 0.0f),
        Float4(0.0f, 0.0f, -1.0f, 0.0f),
        Float4(0.0f, 0.0f, 0.0f, 1.0f)
    )

    val tf_b_cb = inverseMatrix(server)
    val tf_ca_a = local
    val tf_b_a = (tf_ca_a * tf_cb_ca) * scale * tf_b_cb

    return tf_b_a
}

fun Pose.toMat4(): Mat4 {
    val array = FloatArray(16)
    this.toMatrix(array, 0)
    return Mat4(array)
}

val Quaternion.eulerAngles: Vector3
    get() {
        // roll (x-axis rotation)
        val sinRCosP = 2 * (w * x + y * z)
        val cosRCosP = 1 - 2 * (x * x + y * y)
        val roll = atan2(sinRCosP, cosRCosP)

        // pitch (y-axis rotation)
        val sinP = 2 * (w * y - z * x)
        val pitch = if (abs(sinP) >= 1)
            PI / 2 * sinP.sign
        else
            asin(sinP)

        // yaw (z-axis rotation)
        val sinYCosP = 2 * (w * z + x * y)
        val cosYCosP = 1 - 2 * (y * y + z * z)
        val yaw = atan2(sinYCosP, cosYCosP)

        return Vector3(roll, pitch, yaw)
    }

fun Quaternion.toArray() = floatArrayOf(this.x, this.y, this.z, this.w)
fun Vector3.toArray() = floatArrayOf(this.x, this.y, this.z)
fun Float3.toFloat4() = Float4(this[0], this[1], this[2], 1.0f)
