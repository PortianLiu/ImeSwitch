package lpt.imeswitch.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import lpt.imeswitch.utils.ImeManager
import lpt.imeswitch.utils.PermissionChecker

/**
 * 快速设置瓦片服务
 * 
 * 提供快速设置面板中的输入法切换瓦片功能
 */
class ImeSwitchTileService : TileService() {
    
    companion object {
        private const val TAG = "ImeSwitchTile"
    }
    
    private lateinit var imeManager: ImeManager
    
    /**
     * 瓦片首次被添加时调用
     * 
     * 初始化瓦片状态
     */
    override fun onTileAdded() {
        super.onTileAdded()
        Log.d(TAG, "瓦片已添加")
        imeManager = ImeManager(applicationContext)
        updateTileState()
    }
    
    /**
     * 瓦片被移除时调用
     * 
     * 清理资源
     */
    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.d(TAG, "瓦片已移除")
    }
    
    /**
     * 瓦片可见时调用
     * 
     * 更新瓦片显示状态
     */
    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "瓦片开始监听")
        imeManager = ImeManager(applicationContext)
        updateTileState()
    }
    
    /**
     * 瓦片不可见时调用
     * 
     * 清理资源
     */
    override fun onStopListening() {
        super.onStopListening()
        Log.d(TAG, "瓦片停止监听")
    }
    
    /**
     * 用户点击瓦片时调用
     * 
     * 执行输入法切换操作并更新瓦片状态
     */
    override fun onClick() {
        super.onClick()
        Log.d(TAG, "瓦片被点击")
        
        // 检查权限
        if (!PermissionChecker.hasWriteSecureSettingsPermission(applicationContext)) {
            Log.w(TAG, "权限未授予，无法切换输入法")
            updateTileState()
            return
        }
        
        // 检查输入法数量
        val imeList = imeManager.getEnabledInputMethods()
        if (imeList.size < 2) {
            Log.w(TAG, "输入法数量不足，无法切换")
            updateTileState()
            return
        }
        
        // 执行切换
        val success = imeManager.switchToNextInputMethod()
        if (success) {
            Log.i(TAG, "输入法切换成功")
        } else {
            Log.e(TAG, "输入法切换失败")
        }
        
        // 更新瓦片状态
        updateTileState()
    }
    
    /**
     * 更新瓦片的显示状态（私有方法）
     * 
     * 根据权限状态和输入法数量设置瓦片的状态和标签
     */
    private fun updateTileState() {
        val tile = qsTile ?: run {
            Log.w(TAG, "无法获取瓦片对象")
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
                Log.d(TAG, "瓦片状态更新: 需要授权")
                return
            }
            
            // 检查输入法数量
            val imeList = imeManager.getEnabledInputMethods()
            
            if (imeList.size < 2) {
                // 输入法数量不足
                tile.state = Tile.STATE_INACTIVE
                tile.label = when (imeList.size) {
                    0 -> "无输入法"
                    1 -> "请启用更多输入法"
                    else -> "输入法不足"
                }
                tile.updateTile()
                Log.d(TAG, "瓦片状态更新: 输入法数量不足")
                return
            }
            
            // 正常状态：显示当前输入法名称
            tile.state = Tile.STATE_ACTIVE
            val currentImeName = imeManager.getCurrentInputMethodName()
            tile.label = currentImeName
            tile.updateTile()
            Log.d(TAG, "瓦片状态更新: $currentImeName")
            
        } catch (e: Exception) {
            Log.e(TAG, "更新瓦片状态失败", e)
            tile.state = Tile.STATE_INACTIVE
            tile.label = "错误"
            tile.updateTile()
        }
    }
}
