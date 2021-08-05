package city.augmented.ar_viewer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import city.augmented.ar_viewer.databinding.ActivityMainBinding
import city.augmented.ar_viewer_lib.ARViewerFragment
import city.augmented.ar_viewer_lib.replaceFragment

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewerFragment: ARViewerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createViewerFragment(savedInstanceState)
    }

    private fun createViewerFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null)
            supportFragmentManager.replaceFragment<ARViewerFragment>(R.id.viewer_fragment_container) {
                viewerFragment = it
            }
    }
}