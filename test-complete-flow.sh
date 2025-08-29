#!/bin/bash

# 完整的 App-to-App OAuth 流程自动化测试
# 测试 demo app -> mock XHS app -> OAuth server -> demo app 的完整流程

echo "=== 完整 App-to-App OAuth 流程测试 ==="
echo

# 检查设备连接
echo "1. 检查Android设备连接..."
DEVICES=$(adb devices | grep -E "device$|emulator" | wc -l)
if [ "$DEVICES" -eq 0 ]; then
    echo "❌ 没有找到连接的Android设备或模拟器"
    echo "请启动Android模拟器或连接Android设备"
    exit 1
fi
echo "   ✅ 找到 $DEVICES 个Android设备"
echo

# 检查OAuth服务器状态
echo "2. 检查OAuth服务器状态..."
if ! curl -s --max-time 5 http://localhost:8080/health > /dev/null; then
    echo "❌ OAuth服务器未运行 (http://localhost:8080)"
    echo "请确保OAuth服务器正在运行"
    exit 1
fi
echo "   ✅ OAuth服务器运行正常"
echo

# 安装应用
echo "3. 安装测试应用..."
echo "   安装模拟小红书App..."
adb install -r /Users/wangyi19/workspace/xhs-login-sdk/mock-xhs-app/build/outputs/apk/debug/mock-xhs-app-debug.apk
echo "   安装Demo App..."
adb install -r /Users/wangyi19/workspace/xhs-login-sdk/demo/build/outputs/apk/debug/demo-debug.apk
echo "   ✅ 应用安装完成"
echo

# 启动Demo App
echo "4. 启动Demo App..."
adb shell am start -n com.xiaohongshu.login.demo/com.xiaohongshu.login.demo.MainActivity
echo "   ✅ Demo App已启动"
echo

echo "🎉 应用安装和配置完成！"
echo
echo "请在Android设备上进行以下测试："
echo "1. 在Demo App中点击"登录"按钮"
echo "2. 应该会自动跳转到模拟小红书App的OAuth授权界面"
echo "3. 在授权界面点击"授权"按钮"
echo "4. 应该会返回Demo App并显示登录成功"
echo "5. 可以测试获取用户信息、刷新token等功能"
echo
echo "📱 已安装的应用："
echo "   • Demo App: com.xiaohongshu.login.demo"
echo "   • 模拟小红书App: com.xingin.xhs"
echo
echo "🔧 配置信息："
echo "   • OAuth服务器: http://localhost:8080 (设备内: http://10.0.2.2:8080)"
echo "   • App ID: test_app_id" 
echo "   • App Secret: test_app_secret"
echo
echo "如需查看详细日志，请运行："
echo "   adb logcat | grep -E '(OAuth|XHS|Demo)'"