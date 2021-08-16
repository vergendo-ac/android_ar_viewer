package city.augmented.ar_viewer_lib

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import city.augmented.core.extensions.px

class PinDisplay(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val pinViews = mutableMapOf<String, View>()
    private val pinRects = mutableMapOf<String, Rect>()
    private val headViews = mutableMapOf<String, View>()
    private val headRects = mutableMapOf<String, Rect>()
    private val resourcesId = mutableMapOf<String, Pair<Drawable?, Int>>()
    private val dashThickness = 2.px

    private var onClickListener: (String) -> Unit = {}
    private var selectedPin: String = ""

    private var linePaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = dashThickness
    }

    private val headPaint = Paint().apply {
        isAntiAlias = true
    }

    fun clear() {
        pinViews.clear()
        pinRects.clear()
        headViews.clear()
        headRects.clear()
        resourcesId.clear()
        selectedPin = ""
        invalidate()
    }

    fun setOnclickListener(listener: (String) -> Unit) {
        onClickListener = listener
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
                MotionEvent.ACTION_UP -> {
                    pinRects.filterValues { value ->
                        value.contains(
                            event.x.toInt(),
                            event.y.toInt()
                        )
                    }.forEach { (key, _) -> onClickListener(key) }

                    headRects.filterValues { value ->
                        value.contains(
                            event.x.toInt(),
                            event.y.toInt()
                        )
                    }.forEach { (key, _) -> onClickListener(key) }
                }
            }
            return true
        }
        return true
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
}