package city.augmented.ar_viewer_lib.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import city.augmented.ar_viewer_lib.R
import city.augmented.ar_viewer_lib.components.ARObjectsManager
import city.augmented.ar_viewer_lib.databinding.FragmentArViewerBinding
import city.augmented.ar_viewer_lib.entity.ArObject
import city.augmented.ar_viewer_lib.entity.Sticker
import city.augmented.ar_viewer_lib.replaceFragment
import com.google.ar.core.Pose
import com.google.ar.sceneform.Sceneform
import com.google.ar.sceneform.ux.ArFragment

class ARViewerFragment : Fragment() {
    private lateinit var binding: FragmentArViewerBinding
    private lateinit var arManager: ARObjectsManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (Sceneform.isSupported(requireContext())) {
            parentFragmentManager.replaceFragment<ArFragment>(R.id.ar_fragment_container) { fragment ->
                arManager = ARObjectsManager(requireContext(), binding.pinsView, fragment)
            }
        }
    }

    fun setOnPinClickListener(onClick: (String) -> Unit) {
        binding.pinsView.onClickListener = onClick
    }

    fun onLocalized(arObjects: List<ArObject>, stickers: List<Sticker>, syncPose: Pose) =
        arManager.updateObjects(arObjects, stickers, syncPose)

}