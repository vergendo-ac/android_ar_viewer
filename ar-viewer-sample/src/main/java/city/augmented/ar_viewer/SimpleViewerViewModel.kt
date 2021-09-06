package city.augmented.ar_viewer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import city.augmented.ar_viewer_lib.entity.ImageData
import city.augmented.ar_viewer_lib.utils.kotlinMath.Float3
import city.augmented.core.providers.GmsLocationProvider
import city.augmented.core.providers.GravityProvider
import city.augmented.core.providers.GravitySensorProvider
import city.augmented.core.providers.LocationProvider
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class SimpleViewerViewModel : ViewModel() {
    private lateinit var locationProvider: LocationProvider
    private lateinit var gravityProvider: GravityProvider

    fun initProviders(context: Context) {
        locationProvider = GmsLocationProvider(FusedLocationProviderClient(context))
        gravityProvider = GravitySensorProvider(context)
    }

    fun prepareLocalizationRequest(imageData: ImageData) = viewModelScope.launch {
        val sensors = getLocationAndGVector()
        Timber.d("Updated loc and vector: ${sensors.first} | ${sensors.second}")
    }

    private suspend fun getLocationAndGVector() = locationProvider.locationsFlow
        .combine(gravityProvider.vectorsFlow.map {
            Float3(
                it[0],
                it[1],
                it[2]
            )
        }) { l, g -> l to g }.first()
}