package city.augmented.ar_viewer_lib.presentation

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import city.augmented.core.extensions.px
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.abs

class PinsView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val pinViews = mutableMapOf<String, View>()
    private val pinRects = mutableMapOf<String, Rect>()
    private val headViews = mutableMapOf<String, View>()
    private val headRects = mutableMapOf<String, Rect>()
    private val resourcesId = mutableMapOf<String, Pair<Drawable?, Int>>()
    private var selectedPin: String = ""

    fun clear() {
        pinViews.clear()
        pinRects.clear()
        headViews.clear()
        headRects.clear()
        resourcesId.clear()
        selectedPin = ""
        invalidate()
    }

    private val dashThickness = 2.px
    private val longClickDelay = 2000L

    private var lastActionDownTime = 0L
    private var lastActionDownX = -1
    private var lastActionDownY = -1
    private var lastKnownX = -1
    private var lastKnownY = -1
    private var fingerStillDown = false

    private var linePaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = dashThickness
    }

    private var onClickListener: (String) -> Unit = {}
    private var onLongClickListener: (String) -> Unit = {}
    private var timer: Timer? = null

    fun setOnclickListener(listener: (String) -> Unit) {
        onClickListener = listener
    }

    fun setOnLongClickListener(listener: (String) -> Unit) {
        onLongClickListener = listener
    }

    fun addSticker(
        pinBounds: Rect,
        pinView: View,
        headBounds: Rect?,
        headView: View?,
        resources: Pair<Drawable?, Int>,
        stickerId: String
    ) {
        pinRects[stickerId] = pinBounds
        pinViews[stickerId] = pinView
        headView?.let {
            headViews[stickerId] = it
        }
        headBounds?.let {
            headRects[stickerId] = it
        }
        resourcesId[stickerId] = resources
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    lastKnownX = event.x.toInt()
                    lastKnownY = event.y.toInt()
                }
                MotionEvent.ACTION_DOWN -> {
                    lastActionDownTime = System.currentTimeMillis()
                    lastActionDownX = event.x.toInt()
                    lastActionDownY = event.y.toInt()
                    startChecking()
                    fingerStillDown = true
                }
                MotionEvent.ACTION_UP -> {
                    fingerStillDown = false
                    timer?.cancel()
                    val currentActionUpTime = System.currentTimeMillis()
                    if (currentActionUpTime - lastActionDownTime < longClickDelay) {
                        onPin(event.x.toInt(), event.y.toInt(), onClickListener)
                        onHead(event.x.toInt(), event.y.toInt(), onClickListener)
                    }
                }
            }
            return true
        }
        return true
    }

    private fun onPin(x: Int, y: Int, onClick: (String) -> Unit) {
        pinRects
            .filterValues { value -> value.contains(x, y) }
            .forEach { (key, _) -> onClick(key) }
    }

    private fun onHead(x: Int, y: Int, onClick: (String) -> Unit) {
        headRects
            .filterValues { value -> value.contains(x, y) }
//            .firstNotNullOfOrNull { it.key }
//            ?.let { key -> onClick(key) }
            .forEach { (key, _) -> onClick(key) }
    }

    private fun startChecking() {
        val handler = Handler(Looper.getMainLooper())
        timer = Timer().apply {
            schedule(longClickDelay) {
                if (
                    fingerStillDown
                    && abs(lastKnownX - lastActionDownX) < 30
                    && abs(lastKnownY - lastActionDownY) < 30
                ) {
                    onPin(lastActionDownX, lastActionDownY) {
                        handler.post { onLongClickListener(it) }
                    }
                    onHead(lastActionDownX, lastActionDownY) {
                        handler.post { onLongClickListener(it) }
                    }
                }
                fingerStillDown = false
            }
        }
    }


    override fun onDraw(canvas: Canvas?) {
        if (pinViews.isEmpty()) return
        canvas?.let {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            drawLines(pinRects, headRects, resourcesId, canvas, linePaint)
            pinViews.forEach { (key, view) ->
                canvas.save()
                canvas.translate(
                    pinRects.getValue(key).exactCenterX() - pinViews.getValue(key).width / 2,
                    pinRects.getValue(key).exactCenterY() - pinViews.getValue(key).height / 2
                )
                // update size of pin view
                pinRects[key]?.let { pinRect ->
                    view.layout(pinRect.left, pinRect.top, pinRect.right, pinRect.bottom)
                }
                // Draw the View and clear the translation
                view.draw(canvas)
                canvas.restore()
            }
            headViews.forEach { (key, _) ->
                if (headRects.containsKey(key))
                    drawHead(key, canvas)
            }
        }
    }

    private fun drawHead(key: String, canvas: Canvas) {
        canvas.save()
        canvas.translate(
            headRects.getValue(key).exactCenterX() - headViews.getValue(key).width / 2,
            headRects.getValue(key).exactCenterY() - headViews.getValue(key).height / 2
        )
        // Draw the View and clear the translation
        headViews.getValue(key).draw(canvas)
        canvas.restore()
    }

    private fun drawLines(
        pinRects: Map<String, Rect>,
        headRects: Map<String, Rect>,
        colors: Map<String, Pair<Drawable?, Int>>,
        canvas: Canvas,
        paint: Paint
    ) {
        headRects.forEach { (key, headRect) ->
            drawLine(
                PointF(
                    pinRects.getValue(key).exactCenterX(),
                    pinRects.getValue(key).exactCenterY()
                ),
                PointF(headRect.exactCenterX(), headRect.exactCenterY() + (headRect.height() / 2)),
                colors.getValue(key).second,
                canvas,
                paint
            )
        }
    }

    private fun drawLine(start: PointF, finish: PointF, color: Int, canvas: Canvas, paint: Paint) {
        paint.color = color
        canvas.drawLine(start.x, start.y, finish.x, finish.y, paint)
    }

    override fun onDetachedFromWindow() {
        timer?.cancel()
        super.onDetachedFromWindow()
    }

}