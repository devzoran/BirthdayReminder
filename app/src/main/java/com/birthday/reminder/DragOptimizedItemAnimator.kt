package com.birthday.reminder

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

/**
 * 自定义ItemAnimator，用于优化拖拽体验
 * 在拖拽完成时禁用多余的change动画
 */
class DragOptimizedItemAnimator : DefaultItemAnimator() {
    
    private var isDragging = false
    
    fun setDragging(dragging: Boolean) {
        isDragging = dragging
    }
    
    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        // 在拖拽过程中禁用change动画，减少不必要的视觉干扰
        if (isDragging) {
            dispatchChangeFinished(oldHolder, true)
            if (newHolder != oldHolder) {
                dispatchChangeFinished(newHolder, false)
            }
            return false
        }
        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY)
    }
    
    override fun animateMove(
        holder: RecyclerView.ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        // 拖拽时的移动动画保持正常
        return super.animateMove(holder, fromX, fromY, toX, toY)
    }
}
