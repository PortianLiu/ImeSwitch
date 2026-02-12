package lpt.imeswitch.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import lpt.imeswitch.utils.ImeManager
import lpt.imeswitch.utils.PermissionChecker

/**
 * 输入法选择Activity
 * 
 * 透明Activity,用于显示输入法选择对话框
 * 长按快捷开关时会打开此Activity
 */
class ImeSelectionActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "ImeSelectionActivity"
    }
    
    private lateinit var imeManager: ImeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Activity创建")
        
        // 初始化
        imeManager = ImeManager(this)
        
        // 显示输入法选择对话框
        showInputMethodSelectionDialog()
    }
    
    /**
     * 显示输入法选择对话框
     */
    private fun showInputMethodSelectionDialog() {
        Log.d(TAG, "显示输入法选择对话框")
        
        // 检查权限
        if (!PermissionChecker.hasWriteSecureSettingsPermission(this)) {
            Log.w(TAG, "权限未授予")
            showPermissionDialog()
            return
        }
        
        // 获取已启用的输入法ID列表
        val imeIds = imeManager.getEnabledInputMethodIds()
        if (imeIds.isEmpty()) {
            Log.w(TAG, "没有已启用的输入法")
            showErrorDialog("没有可用的输入法")
            return
        }
        
        // 获取当前输入法ID
        val currentImeId = imeManager.getCurrentInputMethodId()
        
        // 准备对话框数据 - 使用ID列表获取名称
        val imeNames = imeIds.map { imeId ->
            imeManager.getInputMethodName(imeId)
        }.toTypedArray()
        val currentIndex = imeIds.indexOf(currentImeId)
        
        // 创建并显示对话框
        AlertDialog.Builder(this)
            .setTitle("选择输入法")
            .setSingleChoiceItems(imeNames, currentIndex) { dialog, which ->
                // 用户选择了某个输入法
                val selectedImeId = imeIds[which]
                Log.d(TAG, "用户选择: $selectedImeId")
                
                // 切换到选中的输入法
                val success = imeManager.switchToInputMethod(selectedImeId)
                if (success) {
                    val imeName = imeManager.getInputMethodName(selectedImeId)
                    Toast.makeText(this, "已切换到: $imeName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "切换失败", Toast.LENGTH_SHORT).show()
                }
                
                dialog.dismiss()
                finish()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .show()
    }
    
    /**
     * 显示权限提示对话框
     */
    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要授权")
            .setMessage("请先在应用中完成权限授予")
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .show()
    }
    
    /**
     * 显示错误对话框
     */
    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("错误")
            .setMessage(message)
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .show()
    }
}
