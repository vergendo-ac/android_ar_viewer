package city.augmented.ar_viewer_lib.components

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import city.augmented.ar_viewer_lib.entity.*
import city.augmented.ar_viewer_lib.presentation.PinsView
import city.augmented.ar_viewer_lib.utils.kotlinMath.Float3
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Sun
import com.google.ar.sceneform.ux.ArFragment
import timber.log.Timber

class ARObjectsManager(
    context: Context,
    pinsView: PinsView,
    private val arFragment: ArFragment
) {
    private val pinCoordinator = PinCoordinator(context, pinsView)

    // ноды уже размещенных объектов
    // нужны чтобы обновить позиции 2d стикеров в DisplayFragment через репозиторий.
    // перед этим 3d координаты нодов конвертируются в 2d координаты экрана
    private val flatStickerNodes = mutableMapOf<String, ArNode>()

    // новы видеостикеров. имеют свой lifecycle, отличный от 2д стикеров
    private val videoStickerNodes = mutableMapOf<String, VideoNode>()

    private val _sessionState: MutableLiveData<ArSceneState> =
        MutableLiveData(ArSceneState.STOPPED)

    val sessionState: LiveData<ArSceneState>
        get() = _sessionState

    private var currentState: ArSceneState
        get() = _sessionState.value!!
        set(value) {
            _sessionState.value = value
        }

    init {
        arFragment.arSceneView.planeRenderer.isEnabled = false
        currentState = ArSceneState.READY
    }

    fun updateObjects(arObjects: List<ArObject>, stickers: List<Sticker>, syncPose: Pose) {
        if (currentState == ArSceneState.STOPPED) return

        val stickersMeta = stickers
            .map { sticker ->
                sticker.stickerId to StickerMeta(
                    sticker as InfoSticker,
                    PinPosition(Point(-1, -1), Float3(-1F, -1F, -1F))
                )
            }
            .toMap()

        pinCoordinator.update(stickersMeta.map { it.value })

        placeObjects(arObjects, syncPose)
        currentState = ArSceneState.LOCALIZED
    }

    private fun placeObjects(arObjects: List<ArObject>, syncPose: Pose) {
        clearObjects()
        // очистить буфер с уже размещенными нодами
        flatStickerNodes.clear()
//        Timber.d("place objects: $objectsToPlace")
        arObjects.forEach { objectToPlace ->
//            if (objectToPlace is ArObject.VideoSticker) {
//                if (videoStickerNodes.contains(objectToPlace.id))
//                    videoStickerNodes[objectToPlace.id]!!.changePosition(arSceneView, objectToPlace, syncPose)
//                else {
//                    videoStickerNodes[objectToPlace.id] = VideoNode(arSceneView, objectToPlace, syncPose).apply {
////                        prepareDebugRenderable(requireContext())
//                        prepareVideo(context)
//                    }
//                    videoStickerNodes[objectToPlace.id]!!
//                }
//            } else
                flatStickerNodes[objectToPlace.id] = ArNode(arFragment.arSceneView, objectToPlace.position, syncPose)

        }
        Timber.d("stickerNodes size: ${flatStickerNodes.size}")
        Timber.d("videoNodes size: ${videoStickerNodes.size}")
//        objectsToPlace = listOf()
        currentState = ArSceneState.TRACKING
    }

    private fun clearObjects() {
        val children: List<Node> =
            ArrayList(arFragment.arSceneView.scene.children)
        for (node in children) {
            if (node is VideoNode)
                continue
            if (node is AnchorNode) {
                node.anchor?.detach()
            }
            if (node !is Camera && node !is Sun) {
                node.setParent(null)
            }
        }
    }
}

enum class ArSceneState {
    STOPPED,

    // промежуточное состояниие. ожидание переключения на любое другое
    READY,

    // стартовая точка локализации. переключается по требованию таймера либо по нажатию кнопки
    // localize. тогда берется кадр и отправляется на сервер. если сервер распознал
    // позицию, состояние переключается на PLACE_NEW_OBJECTS
    LOCALIZING,

    // когда с сервера получен список объектов, включается это состояние. создаются новые AR объекты,
    // размещаются в AR пространстве и в репозиторий стикеров добавляются новые стикеры
    LOCALIZED,

    // переключается либо после состояния LOCALIZE, либо после PLACE_NEW_OBJECTS.
    // в этом состоянии обновляются позициии (на экране) уже размещеннных объектов.
    // это состояние должно сохранятся (с перерывами на LOCALIZE), пока позиция не локазизуется
    // снова, либо пока не нажмется кнопка рескана, либо пока не прекратится обновление датчиков
    // устройства (GPS, etc.)
    TRACKING
}