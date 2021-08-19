package city.augmented.ar_viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import city.augmented.ar_viewer.databinding.FragmentSimpleViewerBinding
import city.augmented.ar_viewer_lib.components.ArSceneState
import city.augmented.ar_viewer_lib.presentation.ARViewerFragment
import city.augmented.ar_viewer_lib.utils.replaceFragment
import kotlinx.coroutines.flow.collect
import timber.log.Timber

class SimpleViewerFragment : Fragment() {
    private val viewModel: SimpleViewerViewModel by viewModels()
    private lateinit var binding: FragmentSimpleViewerBinding
    private lateinit var viewerFragment: ARViewerFragment

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
        if (savedInstanceState == null)
            parentFragmentManager.replaceFragment<ARViewerFragment>(R.id.viewer_fragment_container) { fragment ->
                viewerFragment = fragment
                fragment.setOnFragmentReadyListener {
                    subscribeToSessionUpdates(it.sessionState)
                }
            }
    }

    private fun subscribeToSessionUpdates(sessionStateLiveData: LiveData<ArSceneState>) {
        sessionStateLiveData.observe(viewLifecycleOwner) { state ->
            Timber.d("ARViewerFragment state: $state")
            when (state) {
                ArSceneState.READY -> {
                    lifecycleScope.launchWhenResumed {
                        viewerFragment.imageDataFlow.collect { data ->
                            Timber.d("Image data taken! Pose = ${data.syncPose}")
                        }
                    }
                }
            }
        }
    }
}