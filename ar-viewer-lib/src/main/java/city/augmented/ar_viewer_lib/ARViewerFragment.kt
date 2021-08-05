package city.augmented.ar_viewer_lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import city.augmented.ar_viewer_lib.databinding.FragmentArViewerBinding

class ARViewerFragment : Fragment() {
    private lateinit var binding: FragmentArViewerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

}