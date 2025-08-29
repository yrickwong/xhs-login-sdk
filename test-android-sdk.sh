#!/bin/bash

# Android SDK 与 OAuth 服务器集成测试脚本
# 模拟 Android SDK 的 HTTP 请求流程

set -e

echo "=== Android SDK 与 OAuth 服务器集成测试 ==="
echo

# 配置
BASE_URL="http://localhost:8080"
APP_ID="test_app_id"
APP_SECRET="test_app_secret"
USER_ID="test_user_123"

# 生成 PKCE 参数
echo "1. 生成 PKCE 参数..."
# 生成128字符的code verifier（与Android SDK一致）
CHARSET="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
CODE_VERIFIER=""
for i in {1..128}; do
    CODE_VERIFIER="${CODE_VERIFIER}${CHARSET:$((RANDOM % ${#CHARSET})):1}"
done
# 使用与服务器端相同的SHA256+Base64URL编码
CODE_CHALLENGE=$(echo -n "$CODE_VERIFIER" | openssl dgst -sha256 -binary | openssl base64 | tr '+/' '-_' | tr -d '=')
echo "   Code Verifier: $CODE_VERIFIER"
echo "   Code Challenge: $CODE_CHALLENGE"
echo

# 2. 测试服务器健康状态
echo "2. 测试服务器健康状态..."
HEALTH_RESPONSE=$(curl -s "$BASE_URL/health")
echo "   服务器状态: $HEALTH_RESPONSE"
echo

# 3. 获取授权码（模拟Android SDK授权请求）
echo "3. 获取授权码（模拟Android授权）..."
# 获取重定向响应中的Location header
REDIRECT_LOCATION=$(curl -s -I -X GET "$BASE_URL/oauth/authorize?client_id=$APP_ID&scope=basic_info,user_profile&redirect_uri=com.xiaohongshu.demo://oauth/callback&code_challenge=$CODE_CHALLENGE&code_challenge_method=S256&state=test_state_android" | grep -i "location:" | sed 's/location: //i' | tr -d '\r')

echo "   重定向URL: $REDIRECT_LOCATION"

# 从重定向URL中提取授权码
AUTH_CODE=$(echo "$REDIRECT_LOCATION" | sed 's/.*code=\([^&]*\).*/\1/')
if [ -z "$AUTH_CODE" ]; then
    echo "❌ 获取授权码失败"
    exit 1
fi
echo "   授权码: $AUTH_CODE"
echo

# 4. 换取访问令牌（模拟Android SDK令牌请求）
echo "4. 换取访问令牌（模拟Android SDK请求）..."
TOKEN_RESPONSE=$(curl -s -X POST "$BASE_URL/oauth/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&code=$AUTH_CODE&client_id=$APP_ID&client_secret=$APP_SECRET&code_verifier=$CODE_VERIFIER&redirect_uri=com.xiaohongshu.demo://oauth/callback")

echo "   令牌响应: $TOKEN_RESPONSE"

# 提取访问令牌（注意字段名是下划线格式）
ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.access_token // .accessToken // empty')
REFRESH_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.refresh_token // .refreshToken // empty')

if [ -z "$ACCESS_TOKEN" ] || [ "$ACCESS_TOKEN" = "null" ]; then
    echo "❌ 获取访问令牌失败"
    exit 1
fi

echo "   ✅ 访问令牌: ${ACCESS_TOKEN:0:20}..."
echo "   ✅ 刷新令牌: ${REFRESH_TOKEN:0:20}..."
echo

# 5. 获取用户信息（模拟Android SDK用户信息请求）
echo "5. 获取用户信息（模拟Android SDK请求）..."
USER_INFO_RESPONSE=$(curl -s -X GET "$BASE_URL/oauth/userinfo" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "   用户信息响应: $USER_INFO_RESPONSE"

USER_NICKNAME=$(echo "$USER_INFO_RESPONSE" | jq -r '.nickname // empty')
if [ -z "$USER_NICKNAME" ] || [ "$USER_NICKNAME" = "null" ]; then
    echo "❌ 获取用户信息失败"
    exit 1
fi

echo "   ✅ 用户昵称: $USER_NICKNAME"
echo

# 6. 刷新令牌测试（模拟Android SDK刷新请求）
echo "6. 刷新访问令牌（模拟Android SDK请求）..."
REFRESH_RESPONSE=$(curl -s -X POST "$BASE_URL/oauth/refresh_token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token&refresh_token=$REFRESH_TOKEN&client_id=$APP_ID&client_secret=$APP_SECRET")

echo "   刷新响应: $REFRESH_RESPONSE"

NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESPONSE" | jq -r '.access_token // .accessToken // empty')
if [ -z "$NEW_ACCESS_TOKEN" ] || [ "$NEW_ACCESS_TOKEN" = "null" ]; then
    echo "❌ 刷新令牌失败"
    exit 1
fi

echo "   ✅ 新访问令牌: ${NEW_ACCESS_TOKEN:0:20}..."
echo

echo "🎉 所有测试通过！Android SDK 与 OAuth 服务器集成成功！"
echo
echo "Android SDK 配置确认："
echo "- BASE_URL: $BASE_URL"
echo "- APP_ID: $APP_ID"
echo "- APP_SECRET: $APP_SECRET"
echo
echo "✅ OAuth 流程完整性验证："
echo "✅ 1. PKCE 参数生成正确"
echo "✅ 2. 授权码获取成功"  
echo "✅ 3. 访问令牌换取成功"
echo "✅ 4. 用户信息获取成功"
echo "✅ 5. 令牌刷新功能正常"