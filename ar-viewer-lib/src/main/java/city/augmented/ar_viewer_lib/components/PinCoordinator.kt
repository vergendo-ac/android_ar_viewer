package city.augmented.ar_viewer_lib.components

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import city.augmented.ar_viewer_lib.presentation.PinsView
import city.augmented.ar_viewer_lib.R
import city.augmented.ar_viewer_lib.entity.InfoSticker
import city.augmented.ar_viewer_lib.entity.InfoStickerType
import city.augmented.ar_viewer_lib.entity.Point
import city.augmented.ar_viewer_lib.entity.StickerMeta
import city.augmented.core.extensions.getColorCompat
import city.augmented.core.extensions.getDrawableCompat
import city.augmented.core.extensions.px
import com.doors.tourist2.utils.kotlinMath.toDistance

class PinCoordinator(
    private val context: Context,
    private val pinsView: PinsView
) {
    private var stickers = mutableMapOf<String, StickerMeta>()

    private var heads = mutableMapOf<String, View>()
    private var pins = mutableMapOf<String, View>()

    private var onScreen = mutableMapOf<String, Point>()
    private var headPositions = mutableMapOf<String, Rect>()

    private var selectedPin = ""

    private val margin = 16.px

    private val pinW = 70.px.toInt()
    private val pinH = 70.px.toInt()


    private fun layoutView(view: View): View {
        val wrapContentMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(wrapContentMeasureSpec, wrapContentMeasureSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        return view
    }

    private fun prepareHeadView(inflater: LayoutInflater, sticker: InfoSticker): View {
        return inflater.inflate(R.layout.view_sticker_head, null).apply {
            val name = findViewById<TextView>(R.id.name)
            val ratingText = findViewById<TextView>(R.id.ratingText)
            val ratingIcon = findViewById<ImageView>(R.id.ratingIcon)
            val type = findViewById<TextView>(R.id.type)
            val pinLayout = findViewById<ConstraintLayout>(R.id.pinLayout)
            name.text = sticker.stickerText

            if (sticker.rating > 0.0f) {
                ratingText.text = sticker.rating.toString()
            } else {
                ratingText.visibility = View.GONE
                ratingIcon.visibility = View.GONE
            }

            if (sticker.stickerType == InfoStickerType.OTHER || sticker.stickerType == InfoStickerType.TEXT)
                type.visibility = View.GONE
            type.text = sticker.stickerType.toString()

            pinLayout.backgroundTintList =
                ContextCompat.getColorStateList(context, getBackgroundColor(sticker.stickerType))
        }
    }

    private fun preparePinView(context: Context, sticker: InfoSticker): View {
        return ImageView(context).apply {
            if (selectedPin.isNotBlank()) {
                if (selectedPin == sticker.stickerId) {
                    setImageResource(getSelectedDrawable(sticker.stickerType))
                } else {
                    setColorFilter(Color.parseColor("#777777"), PorterDuff.Mode.MULTIPLY)
                    setImageDrawable(getResources(sticker.stickerType).first)
                }
            } else {
                setImageDrawable(getResources(sticker.stickerType).first)
            }
//                val padding = (getPadding(it.stickerType) * it.distance).toInt()
//                val padding = getPadding(it.stickerType).toInt()
//                setPadding(padding, padding, padding, padding)
        }
    }

    private fun createPinViews(stickers: List<StickerMeta>) {

        //First we need to determine which stickers is really new
        val newStickers = stickers
            .filter { !this.stickers.containsKey(it.stickerInfo.stickerId) }
            .map { it.stickerInfo.stickerId to it }
            .toMap().toMutableMap()

        //Next we add them to our local cache
        this.stickers.putAll(newStickers)


        val inflater = LayoutInflater.from(context)
        //Layout views related to this sticker
        newStickers.forEach { (id, sticker) ->
            //Again only views that not created yet
            if (heads.containsKey(id) && pins.containsKey(id)) return

            val head = prepareHeadView(inflater, sticker.stickerInfo)
            heads[id] = layoutView(head)

            val pin = preparePinView(context, sticker.stickerInfo)
            pin.layout(0, 0, pinW, pinH)
            pins[id] = pin
        }
    }

    fun update(newLocations: List<StickerMeta>) {
        this.stickers.values.onEach { it.shouldShowFull = false }
        newLocations.onEach { it.shouldShowFull = false }
            .filter { isOnScreen(it.position.screenProjection) }
            .filter { it.stickerInfo.stickerType != InfoStickerType.VIDEO }
            .sortedBy { it.position.worldPosition.toDistance() }
            .take(5)
            .forEach { it.shouldShowFull = true }

        createPinViews(newLocations)

        onScreen =
            newLocations.map {
                it.stickerInfo.stickerId to it.position.screenProjection
            }
                .toMap().filter { isOnScreen(it.value.x, it.value.y) }
                .toList()
                .sortedBy { (_, value) -> value.x }
                .toMap().toMutableMap()

        headPositions = calculateHeadPositions(onScreen).toMutableMap()
        pinsView.clear()
        onScreen.forEach { (id, pin) ->
            if (pins[id] != null && stickers[id] != null) {
                pinsView.addSticker(
                    getPinBounds(
                        pin.x,
                        pin.y
                    ),
                    pins[id]!!,
                    headPositions[id],
                    heads[id],
                    getResources(stickers[id]!!.stickerInfo.stickerType),
                    id
                )
            }
        }
    }

    /*fun selectPin(pinId: String) {
        selectedPin = pinId
        pins.clear()
        onScreen.forEach {
            val pin = ImageView(context).apply {
                if (selectedPin.isNotBlank()) {
                    if (selectedPin == it.stickerId) {
                        setImageResource(getSelectedDrawable(it.stickerType))
                    } else {
                        setColorFilter(Color.parseColor("#777777"), PorterDuff.Mode.MULTIPLY)
                        setImageDrawable(getResources(it.stickerType).first)
                    }
                } else {
                    setImageDrawable(getResources(it.stickerType).first)
                }
                val padding = getPadding(it.stickerType).toInt()
                setPadding(padding, padding, padding, padding)
            }
            pin.layout(0, 0, pinW, pinH)
            pins[it.stickerId] = pin
        }
        pinDisplay.clear()
        for ((i, sticker) in onScreen.withIndex()) {
            pinDisplay.addSticker(
                getPinBounds(
                    sticker.stickerLocation[0],
                    sticker.stickerLocation[1]
                ),
                pins[i],
                headPositions[i],
                heads[i],
                getResources(sticker.stickerType), sticker
            )
        }
        pinDisplay.invalidate()
    }*/

    private fun getPinBounds(x: Int, y: Int) =
        getBounds(x, y, pinW, pinH)

    private fun isOnScreen(point: Point) =
        isOnScreen(point.x, point.y)

    private fun isOnScreen(x: Float, y: Float) =
        isOnScreen(x.toInt(), y.toInt())

    private fun isOnScreen(x: Int, y: Int) =
        (x > 0 && x < pinsView.width) && (y > 0 && y < pinsView.height)


    private fun getBounds(x: Int, y: Int, w: Int, h: Int): Rect {
        val halfW = w / 2
        val halfH = h / 2
        return Rect(
            x - halfW, y - halfH,
            x + halfW, y + halfH
        )
    }

    private fun calculateHeadPositions(locations: Map<String, Point>): Map<String, Rect> {
        val processed = mutableMapOf<String, Rect>()
        for ((id, location) in locations) {
            if (stickers[id]?.shouldShowFull == true)
                heads[id]?.let { head ->
                    if (processed.isEmpty()) {
                        processed[id] =
                            getBounds(
                                location.x,
                                (head.height / 2 + margin).toInt(),
                                head.width,
                                head.height
                            )

                    } else {
                        val bounds =
                            placeHead(
                                getBounds(
                                    location.x,
                                    (head.height / 2 + margin).toInt(),
                                    head.width,
                                    head.height
                                ), processed
                            )
                        processed[id] = bounds
                    }
                }
        }
        return processed
    }

    private fun placeHead(bounds: Rect, processed: Map<String, Rect>): Rect {
        var intersected = false
        var intersectedHeight = 0
        for (item in processed.values) {
            if ((bounds.centerY() >= item.centerY() && bounds.top <= item.bottom ||
                        bounds.centerY() <= item.centerY() && bounds.bottom >= item.top) &&
                (bounds.centerX() >= item.centerX() && bounds.left <= item.right ||
                        bounds.centerX() <= item.centerX() && bounds.right >= item.left)
            ) {
                intersected = true
                intersectedHeight = item.bottom
            }
        }
        return if (intersected)
            placeHead(
                getBounds(
                    bounds.centerX(),
                    intersectedHeight + (bounds.height() / 2 + margin).toInt(),
                    bounds.width(),
                    bounds.height()
                ),
                processed
            )
        else bounds
    }

    fun clearPins() {
        stickers.clear()
        onScreen.clear()
        headPositions.clear()
        heads.clear()
        pins.clear()
        selectedPin = ""
        pinsView.clear()
    }

    private fun getPadding(type: InfoStickerType) = when (type) {
        InfoStickerType.REST -> 3.px
        InfoStickerType.SHOP -> 0F
        InfoStickerType.PLACE -> 3.px
        else -> 7.px
    }

    private fun getResources(type: InfoStickerType) = when (type) {
        InfoStickerType.REST -> arrayOf(R.drawable.pin_ic_food, R.color.pin_rest_color)
        InfoStickerType.SHOP -> arrayOf(R.drawable.pin_ic_shop, R.color.pin_shop_color)
        InfoStickerType.PLACE -> arrayOf(R.drawable.pin_ic_attraction, R.color.pin_place_color)
        else -> arrayOf(R.drawable.pin_ic_other, R.color.pin_other_color)
    }.let { data ->
        Pair(getDrawable(data[0]), getColor(data[1]))
    }

    private fun getSelectedDrawable(type: InfoStickerType) = when (type) {
        InfoStickerType.REST -> R.drawable.pin_ic_food
        InfoStickerType.SHOP -> R.drawable.pin_ic_shop
        InfoStickerType.PLACE -> R.drawable.pin_ic_attraction
        else -> R.drawable.pin_ic_other
    }

    private fun getBackgroundColor(type: InfoStickerType) = when (type) {
        InfoStickerType.REST -> R.color.pin_rest_color
        InfoStickerType.SHOP -> R.color.pin_shop_color
        InfoStickerType.PLACE -> R.color.pin_place_color
        else -> R.color.pin_other_color
    }

    private fun getDrawable(id: Int) = context.getDrawableCompat(id)
    private fun getColor(id: Int) = context.getColorCompat(id)
}
