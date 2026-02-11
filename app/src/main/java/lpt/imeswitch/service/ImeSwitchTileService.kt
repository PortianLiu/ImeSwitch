package lpt.imeswitch.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.core.app.NotificationCompat
import lpt.imeswitch.R
import lpt.imeswitch.utils.ImeManager
import lpt.imeswitch.utils.PermissionChecker

/**
 * 快速设置服务
 * 
 * 提供快速设置面板中的输入法切换快捷开关功能
 */
class ImeSwitchTileService : TileService() {
    
    companion object {
        private const val TAG = "ImeSwitchTile"
        private const val CHANNEL_ID = "ime_switch_channel"
        private const val NOTIFICATION_ID = 1001
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
        createNotificationChannel()
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
        createNotificationChannel()
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
     * 执行输入法切换操作并更新快捷开关状态
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
        
        // 执行切换
        val success = imeManager.switchToNextInputMethod()
        if (success) {
            Log.i(TAG, "输入法切换成功")
            // 显示切换成功的通知
            val currentImeName = imeManager.getCurrentInputMethodName()
            showNotification("已切换到: $currentImeName")
        } else {
            Log.e(TAG, "输入法切换失败")
        }
        
        // 更新快捷开关状态
        updateTileState()
    }
    
    /**
     * 创建通知渠道（Android 8.0+需要）
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "输入法切换通知"
            val descriptionText = "显示输入法切换结果"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 显示通知
     */
    private fun showNotification(message: String) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_preferences)
            .setContentTitle("输入法切换")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(2000) // 2秒后自动消失
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
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
                // 输入法数量不足
                tile.state = Tile.STATE_INACTIVE
                tile.label = when (imeIds.size) {
                    0 -> "无输入法"
                    1 -> "请启用更多输入法"
                    else -> "输入法不足"
                }
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
