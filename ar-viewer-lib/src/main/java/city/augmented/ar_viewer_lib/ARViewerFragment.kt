package city.augmented.ar_viewer_lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import city.augmented.ar_viewer_lib.databinding.FragmentArViewerBinding
import com.google.ar.sceneform.Sceneform
import com.google.ar.sceneform.ux.ArFragment

class ARViewerFragment : Fragment() {
    private lateinit var binding: FragmentArViewerBinding
    private lateinit var arFragment: ArFragment

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
            parentFragmentManager.replaceFragment<ArFragment>(R.id.ar_fragment_container) {
                arFragment = it
            }
        }
    }

}