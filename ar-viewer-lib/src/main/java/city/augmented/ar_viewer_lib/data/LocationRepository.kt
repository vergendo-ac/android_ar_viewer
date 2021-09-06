package city.augmented.ar_viewer_lib.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import city.augmented.core.providers.GmsLocationProvider
import city.augmented.core.providers.LocationProvider
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.delay

interface LocationRepository {
    val locationLiveData: LiveData<Location>
    suspend fun getLocation(): Location
}

class LocationRepositoryImpl(
    locationProvider: LocationProvider
) : LocationRepository {
    private var _locationLiveData = MutableLiveData<Location>()
    override val locationLiveData: LiveData<Location>
        get() = _locationLiveData
    private var locationCache: Location = Location("null")
    private var started = false

    init {
        locationProvider.subscribe {
            updateLocation(it)
        }
    }

    private fun updateLocation(location: Location) {
        locationCache = location
        _locationLiveData.value = location
    }

    override suspend fun getLocation(): Location {
        while (locationCache.provider == "null")
            delay(LOCATION_CHECK_DELAY)
        return locationCache
    }

    companion object {
        const val LOCATION_CHECK_DELAY = 500L

        @SuppressLint("VisibleForTests")
        fun createWithLocationClient(context: Context) =
            LocationRepositoryImpl(GmsLocationProvider(FusedLocationProviderClient(context)))
    }
}