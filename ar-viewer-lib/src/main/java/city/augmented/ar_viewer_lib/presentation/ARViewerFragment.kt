package city.augmented.ar_viewer_lib.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import city.augmented.ar_viewer_lib.R
import city.augmented.ar_viewer_lib.components.ArObjectsManager
import city.augmented.ar_viewer_lib.components.ViewerArObjectsManager
import city.augmented.ar_viewer_lib.databinding.FragmentArViewerBinding
import city.augmented.ar_viewer_lib.entity.ArObject
import city.augmented.ar_viewer_lib.entity.ImageData
import city.augmented.ar_viewer_lib.utils.replaceFragment
import city.augmented.ar_viewer_lib.utils.toByteArray
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.NotYetAvailableException
import com.google.ar.sceneform.Sceneform
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber

class ARViewerFragment : Fragment() {
    private lateinit var binding: FragmentArViewerBinding
    private lateinit var arManager: ArObjectsManager
    private var onFragmentReady: (ARViewerFragment) -> Unit = {}
    private var isFragmentExist = true
    var acquireImageDelay = 800L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun setOnFragmentReadyListener(onReady: (ARViewerFragment) -> Unit) {
        onFragmentReady = onReady
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (Sceneform.isSupported(requireContext())) {
            parentFragmentManager.replaceFragment<ArFragment>(R.id.ar_fragment_container) { fragment ->
                fragment.arSceneView.planeRenderer.isEnabled = false
                arManager =
                    ViewerArObjectsManager(requireContext(), binding.pinsView, fragment.arSceneView)
                onFragmentReady(this)
            }
        }
    }

    val imageDataFlow: Flow<ImageData> = flow {
        while (isFragmentExist) {
            try {
                requestFrame()?.let { imageData ->
                    emit(imageData)
                }
            } catch (e: Exception) {
                Timber.d("Failed to request image^ $e")
            }
            delay(acquireImageDelay)
        }
    }

    suspend fun requestFrame(): ImageData? = withContext(Dispatchers.Default) {
        val frame = arManager.arView.arFrame
        if (frame != null && frame.camera.trackingState == TrackingState.TRACKING) {
            Timber.d("trying acquireCameraImage")

            val image = frame.acquireCameraImage()
            val imageBytes = image.toByteArray()
            val syncPose = frame.camera.pose

            image.close()

            ImageData(imageBytes, 0, syncPose)
        } else null
    }

    fun setOnPinClickListener(onClick: (String) -> Unit) {
        binding.pinsView.onClickListener = onClick
    }

    fun onLocalized(arObjects: List<ArObject>) {
        arManager.updateObjects(arObjects)
    }

    override fun onDestroy() {
        isFragmentExist = false
        super.onDestroy()
    }
}