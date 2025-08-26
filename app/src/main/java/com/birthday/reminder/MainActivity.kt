package com.birthday.reminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.birthday.reminder.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: BirthdayViewModel
    private lateinit var adapter: BirthdayAdapter
    private lateinit var simpleAdapter: BirthdaySimpleAdapter
    private var isSimpleMode = false
    private lateinit var itemTouchHelper: ItemTouchHelper
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限已授予，可以显示成功提示
            showPermissionGrantedMessage()
        } else {
            // 权限被拒绝，显示设置对话框
            showPermissionDialog()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupRecyclerView()
        setupFab()
        checkNotificationPermission()
        observeData()
        
        // 请求加入电池优化白名单
        requestBatteryOptimizationWhitelist()
    }
    
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_view -> {
                toggleDisplayMode()
                true
            }
            R.id.action_test_notification -> {
                testNotification()
                true
            }
            R.id.action_add_test_birthday -> {
                addTestBirthdayForReminderTest()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 当用户从设置页面返回时，重新检查通知权限状态
        checkNotificationPermissionStatus()
    }
    
    private fun checkNotificationPermissionStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            // 可以在这里添加权限状态变化的处理逻辑
            // 比如显示权限状态的提示或更新UI
        }
    }
    
    private fun setupViewModel() {
        val database = BirthdayDatabase.getDatabase(this)
        val repository = BirthdayRepository(database.birthdayDao())
        val factory = BirthdayViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[BirthdayViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        adapter = BirthdayAdapter(
            onEditClick = { birthday ->
                showAddEditDialog(birthday)
            },
            onDeleteClick = { birthday ->
                showDeleteConfirmDialog(birthday)
            },
            onToggleReminder = { birthday ->
                viewModel.update(birthday)
            }
        )
        
        simpleAdapter = BirthdaySimpleAdapter(
            onItemClick = { birthday ->
                showAddEditDialog(birthday)
            },
            onDeleteClick = { birthday ->
                showDeleteConfirmDialog(birthday)
            },
            onToggleReminder = { birthday ->
                viewModel.update(birthday)
            }
        )
        
        // 设置排序完成监听器，确保两个适配器同步
        adapter.setOnMoveFinishedListener {
            updateSortOrder()
        }
        
        simpleAdapter.setOnMoveFinishedListener {
            updateSortOrder()
        }
        
        // 设置适配器的拖拽完成监听器
        binding.recyclerViewBirthdays.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            // 添加顶部和底部间距，避免边缘硬切
            setPadding(0, 8, 0, 8)
            clipToPadding = false
        }
        
        // 设置拖拽和滑动删除功能
        setupItemTouchHelper()
    }
    
    private fun setupItemTouchHelper() {
        val currentAdapter = if (isSimpleMode) simpleAdapter else adapter
        val callback = ItemMoveCallback(
            adapter = currentAdapter as ItemMoveInterface
        )
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewBirthdays)
    }
    
    private fun setupFab() {
        binding.fabAddBirthday.setOnClickListener {
            showAddEditDialog()
        }
        
        binding.buttonMenu.setOnClickListener {
            showPopupMenu(it)
        }
    }
    
    private fun observeData() {
        viewModel.allBirthdays.observe(this) { birthdays ->
            // 确保两个适配器都收到最新数据
            adapter.submitList(birthdays)
            simpleAdapter.submitList(birthdays)
            
            binding.textViewEmpty.visibility = if (birthdays.isEmpty()) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
    }
    
    private fun toggleDisplayMode() {
        isSimpleMode = !isSimpleMode
        
        // 先分离旧的ItemTouchHelper
        itemTouchHelper.attachToRecyclerView(null)
        
        val currentAdapter = if (isSimpleMode) simpleAdapter else adapter
        binding.recyclerViewBirthdays.adapter = currentAdapter
        
        // 重新设置ItemTouchHelper
        setupItemTouchHelper()
        
        // 不需要重新提交数据，observeData() 已经确保两个适配器都有最新数据
    }
    
    private fun showAddEditDialog(birthday: Birthday? = null) {
        val dialog = AddEditBirthdayDialog.newInstance(birthday)
        dialog.setOnSaveListener { savedBirthday ->
            if (birthday == null) {
                viewModel.insert(savedBirthday)
            } else {
                viewModel.update(savedBirthday)
            }
        }
        dialog.show(supportFragmentManager, "AddEditBirthdayDialog")
    }
    
    // 测试通知功能 - 立即触发通知
    private fun testNotification() {
        // 检查通知权限
        val hasPermission = NotificationHelper.hasNotificationPermission(this)
        val channelEnabled = NotificationHelper.isNotificationChannelEnabled(this)
        
        if (!hasPermission) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("通知权限未授予")
                .setMessage("请先在应用设置中开启通知权限，然后重试。")
                .setPositiveButton("去设置") { _, _ ->
                    // 跳转到应用设置页面
                    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = android.net.Uri.parse("package:$packageName")
                    startActivity(intent)
                }
                .setNegativeButton("取消", null)
                .show()
            return
        }
        
        // 运行完整测试
        val testResults = NotificationTestHelper.runFullNotificationTest(this)
        
        // 显示测试结果
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("通知功能测试")
            .setMessage("测试结果：\n\n$testResults\n\n请检查通知栏是否收到多个测试通知。\n\n测试说明：\n• 立即显示3个不同类型的通知\n• 10秒后会显示一个延迟通知\n• 检查了通知权限和渠道状态")
            .setPositiveButton("确定", null)
            .show()
    }
    
    // 添加一个快速测试生日条目，设置明天生日进行实际测试
    private fun addTestBirthdayForReminderTest() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1) // 明天
        
        val testBirthday = Birthday(
            name = "提醒测试用户",
            gregorianDate = "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-${String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))}",
            lunarDate = null,
            isLunar = false,
            relationship = "测试",
            notes = "这是一个用于测试提醒功能的生日条目",
            reminderDaysBefore = 0, // 今天提醒明天的生日
            isEnabled = true
        )
        
        viewModel.insert(testBirthday)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("测试生日已添加")
            .setMessage("已添加明天生日的测试用户。\n\n系统将在今天上午9点（如果已过则明年）发送提醒通知。\n\n你可以在生日列表中看到这个测试条目，测试完成后可以删除它。")
            .setPositiveButton("确定", null)
            .show()
    }
    
    private fun showDeleteDialog(birthday: Birthday) {
        MaterialAlertDialogBuilder(this)
            .setTitle("删除生日提醒")
            .setMessage("确定要删除 ${birthday.name} 的生日提醒吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.delete(birthday)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    private fun showPermissionGrantedMessage() {
        // 可以使用 Snackbar 或 Toast 显示权限授予成功的消息
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "通知权限已开启，您将收到生日提醒通知",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }
    
    private fun showPermissionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("需要通知权限")
            .setMessage("为了能够及时提醒生日信息，需要开启通知权限。")
            .setPositiveButton("去设置") { _, _ ->
                openNotificationSettings()
            }
            .setNegativeButton("暂不", null)
            .show()
    }
    
    private fun openNotificationSettings() {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0 及以上版本，跳转到应用通知设置页面
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
            } else {
                // Android 8.0 以下版本，跳转到应用详情页面
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
            }
            startActivity(intent)
        } catch (e: Exception) {
            // 如果上述方式失败，尝试跳转到系统设置主页
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(fallbackIntent)
            } catch (fallbackException: Exception) {
                // 最后的备选方案，显示提示信息
                MaterialAlertDialogBuilder(this)
                    .setTitle("无法打开设置")
                    .setMessage("请手动前往系统设置 > 应用管理 > 生日提醒 > 通知权限，开启通知功能。")
                    .setPositiveButton("知道了", null)
                    .show()
            }
        }
    }

    private fun showDeleteConfirmDialog(birthday: Birthday) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_confirmation, null)
        
        // 设置消息内容
        val messageTextView = dialogView.findViewById<TextView>(R.id.dialogMessage)
        messageTextView.text = "确定要删除「${birthday.name}」的生日提醒吗？\n此操作无法撤销。"
        
        // 创建弹框
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
            
        // 设置弹框背景透明，使用自定义圆角背景
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            // 添加进入动画
            attributes?.windowAnimations = android.R.style.Animation_Dialog
        }
        
        // 设置按钮点击事件
        val cancelButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.cancelButton)
        val deleteButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.deleteButton)
        
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        deleteButton.setOnClickListener {
            // 添加触觉反馈
            deleteButton.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
            viewModel.delete(birthday)
            dialog.dismiss()
        }
        
        dialog.show()
        
        // 添加弹框显示动画
        dialogView.alpha = 0f
        dialogView.scaleX = 0.8f
        dialogView.scaleY = 0.8f
        dialogView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }
    
    private fun updateSortOrder() {
        // 获取当前活动适配器的数据
        val currentAdapter = if (isSimpleMode) simpleAdapter else adapter
        val currentList = currentAdapter.currentList
        
        // 更新排序并保存到数据库
        val updatedList = currentList.mapIndexed { index, birthday ->
            birthday.copy(sortOrder = index)
        }
        
        // 同步到ViewModel和数据库
        viewModel.updateSortOrders(updatedList)
    }
    
    private fun showPopupMenu(view: View) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.custom_popup_menu, null)
        
        val popupWindow = PopupWindow(
            popupView,
            resources.getDimensionPixelSize(R.dimen.popup_menu_width),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        
        // 设置背景和动画
        popupWindow.elevation = 8f
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        
        // 设置点击监听器
        popupView.findViewById<View>(R.id.menuToggleView).setOnClickListener {
            toggleDisplayMode()
            popupWindow.dismiss()
        }
        
        popupView.findViewById<View>(R.id.menuTestNotification).setOnClickListener {
            testNotification()
            popupWindow.dismiss()
        }
        
        popupView.findViewById<View>(R.id.menuAddTestBirthday).setOnClickListener {
            addTestBirthdayForReminderTest()
            popupWindow.dismiss()
        }
        
        // 显示弹出窗口
        popupWindow.showAsDropDown(view, -200, 0)
    }
    
    private fun requestBatteryOptimizationWhitelist() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("电池优化设置")
                    .setMessage("为了确保生日提醒能够准时送达，建议将本应用加入电池优化白名单。这不会显著影响电池续航。")
                    .setPositiveButton("前往设置") { _, _ ->
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(intent)
                        } catch (e: Exception) {
                            // 如果直接跳转失败，打开电池优化设置页面
                            try {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                startActivity(intent)
                            } catch (e2: Exception) {
                                e2.printStackTrace()
                            }
                        }
                    }
                    .setNegativeButton("稍后") { _, _ -> }
                    .show()
            }
        }
    }
}
