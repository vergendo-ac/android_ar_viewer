package city.augmented.ar_viewer_lib

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import city.augmented.ar_viewer_lib.entity.ArObject
import com.doors.tourist2.utils.kotlinMath.Float3
import com.google.android.filament.filamat.MaterialBuilder
import com.google.android.filament.filamat.MaterialPackage
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import timber.log.Timber
import java.nio.ByteBuffer
import kotlin.math.min

open class ArNode(
    arSceneView: ArSceneView,
    position: Float3,
    syncPose: Pose
) : Node() {

    init {
        initPosition(arSceneView, position, syncPose)
    }

    protected fun initPosition(arSceneView: ArSceneView, position: Float3, syncPose: Pose) {
        val anchorNode = createAnchor(arSceneView, position, syncPose)
        setParent(anchorNode)
    }

    fun createAnchor(
        arSceneView: ArSceneView,
        position: Float3,
        syncPose: Pose
    ): AnchorNode {
        // от позы камеры, которая была зафиксирована в момент съемки изображения,
        // разместить объекты.
        // вызываем extractTranslation() чтобы извлечь позу без вращения. иначе нод будет повернут
        // на угол поворота камеры
        val pos: Pose = syncPose.compose(
            Pose.makeTranslation(
                position.x,
                position.y,
                position.z
            )
        ).extractTranslation()
        // внутриаркоровское представление набора фич, за которые цепляются объекты
        val anchor: Anchor = arSceneView.session!!.createAnchor(pos)
        // хз зачем нужно
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arSceneView.scene)
        return anchorNode
    }
}

