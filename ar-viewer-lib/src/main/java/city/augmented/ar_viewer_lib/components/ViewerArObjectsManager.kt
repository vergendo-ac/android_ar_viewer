package city.augmented.ar_viewer_lib.components

import android.content.Context
import city.augmented.ar_viewer_lib.entity.ArObject
import city.augmented.ar_viewer_lib.entity.InfoSticker
import city.augmented.ar_viewer_lib.entity.PinData
import city.augmented.ar_viewer_lib.entity.Point
import city.augmented.ar_viewer_lib.presentation.PinsView
import city.augmented.ar_viewer_lib.utils.isWorldPositionVisible
import city.augmented.ar_viewer_lib.utils.kotlinMath.Float3
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.*
import com.google.ar.sceneform.math.Vector3

interface ArObjectsManager {
    fun updateObjects(objects: List<ArObject>, stickers: List<InfoSticker>, syncPose: Pose)
    fun clearObjects()
    val arView: ArSceneView
}

class ViewerArObjectsManager(
    context: Context,
    pinsView: PinsView,
    override val arView: ArSceneView
) : ArObjectsManager {
    private val pinCoordinator = PinCoordinator(context, pinsView)
    private val arObjectsNodes = mutableMapOf<String, ArNode>()

    init {
        arView.scene.addOnUpdateListener { arView.arFrame?.let { frame -> onFrameUpdate(frame) } }
    }

    private fun onFrameUpdate(frame: Frame) {
        if (frame.camera.trackingState == TrackingState.TRACKING) {
            val newPins = arObjectsNodes.map { it.value }
                .filterIsInstance<InfoStickerNode>()
                .map { node ->
                    val screenPoint = if (isNodeVisible(node))
                        arView.scene.camera.worldToScreenPoint(node.worldPosition).toPoint()
                    else
                        Point(-1, -1)

                    val relativePoint =
                        arView.scene.camera.worldToLocalPoint(node.worldPosition).toFloat3()

                    PinData(screenPoint, relativePoint, node.arObject.stickerData)
                }
            pinCoordinator.update(newPins)
        }
    }

    private fun isNodeVisible(node: Node): Boolean =
        arView.scene.camera.isWorldPositionVisible(node.worldPosition)

    override fun updateObjects(
        arObjects: List<ArObject>,
        stickers: List<InfoSticker>,
        syncPose: Pose
    ) {
//        pinCoordinator.update(stickersMeta.map { it.value })
        arObjects.forEach { objectToPlace ->
            if (arObjectsNodes.containsKey(objectToPlace.id))
                arObjectsNodes[objectToPlace.id]!!.changePosition(
                    arView,
                    objectToPlace.position,
                    syncPose
                )
            else
                arObjectsNodes[objectToPlace.id] = ArNode(arView, objectToPlace, syncPose)
        }
    }

    override fun clearObjects() = arView.scene.children.forEach { node ->
        if (node is AnchorNode)
            node.anchor?.detach()
        if (node !is Camera && node !is Sun)
            node.setParent(null)
    }
}

private fun Vector3.toPoint(): Point = Point(x.toInt(), y.toInt())
private fun Vector3.toFloat3(): Float3 = Float3(x, y, z)