package com.birthday.reminder

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SwipeRevealLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var contentView: View? = null
    private var deleteButton: View? = null
    private var deleteBackground: View? = null
    
    private var startX = 0f
    private var startY = 0f
    private var lastX = 0f
    private var isDragging = false
    private var isSwipeStarted = false
    
    private val deleteButtonWidth = 80 * resources.displayMetrics.density // 80dp
    private var currentOffset = 0f
    
    var onDeleteClickListener: (() -> Unit)? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        contentView = findViewById(R.id.cardContent)
        deleteButton = findViewById(R.id.deleteButton)
        deleteBackground = findViewById(R.id.deleteBackground)
        
        // 添加调试日志
        if (contentView == null) {
            android.util.Log.w("SwipeRevealLayout", "contentView not found")
        }
        if (deleteButton == null) {
            android.util.Log.w("SwipeRevealLayout", "deleteButton not found")
        }
        if (deleteBackground == null) {
            android.util.Log.w("SwipeRevealLayout", "deleteBackground not found")
        }
        
        deleteButton?.setOnClickListener {
            onDeleteClickListener?.invoke()
            resetPosition()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x
                startY = ev.y
                lastX = startX
                isDragging = false
                isSwipeStarted = false
                return false // 先让子视图处理
            }
            
            MotionEvent.ACTION_MOVE -> {
                val deltaX = ev.x - startX
                val deltaY = ev.y - startY
                
                // 只有在明确的水平滑动时才拦截事件
                if (abs(deltaX) > abs(deltaY) && abs(deltaX) > 40) {
                    isSwipeStarted = true
                    return true // 拦截事件，由onTouchEvent处理
                }
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 确保释放状态，不拦截UP事件
                return false
            }
        }
        return false // 默认不拦截
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                lastX = startX
                isDragging = false
                isSwipeStarted = true // 如果到这里说明已经被拦截了
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isSwipeStarted) {
                    isDragging = true
                    val newOffset = currentOffset + (event.x - lastX)
                    updateOffset(max(-deleteButtonWidth, min(0f, newOffset)))
                    lastX = event.x
                    return true
                }
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                if (isDragging) {
                    val threshold = deleteButtonWidth / 3
                    if (currentOffset < -threshold) {
                        animateToOffset(-deleteButtonWidth)
                    } else {
                        animateToOffset(0f)
                    }
                    return true
                } else if (!isSwipeStarted) {
                    // 如果没有滑动，则认为是点击事件，让子视图处理
                    return false
                }
            }
        }
        return super.onTouchEvent(event)
    }
    
    private fun updateOffset(offset: Float) {
        currentOffset = offset
        contentView?.translationX = offset
        
        val progress = abs(offset) / deleteButtonWidth
        deleteBackground?.alpha = progress
        deleteButton?.alpha = progress
        
        if (progress > 0) {
            deleteBackground?.visibility = View.VISIBLE
            deleteButton?.visibility = View.VISIBLE
        } else {
            deleteBackground?.visibility = View.GONE
            deleteButton?.visibility = View.GONE
        }
    }
    
    private fun animateToOffset(targetOffset: Float) {
        contentView?.animate()
            ?.translationX(targetOffset)
            ?.setDuration(200)
            ?.withEndAction {
                currentOffset = targetOffset
                if (targetOffset == 0f) {
                    deleteBackground?.visibility = View.GONE
                    deleteButton?.visibility = View.GONE
                }
            }
            ?.start()
            
        val targetAlpha = if (targetOffset == 0f) 0f else 1f
        deleteBackground?.animate()?.alpha(targetAlpha)?.setDuration(200)?.start()
        deleteButton?.animate()?.alpha(targetAlpha)?.setDuration(200)?.start()
    }
    
    fun resetPosition() {
        animateToOffset(0f)
    }
}
