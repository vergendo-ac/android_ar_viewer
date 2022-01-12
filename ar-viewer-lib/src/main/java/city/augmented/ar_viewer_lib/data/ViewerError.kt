package city.augmented.ar_viewer_lib.data

sealed class ViewerError(message: String) {
    object AcquireImageError : ViewerError("Unable to acquire image")
}