package lpt.imeswitch.utils

import android.content.Context
import android.provider.Settings
import android.util.Log
import java.io.DataOutputStream

/**
 * 权限检查工具类
 * 
 * 提供WRITE_SECURE_SETTINGS权限的检查和授予功能
 */
object PermissionChecker {
    private const val TAG = "PermissionChecker"
    
    /**
     * 检查是否具有WRITE_SECURE_SETTINGS权限
     * 
     * 通过尝试读取系统安全设置来验证权限。
     * 如果能够成功读取，说明具有该权限。
     * 
     * @param context 应用上下文
     * @return true表示有权限，false表示无权限
     */
    fun hasWriteSecureSettingsPermission(context: Context): Boolean {
        return try {
            // 尝试读取当前输入法设置
            val currentIme = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            )
            // 如果能读取到值（即使为null），说明有权限
            Log.d(TAG, "权限检查成功，当前输入法: $currentIme")
            true
        } catch (e: SecurityException) {
            // 捕获安全异常，说明没有权限
            Log.w(TAG, "权限检查失败：没有WRITE_SECURE_SETTINGS权限", e)
            false
        } catch (e: Exception) {
            // 捕获其他异常
            Log.e(TAG, "权限检查时发生未知错误", e)
            false
        }
    }
    
    /**
     * 获取ADB授权命令
     * 
     * 生成用于通过ADB授予WRITE_SECURE_SETTINGS权限的完整命令。
     * 用户需要在电脑上执行此命令来授予权限。
     * 
     * @param context 应用上下文
     * @return ADB授权命令字符串
     */
    fun getAdbGrantCommand(context: Context): String {
        val packageName = context.packageName
        return "adb shell pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"
    }
    
    /**
     * 检测设备是否已获取Root权限
     * 
     * 通过尝试执行su命令来检测Root权限。
     * 
     * @return true表示设备已root，false表示未root
     */
    fun checkRootAccess(): Boolean {
        return try {
            // 尝试执行su命令
            val process = Runtime.getRuntime().exec("su -c exit")
            val exitCode = process.waitFor()
            
            val hasRoot = exitCode == 0
            Log.d(TAG, "Root权限检测结果: ${if (hasRoot) "已获取" else "未获取"}")
            hasRoot
        } catch (e: Exception) {
            // 如果执行失败，说明没有Root权限
            Log.d(TAG, "Root权限检测失败，设备未root", e)
            false
        }
    }
    
    /**
     * 通过Root权限授予WRITE_SECURE_SETTINGS权限
     * 
     * 使用su命令执行pm grant命令来授予权限。
     * 此方法需要设备已获取Root权限。
     * 
     * @param context 应用上下文
     * @return true表示授权成功，false表示授权失败
     */
    fun grantPermissionViaRoot(context: Context): Boolean {
        return try {
            val packageName = context.packageName
            val command = "pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"
            
            Log.d(TAG, "尝试通过Root授予权限...")
            
            // 执行su命令
            val process = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(process.outputStream)
            
            // 写入授权命令
            outputStream.writeBytes("$command\n")
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            
            // 等待命令执行完成
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                Log.i(TAG, "Root授权成功")
                true
            } else {
                Log.e(TAG, "Root授权失败，退出码: $exitCode")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Root授权过程中发生错误", e)
            false
        }
    }
}
