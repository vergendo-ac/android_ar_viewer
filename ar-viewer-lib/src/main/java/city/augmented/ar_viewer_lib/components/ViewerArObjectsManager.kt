package city.augmented.ar_viewer_lib.components

import android.content.Context
import city.augmented.ar_viewer_lib.entity.ArObject
import city.augmented.ar_viewer_lib.entity.FlatObject
import city.augmented.ar_viewer_lib.entity.PinData
import city.augmented.ar_viewer_lib.entity.Point
import city.augmented.ar_viewer_lib.presentation.PinsView
import city.augmented.ar_viewer_lib.utils.isWorldPositionVisible
import city.augmented.ar_viewer_lib.utils.kotlinMath.Float3
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.*
import com.google.ar.sceneform.math.Vector3

interface ArObjectsManager {
    fun updateObjects(arObjects: List<ArObject>)
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
    private var shouldUpdateFocus = true

    init {
        arView.scene.addOnUpdateListener { arView.arFrame?.let { frame -> onFrameUpdate(frame) } }
    }

    private fun onFrameUpdate(frame: Frame) {
        if (frame.camera.trackingState == TrackingState.TRACKING) {
            pinCoordinator.update(arObjectsNodes.map { it.value }
                .filterIsInstance<InfoStickerNode>().map { node ->
                    val screenPoint = if (isNodeVisible(node))
                        arView.scene.camera.worldToScreenPoint(node.worldPosition).toPoint()
                    else
                        Point(-1, -1)

                    val relativePoint =
                        arView.scene.camera.worldToLocalPoint(node.worldPosition).toFloat3()

                    PinData(screenPoint, relativePoint, node.flatObject.stickerData)
                }
            )
        }
        // fix for not working autofocus
        updateFocus()
    }

    private fun updateFocus() {
        if (shouldUpdateFocus && arView.session != null) {
            val arConfig = Config(arView.session).apply {
                updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                focusMode = Config.FocusMode.AUTO
            }
            arView.session?.configure(arConfig)
            shouldUpdateFocus = false
        }
    }

    private fun isNodeVisible(node: Node): Boolean =
        arView.scene.camera.isWorldPositionVisible(node.worldPosition)

    override fun updateObjects(arObjects: List<ArObject>) {
        // prepare sticker views for drawing
        pinCoordinator.update(arObjects.filterIsInstance<FlatObject>().map {
            PinData(
                Point(-1, -1),
                Float3(-1f, -1f, -1f),
                it.stickerData
            )
        })
        arObjects.forEach { objectToPlace ->
            if (arObjectsNodes.containsKey(objectToPlace.id))
                arObjectsNodes[objectToPlace.id]!!.changePosition(
                    arView,
                    objectToPlace.position,
                    objectToPlace.syncPose
                )
            else if (objectToPlace is FlatObject)
                arObjectsNodes[objectToPlace.id] = InfoStickerNode(arView, objectToPlace)
        }
    }

    override fun clearObjects() = arView.scene.children.forEach { node ->
        if (node is AnchorNode)
            node.anchor?.detach()
        if (node !is Camera)
            node.setParent(null)
    }
}

private fun Vector3.toPoint(): Point = Point(x.toInt(), y.toInt())
private fun Vector3.toFloat3(): Float3 = Float3(x, y, z)