package com.zhouyu.pet_science.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.zhouyu.pet_science.R
import kotlin.math.min
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.createBitmap

/**
 * 圆形图片
 */
@SuppressLint("AppCompatCustomView")
class CircleImageView : ImageView {
    private val mDrawableRect = RectF()
    private val mBorderRect = RectF()

    private val mShaderMatrix = Matrix()
    private val mBitmapPaint = Paint()
    private val mBorderPaint = Paint()
    private val mFillPaint = Paint()

    private var mBorderColor = DEFAULT_BORDER_COLOR
    private var mBorderWidth = DEFAULT_BORDER_WIDTH
    private var mFillColor = DEFAULT_FILL_COLOR

    private var mBitmap: Bitmap? = null
    private var mBitmapShader: BitmapShader? = null
    private var mBitmapWidth = 0
    private var mBitmapHeight = 0

    private var mDrawableRadius = 0f
    private var mBorderRadius = 0f

    private var mColorFilter: ColorFilter? = null

    private var mReady = false
    private var mSetupPending = false
    private var mBorderOverlay = false
    private var mDisableCircularTransformation = false

    constructor(context: Context?) : super(context) {
        init()
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int = 0) : super(
        context,
        attrs,
        defStyle
    ) {
        context.withStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0) {

            mBorderWidth = getDimensionPixelSize(
                R.styleable.CircleImageView_civ_border_width,
                DEFAULT_BORDER_WIDTH
            )
            mBorderColor =
                getColor(R.styleable.CircleImageView_civ_border_color, DEFAULT_BORDER_COLOR)
            mBorderOverlay =
                getBoolean(R.styleable.CircleImageView_civ_border_overlay, DEFAULT_BORDER_OVERLAY)
            mFillColor = getColor(R.styleable.CircleImageView_civ_fill_color, DEFAULT_FILL_COLOR)

        }

