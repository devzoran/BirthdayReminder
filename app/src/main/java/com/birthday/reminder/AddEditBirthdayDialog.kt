package com.birthday.reminder

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.birthday.reminder.databinding.DialogAddEditBirthdayBinding
import java.text.SimpleDateFormat
import java.util.*

class AddEditBirthdayDialog : DialogFragment() {
    
    private var _binding: DialogAddEditBirthdayBinding? = null
    private val binding get() = _binding!!
    
    private var birthday: Birthday? = null
    private var onSaveListener: ((Birthday) -> Unit)? = null
    private var selectedDate: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    companion object {
        fun newInstance(birthday: Birthday? = null): AddEditBirthdayDialog {
            val dialog = AddEditBirthdayDialog()
            val args = Bundle()
            birthday?.let {
                args.putLong("birthday_id", it.id)
                args.putString("birthday_name", it.name)
                args.putString("birthday_date", it.gregorianDate)
                args.putString("birthday_lunar_date", it.lunarDate)
                args.putBoolean("birthday_is_lunar", it.isLunar)
                args.putString("birthday_relationship", it.relationship)
                args.putString("birthday_notes", it.notes)
                args.putInt("birthday_reminder_days", it.reminderDaysBefore)
                args.putBoolean("birthday_enabled", it.isEnabled)
            }
            dialog.arguments = args
            return dialog
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        arguments?.let { args ->
            if (args.containsKey("birthday_id")) {
                birthday = Birthday(
                    id = args.getLong("birthday_id"),
                    name = args.getString("birthday_name", ""),
                    gregorianDate = args.getString("birthday_date", ""),
                    lunarDate = args.getString("birthday_lunar_date"),
                    isLunar = args.getBoolean("birthday_is_lunar"),
                    relationship = args.getString("birthday_relationship", ""),
                    notes = args.getString("birthday_notes", ""),
                    reminderDaysBefore = args.getInt("birthday_reminder_days", 1),
                    isEnabled = args.getBoolean("birthday_enabled", true)
                )
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddEditBirthdayBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        populateFields()
        setupClickListeners()
    }
    
    override fun onStart() {
        super.onStart()
        
        // 设置弹窗背景透明，避免四个角变黑问题
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            // 设置弹窗宽度为屏幕宽度的90%
            val displayMetrics = requireContext().resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.9).toInt()
            setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
            
            // 添加进入和退出动画
            attributes?.windowAnimations = android.R.style.Animation_Dialog
        }
        
        // 添加视图动画效果
        binding.root.alpha = 0f
        binding.root.scaleX = 0.9f
        binding.root.scaleY = 0.9f
        binding.root.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(250)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }
    
    private fun setupViews() {
        // 设置关系下拉菜单
        val relationships = arrayOf("父亲", "母亲", "兄弟", "姐妹", "爷爷", "奶奶", "外公", "外婆", "配偶", "儿子", "女儿", "朋友", "其他")
        val relationshipAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, relationships)
        binding.autoCompleteRelationship.setAdapter(relationshipAdapter)
        
        // 设置提前提醒天数下拉菜单
        val reminderDays = arrayOf("当天", "1天前", "2天前", "3天前", "7天前")
        val reminderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, reminderDays)
        binding.autoCompleteReminderDays.setAdapter(reminderAdapter)
        
        // MaterialAutoCompleteTextView 会自动处理点击事件，无需手动设置监听器
        Log.d("DropdownDebug", "下拉菜单配置完成")
    }
    
    private fun populateFields() {
        birthday?.let { bday ->
            binding.editTextName.setText(bday.name)
            binding.autoCompleteRelationship.setText(bday.relationship, false)
            binding.editTextNotes.setText(bday.notes)
            binding.switchCalendarType.isChecked = bday.isLunar
            binding.switchReminder.isChecked = bday.isEnabled
            
            // 设置日期
            val dateParts = bday.gregorianDate.split("-")
            if (dateParts.size == 3) {
                selectedDate.set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt())
            }
            updateDateDisplay()
            
            // 设置提前提醒天数
            val reminderText = when (bday.reminderDaysBefore) {
                0 -> "当天"
                1 -> "1天前"
                2 -> "2天前"
                3 -> "3天前"
                7 -> "7天前"
                else -> "${bday.reminderDaysBefore}天前"
            }
            binding.autoCompleteReminderDays.setText(reminderText, false)
        } ?: run {
            updateDateDisplay()
            binding.autoCompleteReminderDays.setText("1天前", false)
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonSelectDate.setOnClickListener {
            showDatePicker()
        }
        
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
        
        binding.buttonSave.setOnClickListener {
            saveBirthday()
        }
        
        binding.switchCalendarType.setOnCheckedChangeListener { _, isChecked ->
            binding.textViewCalendarType.text = if (isChecked) "农历" else "公历"
        }
    }
    
    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateDateDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun updateDateDisplay() {
        binding.textViewSelectedDate.text = dateFormat.format(selectedDate.time)
    }
    
    private fun saveBirthday() {
        val name = binding.editTextName.text.toString().trim()
        if (name.isEmpty()) {
            binding.editTextName.error = "请输入姓名"
            return
        }
        
        val relationship = binding.autoCompleteRelationship.text.toString()
        val notes = binding.editTextNotes.text.toString()
        val isLunar = binding.switchCalendarType.isChecked
        val isEnabled = binding.switchReminder.isChecked
        
        val reminderDays = when (binding.autoCompleteReminderDays.text.toString()) {
            "当天" -> 0
            "1天前" -> 1
            "2天前" -> 2
            "3天前" -> 3
            "7天前" -> 7
            else -> 1
        }
        
        val gregorianDate = dateFormat.format(selectedDate.time)
        val lunarDate = if (isLunar) gregorianDate else null // 简化处理，实际需要公历转农历
        
        val newBirthday = Birthday(
            id = birthday?.id ?: 0,
            name = name,
            gregorianDate = gregorianDate,
            lunarDate = lunarDate,
            isLunar = isLunar,
            relationship = relationship,
            notes = notes,
            reminderDaysBefore = reminderDays,
            isEnabled = isEnabled,
            sortOrder = birthday?.sortOrder ?: 0
        )
        
        onSaveListener?.invoke(newBirthday)
        dismiss()
    }
    
    fun setOnSaveListener(listener: (Birthday) -> Unit) {
        onSaveListener = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
