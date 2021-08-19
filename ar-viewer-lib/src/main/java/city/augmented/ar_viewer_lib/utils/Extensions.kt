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
