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
 * 提供用户引导和权限授予功能
 */
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var imeManager: ImeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 启用edge-to-edge显示
        window.decorView.systemUiVisibility = 
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or 
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        
        // 初始化
        imeManager = ImeManager(this)
        
        // 显示引导界面
        showGuideMode()
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
                        // 显示Root授权说明，不自动检测
                        binding.rootStatusText.text = "如果您的设备已获取Root权限，可以点击下方按钮一键授权"
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
            // 直接尝试Root授权，不需要先检测
            grantViaRoot()
        }
        
        // 检测权限按钮
        binding.checkPermissionButton.setOnClickListener {
            refreshPermissionStatus()
        }
        
        // 初始化权限状态显示
        refreshPermissionStatus()
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
            Toast.makeText(this, "授权失败，请确保设备已获取Root权限", Toast.LENGTH_LONG).show()
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
