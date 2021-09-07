package city.augmented.ar_viewer

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import city.augmented.api.LocalizerApiClient
import city.augmented.api.ifLeft
import city.augmented.api.ifRight
import city.augmented.api.infrastructure.ACApiClient
import city.augmented.api.model.GpsDto
import city.augmented.api.model.ImageDescriptionDto
import city.augmented.ar_viewer_lib.entity.ArObject
import city.augmented.ar_viewer_lib.entity.ImageData
import city.augmented.core.providers.GmsLocationProvider
import city.augmented.core.providers.LocationProvider
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class SimpleViewerViewModel : ViewModel() {
    private lateinit var locationProvider: LocationProvider
    private val localizerApiClient: LocalizerApiClient = LocalizerApiClient(
        ACApiClient(
//        okHttpClientBuilder = OkHttpClient().newBuilder()
//            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        )
    )

    private var _errorMessages: MutableLiveData<String> = MutableLiveData()
    val errorMessages: LiveData<String> = _errorMessages

    private var _localizationResult: MutableLiveData<List<ArObject>> = MutableLiveData()
    val localizationResult: LiveData<List<ArObject>> = _localizationResult

    fun initProviders(context: Context) {
        locationProvider = GmsLocationProvider(FusedLocationProviderClient(context))
    }

    fun prepareLocalizationRequest(imageData: ImageData) = viewModelScope.launch {
        Timber.i("Attempt to send localize request")
        val loc = locationProvider.locationsFlow.first()
        localizerApiClient.localize(
            ImageDescriptionDto(GpsDto(loc.latitude, loc.longitude)),
            imageData.imageBytes
        ).ifLeft {
            Timber.d("Not localized...")
            _errorMessages.value = it.message
        }.ifRight {
            Timber.d("Localized!")
            Timber.d(it.toString())
            _localizationResult.value = it.toLocalEntities(imageData.syncPose)
        }
    }
}