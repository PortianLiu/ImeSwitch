package lpt.imeswitch.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import lpt.imeswitch.R
import lpt.imeswitch.databinding.ActivityMainBinding
import lpt.imeswitch.utils.ImeManager
import lpt.imeswitch.utils.PermissionChecker

/**
 * 主界面Activity
 * 
 * 提供用户引导和权限检测功能，支持零UI快速切换模式
 */
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        private const val PREF_NAME = "ime_switch_prefs"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var imeManager: ImeManager
    private lateinit var prefs: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化
        imeManager = ImeManager(this)
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        // 检查权限并决定行为模式
        checkPermissionAndDecideMode()
    }
    
    /**
     * 检查权限并决定行为模式
     * 
     * 如果权限已授予且非首次启动，执行快速切换模式
     * 否则显示引导界面
     */
    private fun checkPermissionAndDecideMode() {
        val hasPermission = PermissionChecker.hasWriteSecureSettingsPermission(this)
        val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        
        if (hasPermission && !isFirstLaunch) {
            // 快速切换模式
            Log.d(TAG, "进入快速切换模式")
            executeQuickSwitchMode()
        } else {
            // 引导模式
            Log.d(TAG, "进入引导模式")
            showGuideMode()
        }
    }
    
    /**
     * 执行快速切换模式
     * 
     * 直接执行输入法切换并关闭Activity
     */
    private fun executeQuickSwitchMode() {
        // 检查输入法数量
        val imeList = imeManager.getEnabledInputMethods()
        if (imeList.size < 2) {
            Toast.makeText(
                this,
                getString(R.string.insufficient_imes),
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }
        
        // 执行切换
        val success = imeManager.switchToNextInputMethod()
        
        if (success) {
            val currentImeName = imeManager.getCurrentInputMethodName()
            Toast.makeText(
                this,
                getString(R.string.switched_to, currentImeName),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                getString(R.string.switch_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
        
        // 立即关闭Activity
        finish()
    }
    
    /**
     * 显示引导界面
     * 
     * 显示完整的UI界面，提供使用说明和权限授予指引
     */
    private fun showGuideMode() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置ADB命令文本
        val adbCommand = PermissionChecker.getAdbGrantCommand(this)
        binding.adbCommandText.text = adbCommand
        
        // 设置Tab切换监听
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // ADB授权Tab
                        binding.adbTabContent.visibility = View.VISIBLE
                        binding.rootTabContent.visibility = View.GONE
                    }
                    1 -> {
                        // Root授权Tab
                        binding.adbTabContent.visibility = View.GONE
                        binding.rootTabContent.visibility = View.VISIBLE
                        checkRootStatus()
                    }
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        
        // 复制ADB命令按钮
        binding.copyAdbCommandButton.setOnClickListener {
            copyAdbCommand()
        }
        
        // Root授权按钮
        binding.grantViaRootButton.setOnClickListener {
            grantViaRoot()
        }
        
        // 检测权限按钮
        binding.checkPermissionButton.setOnClickListener {
            refreshPermissionStatus()
        }
        
        // 完成按钮
        binding.completeButton.setOnClickListener {
            // 标记非首次启动
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
            Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
            finish()
        }
        
        // 初始化权限状态显示
        refreshPermissionStatus()
    }
    
    /**
     * 检查Root状态
     */
    private fun checkRootStatus() {
        val hasRoot = PermissionChecker.checkRootAccess()
        binding.rootStatusText.text = if (hasRoot) {
            getString(R.string.root_available)
        } else {
            getString(R.string.root_not_available)
        }
        binding.grantViaRootButton.isEnabled = hasRoot
    }
    
    /**
     * 复制ADB命令到剪贴板
     */
    private fun copyAdbCommand() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ADB Command", binding.adbCommandText.text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.command_copied), Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 通过Root授予权限
     */
    private fun grantViaRoot() {
        val success = PermissionChecker.grantPermissionViaRoot(this)
        
        if (success) {
            Toast.makeText(this, getString(R.string.root_grant_success), Toast.LENGTH_SHORT).show()
            refreshPermissionStatus()
        } else {
            Toast.makeText(this, getString(R.string.root_grant_failed), Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 刷新权限状态显示
     */
    private fun refreshPermissionStatus() {
        val hasPermission = PermissionChecker.hasWriteSecureSettingsPermission(this)
        
        binding.permissionStatusText.text = if (hasPermission) {
            getString(R.string.permission_granted)
        } else {
            getString(R.string.permission_not_granted)
        }
        
        // 设置文本颜色
        val color = if (hasPermission) {
            ContextCompat.getColor(this, android.R.color.holo_green_dark)
        } else {
            ContextCompat.getColor(this, android.R.color.holo_red_dark)
        }
        binding.permissionStatusText.setTextColor(color)
        
        Log.d(TAG, "权限状态: ${if (hasPermission) "已授予" else "未授予"}")
    }
}
