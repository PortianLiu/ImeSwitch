package lpt.imeswitch.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import lpt.imeswitch.utils.ImeManager
import lpt.imeswitch.utils.PermissionChecker

/**
 * 快速设置服务
 * 
 * 提供快速设置面板中的输入法切换快捷开关功能
 * - 短按: 轮切到下一个输入法
 * - 长按: 打开ImeSelectionActivity显示输入法选择对话框
 */
class ImeSwitchTileService : TileService() {
    
    companion object {
        private const val TAG = "ImeSwitchTile"
    }
    
    private lateinit var imeManager: ImeManager
    
    /**
     * 快捷开关首次被添加时调用
     * 
     * 初始化快捷开关状态
     */
    override fun onTileAdded() {
        super.onTileAdded()
        Log.d(TAG, "快捷开关已添加")
        imeManager = ImeManager(applicationContext)
        updateTileState()
    }
    
    /**
     * 快捷开关被移除时调用
     * 
     * 清理资源
     */
    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.d(TAG, "快捷开关已移除")
    }
    
    /**
     * 快捷开关可见时调用
     * 
     * 更新快捷开关显示状态
     */
    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "快捷开关开始监听")
        imeManager = ImeManager(applicationContext)
        updateTileState()
    }
    
    /**
     * 快捷开关不可见时调用
     * 
     * 清理资源
     */
    override fun onStopListening() {
        super.onStopListening()
        Log.d(TAG, "快捷开关停止监听")
    }
    
    /**
     * 用户点击快捷开关时调用
     * 
     * 短按: 执行输入法轮切
     * 长按: 系统会自动打开ImeSelectionActivity(通过AndroidManifest配置)
     */
    override fun onClick() {
        super.onClick()
        Log.d(TAG, "快捷开关被点击")
        
        // 检查权限
        if (!PermissionChecker.hasWriteSecureSettingsPermission(applicationContext)) {
            Log.w(TAG, "权限未授予，无法切换输入法")
            updateTileState()
            return
        }
        
        // 检查输入法数量
        val imeIds = imeManager.getEnabledInputMethodIds()
        if (imeIds.size < 2) {
            Log.w(TAG, "输入法数量不足，无法切换")
            updateTileState()
            return
        }
        
        // 短按: 执行轮切
        val success = imeManager.switchToNextInputMethod()
        if (success) {
            Log.i(TAG, "输入法切换成功")
            val currentImeName = imeManager.getCurrentInputMethodName()
            showToast("已切换到: $currentImeName")
        } else {
            Log.e(TAG, "输入法切换失败")
        }
        
        // 更新快捷开关状态
        updateTileState()
    }
    
    /**
     * 显示Toast提示
     * 
     * 注意：在后台服务（TileService）中显示的Toast会被Android系统屏蔽
     */
    private fun showToast(message: String) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            android.widget.Toast.makeText(applicationContext, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 更新快捷开关的显示状态（私有方法）
     * 
     * 根据权限状态和输入法数量设置快捷开关的状态和标签
     */
    private fun updateTileState() {
        val tile = qsTile ?: run {
            Log.w(TAG, "无法获取快捷开关对象")
            return
        }
        
        try {
            // 检查权限
            val hasPermission = PermissionChecker.hasWriteSecureSettingsPermission(applicationContext)
            
            if (!hasPermission) {
                // 权限未授予
                tile.state = Tile.STATE_INACTIVE
                tile.label = "需要授权"
                tile.updateTile()
                Log.d(TAG, "快捷开关状态更新: 需要授权")
                return
            }
            
            // 检查输入法数量
            val imeIds = imeManager.getEnabledInputMethodIds()
            
            if (imeIds.size < 2) {
                // 输入法数量不足，显示功能名称
                tile.state = Tile.STATE_INACTIVE
                tile.label = "输入法切换"
                tile.updateTile()
                Log.d(TAG, "快捷开关状态更新: 输入法数量不足")
                return
            }
            
            // 正常状态：显示当前输入法名称
            tile.state = Tile.STATE_ACTIVE
            val currentImeName = imeManager.getCurrentInputMethodName()
            tile.label = currentImeName
            tile.updateTile()
            Log.d(TAG, "快捷开关状态更新: $currentImeName")
            
        } catch (e: Exception) {
            Log.e(TAG, "更新快捷开关状态失败", e)
            tile.state = Tile.STATE_INACTIVE
            tile.label = "错误"
            tile.updateTile()
        }
    }
}
