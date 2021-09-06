package city.augmented.ar_viewer

import android.content.Context
import androidx.lifecycle.ViewModel
import city.augmented.ar_viewer_lib.data.LocationRepository
import city.augmented.ar_viewer_lib.data.LocationRepositoryImpl
import city.augmented.ar_viewer_lib.entity.ImageData

class SimpleViewerViewModel : ViewModel() {
    private lateinit var locationRepository: LocationRepository

    fun initLocationRepository(context: Context) {
        locationRepository = LocationRepositoryImpl.createWithLocationClient(context)
    }

    fun prepareLocalizationRequest(imageData: ImageData) {

    }
}