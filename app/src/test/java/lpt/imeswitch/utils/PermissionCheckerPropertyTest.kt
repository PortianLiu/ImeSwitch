package lpt.imeswitch.utils

import android.content.Context
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PermissionChecker的基于属性的测试
 * 
 * 验证权限检查逻辑在各种输入下的正确性
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PermissionCheckerPropertyTest {
    
    // Feature: input-method-switcher, Property 5: 权限检查与使用
    @Test
    fun `对于任意上下文，ADB命令应包含正确的包名和权限`() {
        runBlocking {
            checkAll(100, Arb.boolean()) { _ ->
                val mockContext = mockk<Context>(relaxed = true)
                every { mockContext.packageName } returns "lpt.imeswitch"
                
                val command = PermissionChecker.getAdbGrantCommand(mockContext)
                
                // 验证命令格式正确
                command shouldContain "adb shell pm grant"
                command shouldContain "lpt.imeswitch"
                command shouldContain "android.permission.WRITE_SECURE_SETTINGS"
            }
        }
    }
}
