package com.birthday.reminder

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemMoveCallback(
    private val adapter: ItemMoveInterface,
    private val dragOptimizedAnimator: DragOptimizedItemAnimator? = null
) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean = true

    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // 不支持直接滑动删除
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // 拖拽开始
            dragOptimizedAnimator?.setDragging(true)
            if (viewHolder is ItemTouchHelperViewHolder) {
                viewHolder.onItemSelected()
            }
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            // 拖拽结束
            dragOptimizedAnimator?.setDragging(false)
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder is ItemTouchHelperViewHolder) {
            viewHolder.onItemClear()
        }
        // 拖拽完成后保存新顺序
        adapter.onMoveFinished()
    }
    
    override fun getAnimationDuration(recyclerView: RecyclerView, animationType: Int, animateDx: Float, animateDy: Float): Long {
        // 提供适当的动画时间以改善视觉反馈
        return when (animationType) {
            ItemTouchHelper.ANIMATION_TYPE_DRAG -> 200
            ItemTouchHelper.ANIMATION_TYPE_SWIPE_CANCEL -> 150
            ItemTouchHelper.ANIMATION_TYPE_SWIPE_SUCCESS -> 150
            else -> super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
        }
    }
    
    override fun interpolateOutOfBoundsScroll(
        recyclerView: RecyclerView,
        viewSize: Int,
        viewSizeOutOfBounds: Int,
        totalSize: Int,
        msSinceStartScroll: Long
    ): Int {
        // 改善拖拽到边缘时的滚动体验
        val direction = Math.signum(viewSizeOutOfBounds.toFloat()).toInt()
        return Math.round(Math.max(0.3f, Math.min(1.0f, Math.abs(viewSizeOutOfBounds).toFloat() / viewSize)) * direction * 10)
    }
}

interface ItemMoveInterface {
    fun onRowMoved(fromPosition: Int, toPosition: Int)
    fun onMoveFinished()
}

interface ItemTouchHelperViewHolder {
    fun onItemSelected()
    fun onItemClear()
}