class VideoNode(
    arSceneView: ArSceneView,
    arObject: ArObject.VideoSticker,
    syncPose: Pose
) : ArNode(arSceneView, arObject.position, syncPose) {
    val videoUrl = arObject.url
    private val placeholderPosition: List<Float3> = arObject.placeholderPosition
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var isSoundOn = false
    private var resumeRequested = false
    private var textureRenderable: ModelRenderable? = null
    private var nodes: List<Node>
    private var videoPrepared = false

    init {
        nodes = createPlaceholderNodes(arSceneView, syncPose)
    }

    private fun createPlaceholderNodes(
        arSceneView: ArSceneView,
        syncPose: Pose
    ): List<Node> {
        val newNodes = mutableListOf<Node>()
//        for (i in 0..3) {
        placeholderPosition.forEach { position ->
            newNodes.add(
                Node().also { node ->
                    node.setParent(
                        createAnchor(
                            arSceneView,
                            position,
//                            Pose(localPosition.toFloatArray(), floatArrayOf(0f,0f,0f,1f))
                            syncPose
                        )
                    )
//                    node.setParent(this)
//                    node.localPosition = placeholderPosition[i].toVector3()
                }
            )
        }
        return newNodes
    }

    fun prepareDebugRenderable(context: Context) {
        nodes.forEachIndexed { index, node ->
            val colorInt: Int = when (index) {
                0 -> android.graphics.Color.BLUE
                1 -> android.graphics.Color.RED
                2 -> android.graphics.Color.YELLOW
                3 -> android.graphics.Color.GREEN
                else -> android.graphics.Color.WHITE
            }
            MaterialFactory.makeOpaqueWithColor(context, Color(colorInt))
                .thenAccept { material ->
                    node.renderable = ShapeFactory.makeSphere(
                        0.01f,
                        Vector3(),
                        material
                    )
                }
        }
        MaterialFactory.makeOpaqueWithColor(context, Color())
            .thenAccept { material ->
                this@VideoNode.renderable = ShapeFactory.makeSphere(
                    0.02f,
                    Vector3(),
                    material
                )
            }
    }

    fun prepareVideo(context: Context) {
        videoPrepared = true
        rotateNode()
        // фикс странного бага со смещением объекта
        val scaleFactor = 0.14f
//        ViewRenderable.builder()
//            .setSizer { Vector3(scaleFactor * 1.8f, scaleFactor, 0.0f) }
//            .setView(context, R.layout.view_video_sticker).build()
//            .thenAccept {
//                renderable = it
//                initTextureRenderable(context)
//            }
        initTextureRenderable(context)
    }

    private fun rotateNode() {
        if (nodes.size < 4) return
        val direction = Vector3.subtract(nodes[2].worldPosition, nodes[3].worldPosition).normalized()
        val rotationQuaternion = Quaternion.lookRotation(direction, Vector3.up())
        val rotatedWith180left = Quaternion.multiply(rotationQuaternion, Quaternion.axisAngle(Vector3.left(), 180f))
        val rotatedWith180up = Quaternion.multiply(rotatedWith180left, Quaternion.axisAngle(Vector3.up(), 180f))
        worldRotation =
            Quaternion.multiply(rotatedWith180up, Quaternion.axisAngle(Vector3.down(), 90f))
    }

    private fun initTextureRenderable(context: Context) {
        Timber.d("initTextureRenderable")
        ModelRenderable.builder()
            .setSource(context, Uri.parse("models/video.glb"))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { model: ModelRenderable? ->
                textureRenderable = model
                initVideoMaterial()
            }
            .exceptionally { throwable: Throwable? ->
                throwable?.printStackTrace()
                null
            }
    }

    // TODO() использовать новую реализацию видеоноды без filamat библиотеки
    private fun initVideoMaterial() {
        val filamentEngine = EngineInstance.getEngine().filamentEngine

        MaterialBuilder.init()
        val materialBuilder: MaterialBuilder =
            MaterialBuilder() // By default, materials are generated only for DESKTOP. Since we're an Android
                // app, we set the platform to MOBILE.
                .platform(MaterialBuilder.Platform.MOBILE)
                .name("Plain Video Material")
                .require(MaterialBuilder.VertexAttribute.UV0) // Defaults to UNLIT because it's the only emissive one
                .shading(MaterialBuilder.Shading.UNLIT)
                .doubleSided(true)
                .samplerParameter(
                    MaterialBuilder.SamplerType.SAMPLER_EXTERNAL,
                    MaterialBuilder.SamplerFormat.FLOAT,
                    MaterialBuilder.SamplerPrecision.DEFAULT,
                    "videoTexture"
                )
                .optimization(MaterialBuilder.Optimization.NONE)

        // When compiling more than one material variant, it is more efficient to pass an Engine
        // instance to reuse the Engine's job system
        val plainVideoMaterialPackage: MaterialPackage = materialBuilder
            .blending(MaterialBuilder.BlendingMode.OPAQUE)
            .material(
                "void material(inout MaterialInputs material) {\n" +
                    "    prepareMaterial(material);\n" +
                    "    material.baseColor = texture(materialParams_videoTexture, getUV0()).rgba;\n" +
                    "}\n"
            )
            .build(filamentEngine)
        if (plainVideoMaterialPackage.isValid) {
            val buffer: ByteBuffer = plainVideoMaterialPackage.buffer
            Material.builder()
                .setSource(buffer)
                .build()
                .thenAccept { material: Material ->
                    initMediaPlayer(material)
                }
                .exceptionally { throwable: Throwable? ->
                    throwable?.printStackTrace()
                    null
                }
        } else {
            Timber.e("Invalid plainVideoMaterialPackage")
        }
        MaterialBuilder.shutdown()
    }

    private fun initMediaPlayer(material: Material) {
        Timber.d("initMediaPlayer")
        val videoTexture = ExternalTexture()
        renderable = textureRenderable
        renderableInstance!!.material = material
        renderableInstance!!.material!!.setExternalTexture("videoTexture", videoTexture)
        try {
            mediaPlayer.setSurface(videoTexture.surface)
            mediaPlayer.isLooping = true
            mediaPlayer.setDataSource(videoUrl)
            soundOn()
            mediaPlayer.setOnErrorListener { _, what, extra ->
                Timber.e("Error, what = $what, extra = $extra")
                true
            }
            mediaPlayer.setOnPreparedListener {
                resumeRequested = false
                Timber.d("OnPreparedListener")
                if (!mediaPlayer.isPlaying) {
                    Timber.d("mediaPlayer.isPlaying = false")
                    mediaPlayer.start()
                    videoTexture.surfaceTexture.setOnFrameAvailableListener { surfaceTexture ->
                        scaleVideoNode(
                            placeholderPosition.map { it.toVector3() },
                            mediaPlayer.videoWidth.toFloat(),
                            mediaPlayer.videoHeight.toFloat()
                        )
                        surfaceTexture.setOnFrameAvailableListener(null)
                    }
                }
            }
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e("MediaPlayer error try prepare async: ${e.message}")
        }
    }

    // из-за проблем с SSL сертификатом на сервере, подключаемся к медиасерверу через http
    /*private fun removeSSL(originalLink: String): String {
        return if (originalLink.startsWith("https://developer.augmented.city"))
            originalLink.removeRange(4, 5)
        else originalLink
    }*/

    private fun scaleVideoNode(
        points: List<Vector3>,
        videoWidth: Float,
        videoHeight: Float
    ): Boolean {
        val size = points.size
        var width = 1f
        var height = 1f
        var result = false
        if (size == 4 && videoWidth != 0f) {
            val w0 = Vector3.subtract(points[0], points[1]).length()
            val w1 = Vector3.subtract(points[2], points[3]).length()

            val h0 = Vector3.subtract(points[1], points[2]).length()
            val h1 = Vector3.subtract(points[3], points[0]).length()

            height = min(h0, h1)
            width = min(w0, w1)

            localScale = Vector3(width, height, 1.0f)
//            localPosition = Vector3.add(points[2], points[3]).scaled(.5f)
            result = true
        }
        var wScale = height * (videoWidth / videoHeight)
        var hScale = height
//        Timber.d("video width scale = $wScale")
//        Timber.d("video height scale = $hScale")
        if (wScale > width) {
            wScale = width
            hScale = wScale * (videoHeight / videoWidth)
        }
        localScale = Vector3(wScale, hScale, 1.0f)
        return result
    }

    fun toggleMute() {
        if (isSoundOn) {
            soundOff()
        } else {
            soundOn()
        }

    }

    private fun soundOff() {
//        if (!isSoundOn) return
        try {
            mediaPlayer.setVolume(0f, 0f)
        } catch (e: Exception) {
            Timber.e("MediaPlayer error while try soundOff: $e")
        }
    }

    private fun soundOn() {
//        if (isSoundOn) return
        try {
            mediaPlayer.setVolume(1f, 1f)
        } catch (e: Exception) {
            Timber.e("MediaPlayer error while try soundOn: $e")
        }
    }

    fun changePosition(arSceneView: ArSceneView, newPos: ArObject.VideoSticker, newSyncPose: Pose) {
        initPosition(arSceneView, newPos.position, newSyncPose)
        nodes.forEachIndexed { i, node ->
            val position = newPos.placeholderPosition[i]
            node.setParent(createAnchor(arSceneView, position, newSyncPose))
            Timber.d("new node [$i] position: $position")
        }
        rotateNode()
        if (!videoPrepared) return
        try {
            if (mediaPlayer.videoWidth != 0) {
                scaleVideoNode(
                    newPos.placeholderPosition.map { it.toVector3() },
                    mediaPlayer.videoWidth.toFloat(),
                    mediaPlayer.videoHeight.toFloat()
                )
            }
        } catch (e: Exception) {
            Timber.e("MediaPlayer error while try changePosition: $e")
        }
    }

    override fun onActivate() {
        Timber.d("on video node activate")
        resume()
    }

    fun resume() {
        if (!videoPrepared) return
        try {
            if (mediaPlayer.isPlaying) return
            if (resumeRequested) {
                mediaPlayer.start()
                resumeRequested = false
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            Timber.d("MediaPlayer error while onActivate: ${e.message}")
        }
        Timber.d("Video resumed")
    }

    fun pause() {
        try {
            resumeRequested = true
            if (!mediaPlayer.isPlaying) return
            mediaPlayer.pause()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            Timber.d("MediaPlayer error while onDeactivate: ${e.message}")
        }
        Timber.d("Video paused")
    }

    override fun onDeactivate() {
        Timber.d("on video node deactivate")
        pause()
    }

    fun finalize() {
        try {
            mediaPlayer.stop()
            mediaPlayer.release()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
        nodes.forEach { node ->
            node.setParent(null)
        }
    }

    private fun Float3.toVector3() = Vector3(x, y, z)
}

private fun Vector3.toFloatArray(): FloatArray =
    FloatArray(3).apply {
        this[0] = x
        this[1] = y
        this[2] = z
    }