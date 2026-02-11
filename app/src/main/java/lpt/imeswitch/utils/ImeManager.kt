package lpt.imeswitch.utils

import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager

/**
 * 输入法管理核心类
 * 
 * 封装所有输入法相关操作，包括获取输入法列表、获取当前输入法、切换输入法等
 */
class ImeManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ImeManager"
    }
    
    /**
     * 获取已启用的输入法ID列表
     * 
     * 直接从Settings.Secure.ENABLED_INPUT_METHODS获取，不依赖InputMethodManager，
     * 避免在MIUI等定制系统上获取不完整的列表。
     * 
     * @return 已启用的输入法ID列表
     */
    fun getEnabledInputMethodIds(): List<String> {
        return try {
            // 从Settings.Secure获取已启用的输入法ID列表
            val enabledImeIds = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_INPUT_METHODS
            )
            
            if (enabledImeIds.isNullOrEmpty()) {
                Log.w(TAG, "ENABLED_INPUT_METHODS为空")
                return emptyList()
            }
            
            Log.d(TAG, "ENABLED_INPUT_METHODS: $enabledImeIds")
            
            // 解析输入法ID列表（格式：id1:id2:id3）
            val imeIds = enabledImeIds.split(":").filter { it.isNotEmpty() }
            
            Log.d(TAG, "获取到${imeIds.size}个已启用的输入法ID")
            imeIds.forEachIndexed { index, id ->
                Log.d(TAG, "  [$index] $id")
            }
            
            imeIds
        } catch (e: Exception) {
            Log.e(TAG, "获取已启用输入法ID列表失败", e)
            emptyList()
        }
    }
    
    /**
     * 获取已启用的输入法列表
     * 
     * 使用Settings.Secure.ENABLED_INPUT_METHODS来获取完整的已启用输入法列表，
     * 这比InputMethodManager.enabledInputMethodList更可靠，
     * 特别是在某些定制系统（如MIUI）上。
     * 
     * @return 已启用的输入法信息列表，如果获取失败则返回空列表
     */
    fun getEnabledInputMethods(): List<InputMethodInfo> {
        return try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            if (imm == null) {
                Log.e(TAG, "无法获取InputMethodManager服务")
                return emptyList()
            }
            
            // 从Settings.Secure获取已启用的输入法ID列表
            val enabledImeIds = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_INPUT_METHODS
            )
            
            if (enabledImeIds.isNullOrEmpty()) {
                Log.w(TAG, "ENABLED_INPUT_METHODS为空")
                return emptyList()
            }
            
            Log.d(TAG, "ENABLED_INPUT_METHODS: $enabledImeIds")
            
            // 解析输入法ID列表（格式：id1:id2:id3）
            val enabledIds = enabledImeIds.split(":").toSet()
            
            // 获取所有已安装的输入法
            val allInputMethods = imm.inputMethodList
            
            Log.d(TAG, "inputMethodList返回了${allInputMethods.size}个输入法")
            
            // 过滤出已启用的输入法
            val imeList = allInputMethods.filter { imeInfo ->
                enabledIds.contains(imeInfo.id)
            }
            
            Log.d(TAG, "获取到${imeList.size}个已启用的输入法")
            
            // 打印每个输入法的详细信息
            imeList.forEachIndexed { index, imeInfo ->
                val name = imeInfo.loadLabel(context.packageManager).toString()
                Log.d(TAG, "  [$index] ${imeInfo.id} - $name")
            }
            
            imeList
        } catch (e: Exception) {
            Log.e(TAG, "获取已启用输入法列表失败", e)
            emptyList()
        }
    }
    
    /**
     * 获取当前输入法ID
     * 
     * @return 当前输入法的唯一标识符，格式为"包名/服务类名"，如果获取失败则返回null
     */
    fun getCurrentInputMethodId(): String? {
        return try {
            val currentImeId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            )
            Log.d(TAG, "当前输入法ID: $currentImeId")
            currentImeId
        } catch (e: SecurityException) {
            Log.e(TAG, "权限不足，无法获取当前输入法", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "获取当前输入法失败", e)
            null
        }
    }
    
    /**
     * 获取当前输入法的显示名称
     * 
     * @return 当前输入法的显示名称，如果获取失败则返回默认文本
     */
    fun getCurrentInputMethodName(): String {
        return try {
            val currentImeId = getCurrentInputMethodId()
            if (currentImeId == null) {
                Log.w(TAG, "当前输入法ID为null")
                return "未知输入法"
            }
            
            getInputMethodName(currentImeId)
        } catch (e: Exception) {
            Log.e(TAG, "获取当前输入法名称失败", e)
            "输入法"
        }
    }
    
    /**
     * 根据输入法ID获取输入法的显示名称
     * 
     * @param imeId 输入法的唯一标识符
     * @return 输入法的显示名称，如果找不到则返回默认文本
     */
    fun getInputMethodName(imeId: String): String {
        return try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            if (imm == null) {
                Log.e(TAG, "无法获取InputMethodManager服务")
                return "未知输入法"
            }
            
            val imeList = imm.enabledInputMethodList
            val targetIme = imeList.find { it.id == imeId }
            
            if (targetIme != null) {
                val name = targetIme.loadLabel(context.packageManager).toString()
                Log.d(TAG, "输入法[$imeId]的名称: $name")
                name
            } else {
                Log.w(TAG, "未找到输入法[$imeId]")
                "未知输入法"
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取输入法名称失败", e)
            "未知输入法"
        }
    }
    
    /**
     * 切换到下一个输入法
     * 
     * 在已启用的输入法列表中循环切换。
     * 如果当前输入法是列表中的最后一个，则切换到第一个。
     * 
     * @return true表示切换成功，false表示切换失败
     */
    fun switchToNextInputMethod(): Boolean {
        return try {
            // 获取已启用的输入法ID列表
            val imeIds = getEnabledInputMethodIds()
            
            // 检查输入法数量
            if (imeIds.isEmpty()) {
                Log.w(TAG, "没有已启用的输入法")
                return false
            }
            
            if (imeIds.size == 1) {
                Log.w(TAG, "只有一个输入法，无法切换")
                return false
            }
            
            // 获取当前输入法ID
            val currentImeId = getCurrentInputMethodId()
            if (currentImeId == null) {
                Log.e(TAG, "无法获取当前输入法ID")
                return false
            }
            
            // 计算下一个输入法ID
            val nextImeId = getNextInputMethodId(currentImeId, imeIds)
            if (nextImeId == null) {
                Log.e(TAG, "无法计算下一个输入法ID")
                return false
            }
            
            // 执行切换
            Settings.Secure.putString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD,
                nextImeId
            )
            
            val nextImeName = getInputMethodName(nextImeId)
            Log.i(TAG, "成功切换到: $nextImeName ($nextImeId)")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "权限不足，无法切换输入法", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "切换输入法失败", e)
            false
        }
    }
    
    /**
     * 计算下一个输入法ID（私有方法）
     * 
     * 在输入法ID列表中找到当前输入法的位置，然后返回下一个输入法的ID。
     * 如果当前输入法是最后一个，则返回第一个（循环）。
     * 
     * @param currentId 当前输入法ID
     * @param imeIds 已启用的输入法ID列表
     * @return 下一个输入法的ID，如果计算失败则返回null
     */
    private fun getNextInputMethodId(currentId: String, imeIds: List<String>): String? {
        if (imeIds.isEmpty()) {
            Log.w(TAG, "输入法ID列表为空")
            return null
        }
        
        if (imeIds.size == 1) {
            Log.w(TAG, "只有一个输入法")
            return null
        }
        
        // 在列表中找到当前输入法的位置
        val currentIndex = imeIds.indexOf(currentId)
        
        if (currentIndex == -1) {
            // 当前输入法不在列表中，返回第一个
            Log.w(TAG, "当前输入法不在列表中，返回第一个")
            return imeIds[0]
        }
        
        // 计算下一个位置（循环）
        val nextIndex = (currentIndex + 1) % imeIds.size
        val nextImeId = imeIds[nextIndex]
        
        Log.d(TAG, "当前位置: $currentIndex, 下一个位置: $nextIndex")
        return nextImeId
    }
}
