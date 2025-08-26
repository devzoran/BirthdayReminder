package com.birthday.reminder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.birthday.reminder.databinding.ItemBirthdayBinding
import java.text.SimpleDateFormat
import java.util.*

class BirthdayAdapter(
    private val onEditClick: (Birthday) -> Unit,
    private val onDeleteClick: (Birthday) -> Unit,
    private val onToggleReminder: (Birthday) -> Unit
) : ListAdapter<Birthday, BirthdayAdapter.ViewHolder>(BirthdayDiffCallback()), ItemMoveInterface {
    
    private var tempList: MutableList<Birthday> = mutableListOf()
    private var dragDropComplete: (() -> Unit)? = null

    fun setOnMoveFinishedListener(listener: () -> Unit) {
        dragDropComplete = listener
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
        // 拖拽完成后，直接触发回调而不再submitList，避免额外动画
        if (tempList.isNotEmpty()) {
            // 不再调用submitList，让临时列表成为当前状态
            // tempList将在同步完成后清空
            dragDropComplete?.invoke()
        } else {
            dragDropComplete?.invoke()
        }
    }
    
    // 重写getItem以在拖拽期间使用临时列表
    override fun getItem(position: Int): Birthday {
        return if (tempList.isNotEmpty()) {
            tempList[position]
        } else {
            super.getItem(position)
        }
    }
    
    // 重写getItemCount以在拖拽期间使用临时列表
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
    
    // 无动画地完成拖拽结果，清空临时列表
    fun finalizeDragResult(finalList: List<Birthday>) {
        tempList.clear()
        // 使用 submitList 的无回调版本，但此时DiffUtil会发现内容实际没有变化（因为UI已经是最终状态）
        // 所以不会产生额外动画
        if (currentList != finalList) {
            submitList(finalList)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBirthdayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: ItemBirthdayBinding) :
        RecyclerView.ViewHolder(binding.root), ItemTouchHelperViewHolder {
        
        @SuppressLint("SetTextI18n")
        fun bind(birthday: Birthday) {
            binding.apply {
                textViewName.text = birthday.name
                textViewRelationship.text = birthday.relationship
                
                // 显示生日日期
                val dateText = if (birthday.isLunar) {
                    "农历 ${birthday.lunarDate ?: birthday.gregorianDate}"
                } else {
                    "公历 ${birthday.gregorianDate}"
                }
                textViewDate.text = dateText
                
                // 显示年龄和距离生日天数
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val age = birthday.getAge(currentYear)
                val daysUntil = birthday.getDaysUntilBirthday()
                
                textViewAge.text = "${age}岁"
                
                when {
                    daysUntil == 0 -> {
                        textViewDaysUntil.text = "今天"
                        layoutDaysUntil.setBackgroundResource(R.drawable.days_until_background)
                    }
                    daysUntil > 0 -> {
                        textViewDaysUntil.text = daysUntil.toString()
                        layoutDaysUntil.setBackgroundResource(R.drawable.days_until_background_light)
                    }
                    else -> {
                        textViewDaysUntil.text = "已过"
                        layoutDaysUntil.setBackgroundResource(R.drawable.days_until_background)
                    }
                }
                
                // 在内容视图上设置点击事件，而不是根SwipeRevealLayout
                cardContent.setOnClickListener {
                    onEditClick(birthday)
                }
                
                // 重置滑动状态（防止RecyclerView复用问题）
                (root as? SwipeRevealLayout)?.resetPosition()
                
                // 设置滑动删除监听器
                (root as? SwipeRevealLayout)?.onDeleteClickListener = {
                    onDeleteClick(birthday)
                }
                
                // 备注
                if (birthday.notes.isNotEmpty()) {
                    textViewNotes.text = birthday.notes
                    textViewNotes.visibility = android.view.View.VISIBLE
                } else {
                    textViewNotes.visibility = android.view.View.GONE
                }
                
                // 设置提醒开关状态
                switchReminder.isChecked = birthday.isEnabled
                switchReminder.text = if (birthday.isEnabled) "提醒开启" else "提醒关闭"
                
                // 开关状态变化监听
                switchReminder.setOnCheckedChangeListener { _, isChecked ->
                    val updatedBirthday = birthday.copy(isEnabled = isChecked)
                    onToggleReminder(updatedBirthday)
                }
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
    
    class BirthdayDiffCallback : DiffUtil.ItemCallback<Birthday>() {
        override fun areItemsTheSame(oldItem: Birthday, newItem: Birthday): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Birthday, newItem: Birthday): Boolean {
            return oldItem == newItem
        }
    }
}