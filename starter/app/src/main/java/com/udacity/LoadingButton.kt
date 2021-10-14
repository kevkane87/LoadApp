package com.udacity


import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates
import android.graphics.RectF
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnRepeat

//Custom loading button which extends View class
class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var backgroundColour = 0
    private var loadingColour = 0
    private var textColour = 0
    private var circleColour = 0

    private var defaultText: CharSequence = ""
    private var loadingText: CharSequence = ""
    private var buttonText: CharSequence = ""

    //variable to store the progress of the animation
    private var loadingProgress = 0f

    private var valueAnimator = ValueAnimator()

    private var stopAnimation = false

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->

    }


    init {

        isClickable = true

        //assign custom attributes
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            backgroundColour = getColor(R.styleable.LoadingButton_buttonColor1, 0)
            loadingColour = getColor(R.styleable.LoadingButton_buttonColor2, 0)
            textColour = getColor(R.styleable.LoadingButton_textColor, 0)
            circleColour = getColor(R.styleable.LoadingButton_circleColor, 0)
            defaultText = getText(R.styleable.LoadingButton_buttonDefaultText)
            loadingText = getText(R.styleable.LoadingButton_buttonLoadingText)
        }
        buttonText = defaultText
    }

    //on button click the button is disabled, state changed to loading and animation started
    override fun performClick(): Boolean {
        super.performClick()
        isClickable = false
        if (buttonState == ButtonState.Completed) buttonState = ButtonState.Loading
        animateButton()

        return true
    }


    //Canvas used to draw button attributes
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawButtonBackground(canvas)
        drawLoadingBackground(canvas)
        drawText(canvas)
        drawLoadingCircle(canvas)
    }

    //calculation for button background default colour using canvas
    private fun drawButtonBackground(canvas: Canvas) {
        backgroundPaint.color = backgroundColour
        canvas.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), backgroundPaint)
    }

    //calculation for button background loading colour using canvas.
    //Draws over default background and uses loading progress variable to calculate width
    private fun drawLoadingBackground(canvas: Canvas) {
        backgroundPaint.color = loadingColour
        canvas.drawRect(
            0f,
            0f,
            widthSize.toFloat() * loadingProgress,
            heightSize.toFloat(),
            backgroundPaint
        )
    }

    //function to calculate coordinates and draw button text
    private fun drawText(canvas: Canvas) {
        textPaint.color = textColour

        //determines text to draw based on button state
        buttonText = if (buttonState == ButtonState.Loading) loadingText
        else defaultText

        val textYcoord =
            heightSize.toFloat() / 2 + ((textPaint.descent() - textPaint.ascent()) / 2) - textPaint.descent()
        canvas.drawText(buttonText.toString(), widthSize.toFloat() / 2, textYcoord, textPaint)
    }


    //function to calculate coordinates of loading circle and show progress using loading progress variable
    private fun drawLoadingCircle(canvas: Canvas) {

        val circleMarginFactor = 0.2f
        var circleLeft =
            widthSize.toFloat() - heightSize.toFloat() + heightSize.toFloat() * circleMarginFactor
        var circleTop = heightSize.toFloat() * circleMarginFactor
        var circleRight = widthSize.toFloat() - heightSize.toFloat() * circleMarginFactor
        var circleBottom = heightSize.toFloat() - heightSize.toFloat() * circleMarginFactor

        circlePaint.color = circleColour
        canvas.drawArc(
            circleLeft,
            circleTop,
            circleRight,
            circleBottom,
            0F,
            360F * loadingProgress,
            true,
            circlePaint
        )
    }


    //function to set animation parameters and start animation using value animator
    //the update listener updates loading progress variable with the current animation progress
    private fun animateButton() {

        valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 2000
        valueAnimator.addUpdateListener {
            loadingProgress = it.animatedValue as Float
            if (loadingProgress == 1f) {
                loadingProgress = 0f
            }
            invalidate()
        }

        //animation will repeat until cancelled and therefore will show longer downloads are still downloading
        valueAnimator.repeatCount = INFINITE
        valueAnimator.start()

        //after each repeat of animation, check to see if download has been completed
        //if so, cancel the repeat animation and change button state to completed
        valueAnimator.doOnRepeat {
            if (stopAnimation) {
                valueAnimator.cancel()
                buttonState = ButtonState.Completed
                invalidate()
                loadingProgress = 0f
                stopAnimation = false
            }
        }

        //enable button again after end of animation
        valueAnimator.doOnEnd {
            isClickable = true
        }
    }

    //function to be accessed my main activity to stop the animation repeat
    fun animationComplete() {
        stopAnimation = true
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)

    }
}