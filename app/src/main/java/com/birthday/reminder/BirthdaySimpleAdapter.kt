package com.birthday.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.birthday.reminder.databinding.ItemBirthdaySimpleBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class BirthdaySimpleAdapter(
    private val onItemClick: (Birthday) -> Unit,
    private val onDeleteClick: (Birthday) -> Unit,
    private val onToggleReminder: (Birthday) -> Unit
) : ListAdapter<Birthday, BirthdaySimpleAdapter.BirthdayViewHolder>(BirthdayDiffCallback()), ItemMoveInterface {

    private var tempList: MutableList<Birthday> = mutableListOf()
    private var dragDropComplete: (() -> Unit)? = null
    
    fun setOnMoveFinishedListener(listener: () -> Unit) {
        dragDropComplete = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirthdayViewHolder {
        val binding = ItemBirthdaySimpleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BirthdayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BirthdayViewHolder, position: Int) {
        // 添加安全检查，确保位置有效
        if (position >= 0 && position < getItemCount()) {
            holder.bind(getItem(position))
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        // 使用临时列表来处理拖拽，避免submitList导致的动画中断
        if (tempList.isEmpty()) {
            tempList = currentList.toMutableList()
        }
        
        // 在临时列表中移动项目
        val item = tempList.removeAt(fromPosition)
        tempList.add(toPosition, item)
        
        // 使用notifyItemMoved提供平滑的拖拽动画
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onMoveFinished() {
        // 拖拽完成后，立即将临时列表提交为正式数据，确保数据一致性
        if (tempList.isNotEmpty()) {
            val finalList = tempList.toList()
            // 立即提交数据但不触发动画（因为UI已经是最终状态）
            submitList(finalList)
            tempList.clear()
            dragDropComplete?.invoke()
        } else {
            dragDropComplete?.invoke()
        }
    }
    
    // 在拖拽期间临时重写数据获取方法
    override fun getItem(position: Int): Birthday {
        return if (tempList.isNotEmpty() && position < tempList.size) {
            tempList[position]
        } else {
            super.getItem(position)
        }
    }
    
    // 在拖拽期间临时重写数量获取方法
    override fun getItemCount(): Int {
        return if (tempList.isNotEmpty()) {
            tempList.size
        } else {
            super.getItemCount()
        }
    }
    
    // 获取当前显示的完整列表（包括拖拽期间的临时状态）
    fun getCurrentDisplayList(): List<Birthday> {
        return if (tempList.isNotEmpty()) {
            tempList.toList()
        } else {
            currentList
        }
    }
    
    // 无动画地完成拖拽结果，此方法现在主要用于确保数据同步
    fun finalizeDragResult(finalList: List<Birthday>) {
        // 由于onMoveFinished已经处理了数据提交，这里只需要确保列表是最新的
        if (currentList != finalList) {
            submitList(finalList)
        }
    }

    class BirthdayDiffCallback : DiffUtil.ItemCallback<Birthday>() {
        override fun areItemsTheSame(oldItem: Birthday, newItem: Birthday): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Birthday, newItem: Birthday): Boolean {
            return oldItem == newItem
        }
    }

    inner class BirthdayViewHolder(
        private val binding: ItemBirthdaySimpleBinding
    ) : RecyclerView.ViewHolder(binding.root), ItemTouchHelperViewHolder {

        fun bind(birthday: Birthday) {
            // 设置头像首字母
            binding.avatarText.text = if (birthday.name.isNotEmpty()) {
                birthday.name.first().toString().uppercase()
            } else {
                "?"
            }
            
            binding.nameText.text = birthday.name
            
            // 显示关系和日期信息
            val relationInfo = if (birthday.relationship.isNotEmpty()) {
                "${birthday.relationship} • ${birthday.gregorianDate.substring(5)}" // 只显示月-日
            } else {
                birthday.gregorianDate.substring(5) // 只显示月-日
            }
            binding.dateText.text = relationInfo

            // 计算剩余天数
            val today = LocalDate.now()
            val dateParts = birthday.gregorianDate.split("-")
            val birthdayThisYear = LocalDate.of(today.year, dateParts[1].toInt(), dateParts[2].toInt())
            val birthdayNextYear = birthdayThisYear.plusYears(1)
            
            val nextBirthday = if (birthdayThisYear.isAfter(today) || birthdayThisYear.isEqual(today)) {
                birthdayThisYear
            } else {
                birthdayNextYear
            }
            
            val daysUntil = ChronoUnit.DAYS.between(today, nextBirthday).toInt()
            binding.countdownText.text = if (daysUntil == 0) {
                "今天"
            } else {
                "${daysUntil}天"
            }

            // 设置提醒状态指示器
            if (birthday.isEnabled) {
                binding.reminderStatusIcon.visibility = android.view.View.VISIBLE
                binding.reminderStatusIcon.alpha = 1.0f
            } else {
                binding.reminderStatusIcon.visibility = android.view.View.VISIBLE
                binding.reminderStatusIcon.alpha = 0.3f
            }
            
            // 长按提醒图标可以切换提醒状态
            binding.reminderStatusIcon.setOnLongClickListener {
                val updatedBirthday = birthday.copy(isEnabled = !birthday.isEnabled)
                onToggleReminder(updatedBirthday)
                
                // 提供触觉反馈
                it.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                true
            }
            
            // 单击提醒图标显示提示
            binding.reminderStatusIcon.setOnClickListener {
                val context = it.context
                val message = if (birthday.isEnabled) "长按关闭提醒" else "长按开启提醒"
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            }

            // 在内容视图上设置点击事件，而不是根SwipeRevealLayout
            binding.cardContent.setOnClickListener { onItemClick(birthday) }
            
            // 重置滑动状态（防止RecyclerView复用问题）
            (binding.root as? SwipeRevealLayout)?.resetPosition()
            
            // 设置滑动删除监听器
            (binding.root as? SwipeRevealLayout)?.onDeleteClickListener = {
                onDeleteClick(birthday)
            }
        }

        override fun onItemSelected() {
            // 拖拽时的视觉反馈：放大、透明度和阴影
            binding.root.animate()
                .alpha(0.8f)
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(150)
                .start()
            binding.root.elevation = 12f
            
            // 添加拖拽指示
            binding.cardContent.setBackgroundResource(R.drawable.card_dragging_background)
        }

        override fun onItemClear() {
            // 拖拽结束时恢复正常状态
            binding.root.animate()
                .alpha(1.0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(200)
                .start()
            binding.root.elevation = 2f
            
            // 恢复正常背景
            binding.cardContent.setBackgroundResource(R.drawable.card_content_background)
        }
    }
}
