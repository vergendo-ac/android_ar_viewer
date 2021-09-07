package city.augmented.ar_viewer_lib.utils

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.math.Matrix
import com.google.ar.sceneform.math.Vector3
import java.io.ByteArrayOutputStream

inline fun <reified F : Fragment> FragmentManager.replaceFragment(
    @IdRes container: Int,
    crossinline onCommit: (F) -> Unit = {}
) = commit {
    val className = F::class.java.name
    replace<F>(container, className)
    runOnCommit {
        val fragment = findFragmentByTag(className) as F
        onCommit(fragment)
    }
}

fun Image.toByteArray(): ByteArray {
    val yBuffer = planes[0].buffer // Y
    val uBuffer = planes[1].buffer // U
    val vBuffer = planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    //U and V are swapped
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    // No need to ratate image on device
    //val rotated = rotateNV21(nv21, this.width, this.height, rotationDegrees)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 95, out)
    return out.toByteArray()
}

fun Camera.isWorldPositionVisible(worldPosition: Vector3): Boolean {
    val var2 = Matrix()
    Matrix.multiply(projectionMatrix, viewMatrix, var2)
    val var5: Float = worldPosition.x
    val var6: Float = worldPosition.y
    val var7: Float = worldPosition.z
    val var8 =
        var5 * var2.data[3] + var6 * var2.data[7] + var7 * var2.data[11] + 1.0f * var2.data[15]
    if (var8 < 0f) {
        return false
    }
    val var9 = Vector3()
    var9.x =
        var5 * var2.data[0] + var6 * var2.data[4] + var7 * var2.data[8] + 1.0f * var2.data[12]
    var9.x = var9.x / var8
    if (var9.x !in -1f..1f) {
        return false
    }

    var9.y =
        var5 * var2.data[1] + var6 * var2.data[5] + var7 * var2.data[9] + 1.0f * var2.data[13]
    var9.y = var9.y / var8
    return var9.y in -1f..1f
}

fun <A, B> Iterable<A>.combineWith(
    another: Iterable<B>,
    compareBy: (A, B) -> Boolean
): List<Pair<A, B>> {
    val newList = mutableListOf<Pair<A, B>>()
    forEach { aArg ->
        another.forEach { bArg ->
            if (compareBy(aArg, bArg))
                newList.add(
                    Pair(aArg, bArg)
                )
        }
    }
    return newList
}