        init()
    }

    private fun init() {
        super.setScaleType(SCALE_TYPE)
        mReady = true

        if (mSetupPending) {
            setup()
            mSetupPending = false
        }
    }

    override fun getScaleType(): ScaleType {
        return SCALE_TYPE
    }

    override fun setScaleType(scaleType: ScaleType) {
        require(scaleType == SCALE_TYPE) { String.format("ScaleType %s not supported.", scaleType) }
    }

    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        require(!adjustViewBounds) { "adjustViewBounds not supported." }
    }

    override fun onDraw(canvas: Canvas) {
        if (mDisableCircularTransformation) {
            super.onDraw(canvas)
            return
        }

        if (mBitmap == null) {
            return
        }

        if (mFillColor != Color.TRANSPARENT) {
            canvas.drawCircle(
                mDrawableRect.centerX(),
                mDrawableRect.centerY(),
                mDrawableRadius,
                mFillPaint
            )
        }
        canvas.drawCircle(
            mDrawableRect.centerX(),
            mDrawableRect.centerY(),
            mDrawableRadius,
            mBitmapPaint
        )
        if (mBorderWidth > 0) {
            canvas.drawCircle(
                mBorderRect.centerX(),
                mBorderRect.centerY(),
                mBorderRadius,
                mBorderPaint
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setup()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        setup()
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        setup()
    }

    var borderColor: Int
        get() = mBorderColor
        set(borderColor) {
            if (borderColor == mBorderColor) {
                return
            }

            mBorderColor = borderColor
            mBorderPaint.color = mBorderColor
            invalidate()
        }

    @Deprecated("Use {@link #setBorderColor(int)} instead")
    fun setBorderColorResource(@ColorRes borderColorRes: Int) {
        borderColor = context.getColor(borderColorRes)
    }

    @get:Deprecated("Fill color support is going to be removed in the future")
    @set:Deprecated("Fill color support is going to be removed in the future")
    var fillColor: Int
        /**
         * Return the color drawn behind the circle-shaped drawable.
         *
         * @return The color drawn behind the drawable
         *
         */
        get() = mFillColor
        /**
         * Set a color to be drawn behind the circle-shaped drawable. Note that
         * this has no effect if the drawable is opaque or no drawable is set.
         *
         * @param fillColor The color to be drawn behind the drawable
         *
         */
        set(fillColor) {
            if (fillColor == mFillColor) {
                return
            }

            mFillColor = fillColor
            mFillPaint.color = fillColor
            invalidate()
        }

    /**
     * Set a color to be drawn behind the circle-shaped drawable. Note that
     * this has no effect if the drawable is opaque or no drawable is set.
     *
     * @param fillColorRes The color resource to be resolved to a color and
     * drawn behind the drawable
     *
     */
    @Deprecated("Fill color support is going to be removed in the future")
    fun setFillColorResource(@ColorRes fillColorRes: Int) {
        fillColor = context.getColor(fillColorRes)
    }

    var borderWidth: Int
        get() = mBorderWidth
        set(borderWidth) {
            if (borderWidth == mBorderWidth) {
                return
            }

            mBorderWidth = borderWidth
            setup()
        }

    var isBorderOverlay: Boolean
        get() = mBorderOverlay
        set(borderOverlay) {
            if (borderOverlay == mBorderOverlay) {
                return
            }

            mBorderOverlay = borderOverlay
            setup()
        }

    var isDisableCircularTransformation: Boolean
        get() = mDisableCircularTransformation
        set(disableCircularTransformation) {
            if (mDisableCircularTransformation == disableCircularTransformation) {
                return
            }

            mDisableCircularTransformation = disableCircularTransformation
            initializeBitmap()
        }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        initializeBitmap()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        initializeBitmap()
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        super.setImageResource(resId)
        initializeBitmap()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        initializeBitmap()
    }

    override fun setColorFilter(cf: ColorFilter) {
        if (cf === mColorFilter) {
            return
        }

        mColorFilter = cf
        applyColorFilter()
        invalidate()
    }

    override fun getColorFilter(): ColorFilter {
        return mColorFilter!!
    }

    private fun applyColorFilter() {
        mBitmapPaint.setColorFilter(mColorFilter)
    }

    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        try {
            val bitmap = if (drawable is ColorDrawable) {
                createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG)
            } else {
                createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, BITMAP_CONFIG)
            }

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun initializeBitmap() {
        mBitmap = if (mDisableCircularTransformation) {
            null
        } else {
            getBitmapFromDrawable(drawable)
        }
        setup()
    }

    private fun setup() {
        if (!mReady) {
            mSetupPending = true
            return
        }

        if (width == 0 && height == 0) {
            return
        }

        if (mBitmap == null) {
            invalidate()
            return
        }

        mBitmapShader = BitmapShader(mBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        mBitmapPaint.isAntiAlias = true
        mBitmapPaint.setShader(mBitmapShader)

        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = mBorderColor
        mBorderPaint.strokeWidth = mBorderWidth.toFloat()

        mFillPaint.style = Paint.Style.FILL
        mFillPaint.isAntiAlias = true
        mFillPaint.color = mFillColor

        mBitmapHeight = mBitmap!!.height
        mBitmapWidth = mBitmap!!.width

        mBorderRect.set(calculateBounds())
        mBorderRadius = min(
            ((mBorderRect.height() - mBorderWidth) / 2.0f).toDouble(),
            ((mBorderRect.width() - mBorderWidth) / 2.0f).toDouble()
        ).toFloat()

        mDrawableRect.set(mBorderRect)
        if (!mBorderOverlay && mBorderWidth > 0) {
            mDrawableRect.inset(mBorderWidth - 1.0f, mBorderWidth - 1.0f)
        }
        mDrawableRadius = min(
            (mDrawableRect.height() / 2.0f).toDouble(),
            (mDrawableRect.width() / 2.0f).toDouble()
        ).toFloat()

        applyColorFilter()
        updateShaderMatrix()
        invalidate()
    }

    private fun calculateBounds(): RectF {
        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom

        val sideLength = min(availableWidth.toDouble(), availableHeight.toDouble()).toInt()

        val left = paddingLeft + (availableWidth - sideLength) / 2f
        val top = paddingTop + (availableHeight - sideLength) / 2f

        return RectF(left, top, left + sideLength, top + sideLength)
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx = 0f
        var dy = 0f

        mShaderMatrix.set(null)

        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / mBitmapHeight.toFloat()
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f
        } else {
            scale = mDrawableRect.width() / mBitmapWidth.toFloat()
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f
        }

        mShaderMatrix.setScale(scale, scale)
        mShaderMatrix.postTranslate(
            (dx + 0.5f).toInt() + mDrawableRect.left,
            (dy + 0.5f).toInt() + mDrawableRect.top
        )

        mBitmapShader!!.setLocalMatrix(mShaderMatrix)
    }

    companion object {
        private val SCALE_TYPE = ScaleType.CENTER_CROP

        private val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        private const val COLORDRAWABLE_DIMENSION = 2

        private const val DEFAULT_BORDER_WIDTH = 0
        private const val DEFAULT_BORDER_COLOR = Color.BLACK
        private const val DEFAULT_FILL_COLOR = Color.TRANSPARENT
        private const val DEFAULT_BORDER_OVERLAY = false
    }
}
