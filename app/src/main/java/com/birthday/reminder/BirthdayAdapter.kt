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
    
    private var dragDropComplete: (() -> Unit)? = null

    fun setOnMoveFinishedListener(listener: () -> Unit) {
        dragDropComplete = listener
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        val list = currentList.toMutableList()
        val item = list.removeAt(fromPosition)
        list.add(toPosition, item)
        submitList(list)
    }

    override fun onMoveFinished() {
        dragDropComplete?.invoke()
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
            // 直接设置选中状态，无动画
            binding.root.alpha = 0.95f
            binding.root.scaleX = 1.01f
            binding.root.scaleY = 1.01f
            binding.root.elevation = 6f
        }
        
        override fun onItemClear() {
            // 直接恢复状态，无动画
            binding.root.alpha = 1.0f
            binding.root.scaleX = 1.0f
            binding.root.scaleY = 1.0f
            binding.root.elevation = 0f
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