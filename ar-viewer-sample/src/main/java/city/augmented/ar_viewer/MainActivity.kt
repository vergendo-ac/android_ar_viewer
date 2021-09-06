package city.augmented.ar_viewer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import city.augmented.ar_viewer.databinding.ActivityMainBinding
import city.augmented.ar_viewer_lib.components.PermissionsManager
import city.augmented.ar_viewer_lib.utils.replaceFragment

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var binding: ActivityMainBinding
    private val permissionsManager: PermissionsManager by lazy { PermissionsManager() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissions()
    }

    private fun checkPermissions() {
        if (!permissionsManager.isAllPermissionsGranted(applicationContext))
            permissionsManager.requestPermissions(this)
        else
            createViewerFragment()
    }

    private fun createViewerFragment() {
        supportFragmentManager.replaceFragment<SimpleViewerFragment>(R.id.simple_viewer_fragment_container)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkPermissions()
    }
}