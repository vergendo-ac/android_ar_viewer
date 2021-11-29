package city.augmented.ar_viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import city.augmented.ar_viewer.databinding.FragmentSimpleViewerBinding
import city.augmented.ar_viewer_lib.presentation.ARViewerFragment
import city.augmented.ar_viewer_lib.utils.replaceFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class SimpleViewerFragment : Fragment() {
    private val viewModel: SimpleViewerViewModel by viewModels()
    private lateinit var binding: FragmentSimpleViewerBinding
    private var viewerFragment: ARViewerFragment? = null
    private var localizingJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSimpleViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initProviders(requireContext())
        if (savedInstanceState == null)
            parentFragmentManager.replaceFragment<ARViewerFragment>(R.id.viewer_fragment_container) { fragment ->
                fragment.setOnFragmentReadyListener {
                    viewerFragment = fragment
                    startTakingPictures()
                }
            }
        viewModel.errorMessages.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Localizing error: $it", Toast.LENGTH_SHORT).show()
        }
        viewModel.localizationResult.observe(viewLifecycleOwner) {
            Toast.makeText(
                requireContext(),
                "Localized! There is ${it.size} objects nearby",
                Toast.LENGTH_SHORT
            ).show()
            viewerFragment?.onLocalized(it)
        }
    }

    private fun startTakingPictures() = viewerFragment?.let { fragment ->
        if (localizingJob == null) {
            Toast.makeText(requireContext(), "Starting take picture job", Toast.LENGTH_SHORT).show()
            localizingJob = lifecycleScope.launch {
                fragment.imageDataFlow
                    .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                    .catch { Timber.d("Error on try collect imageData: $it") }
                    .collect { data ->
                        Timber.i("Image data taken! Pose = ${data.syncPose}")
                        viewModel.prepareLocalizationRequest(data)
                    }
            }
        }
    }

    private fun stopTakingPictures() {
        Timber.d("Stopping take picture job")
        localizingJob?.cancel()
        localizingJob = null
    }

    override fun onResume() {
        super.onResume()
        startTakingPictures()
    }

    override fun onPause() {
        stopTakingPictures()
        super.onPause()
    }
}